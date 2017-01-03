/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.spi.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.scout.sdk.core.model.api.IFileLocator;
import org.eclipse.scout.sdk.core.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AbstractJavaElementImplementor;
import org.eclipse.scout.sdk.core.model.api.internal.JavaEnvironmentImplementor;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.model.spi.internal.SpiWithJdtUtils.TypeDescriptor;
import org.eclipse.scout.sdk.core.util.CompositeObject;

/**
 * <h3>{@link JavaEnvironmentWithJdt}</h3>
 *
 * @author Ivan Motsch, Matthias Villiger
 * @since 5.1.0
 */
public class JavaEnvironmentWithJdt implements JavaEnvironmentSpi {

  private static final Object NULL_OBJECT = new Object();
  private final IFileLocator m_fileLocator;
  private final Set<ClasspathEntry> m_classpaths;
  private final AstCompiler m_compiler;
  private final WorkspaceFileSystem m_nameEnv;
  private final Map<Object, JavaElementSpi> m_compilerCache;
  private final Map<Object, Object> m_performanceCache;
  private final AtomicInteger m_hashSeq;

  private IJavaEnvironment m_api;

  public JavaEnvironmentWithJdt(IFileLocator fileLocator, List<ClasspathEntry> classpaths) {
    m_compilerCache = new HashMap<>();
    m_performanceCache = new HashMap<>();
    m_hashSeq = new AtomicInteger();
    m_fileLocator = fileLocator;
    m_classpaths = new LinkedHashSet<>(classpaths);
    m_nameEnv = new WorkspaceFileSystem(ClasspathEntry.toClassPaths(m_classpaths));
    m_compiler = new AstCompiler(m_nameEnv);
    m_api = new JavaEnvironmentImplementor(this);
  }

  @Override
  public IJavaEnvironment wrap() {
    return m_api;
  }

  @Override
  public PackageSpi getPackage(String name) {
    return createPackage(name);
  }

  protected CompositeObject createTypeKey(String fqn) {
    return new CompositeObject(TypeSpi.class, fqn);
  }

  @Override
  public synchronized TypeSpi findType(String fqn) {
    final CompositeObject key = createTypeKey(fqn);
    Object elem = m_performanceCache.get(key);
    if (elem == null) {
      elem = doFindType(fqn);
      m_performanceCache.put(key, elem);
    }
    if (elem == NULL_OBJECT) {
      return null;
    }
    return (TypeSpi) elem;
  }

  /**
   * Performs a search for the given fqn
   *
   * @param fqn
   * @return The result. Is never {@code null}. If the item was not found {@link #NULL_OBJECT} is returned instead.
   */
  protected Object doFindType(String fqn) {
    Object result = null;
    TypeDescriptor desc = SpiWithJdtUtils.getTypeDescriptor(fqn);
    TypeBinding binding = SpiWithJdtUtils.findTypeBinding(desc.m_primaryTypeName, m_compiler);
    if (binding != null) {
      if (desc.hasInnerType()) {
        result = SpiWithJdtUtils.bindingToInnerType(this, binding, desc.m_innerTypeNames);
      }
      else {
        // no inner types: directly return answer
        result = SpiWithJdtUtils.bindingToType(this, binding);
      }
      if (desc.m_arrayDimension > 0 && result instanceof AbstractTypeWithJdt) {
        TypeBinding b = ((AbstractTypeWithJdt) result).getInternalBinding();
        if (b != null) {
          result = SpiWithJdtUtils.bindingToType(this, m_compiler.lookupEnvironment.createArrayType(b, desc.m_arrayDimension));
        }
      }
    }

    if (result == null) {
      return NULL_OBJECT;
    }
    return result;
  }

  @Override
  public IFileLocator getFileLocator() {
    return m_fileLocator;
  }

  @Override
  public JavaEnvironmentWithJdt emptyCopy() {
    List<ClasspathSpi> classpath = getClasspath();
    List<ClasspathEntry> newClasspath = new ArrayList<>(classpath.size());
    for (ClasspathSpi spi : classpath) {
      Classpath newCpEntry = WorkspaceFileSystem.createClasspath(new File(spi.getPath()), spi.isSource(), spi.getEncoding());
      if (newCpEntry != null) {
        newClasspath.add(new ClasspathEntry(newCpEntry, spi.getEncoding()));
      }
    }
    JavaEnvironmentWithJdt newEnv = new JavaEnvironmentWithJdt(m_fileLocator, newClasspath);
    for (org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu : m_nameEnv.getOverrideCompilationUnits()) {
      newEnv.registerCompilationUnitOverride(CharOperation.toString(cu.getPackageName()), new String(cu.getFileName()), cu.getContents());
    }
    return newEnv;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public synchronized JavaEnvironmentSpi reload() {
    JavaEnvironmentWithJdt oldEnv = this;
    JavaEnvironmentWithJdt newEnv = emptyCopy();

    //reload spi of all objects that origin from this environment
    newEnv.m_api = oldEnv.m_api;
    ((JavaEnvironmentImplementor) newEnv.m_api).internalSetSpi(newEnv);
    JavaElementSpi[] oldSpiArray = oldEnv.m_compilerCache.values().toArray(new JavaElementSpi[oldEnv.m_compilerCache.size()]);
    JavaElementSpi[] newSpiArray = new JavaElementSpi[oldSpiArray.length];
    IJavaElement[] apiArray = new IJavaElement[oldSpiArray.length];
    //first collect all new spi/api mappings without modifying anything
    for (int i = 0; i < apiArray.length; i++) {
      apiArray[i] = oldSpiArray[i].wrap();
      if (apiArray[i] == null) {
        continue;
      }
      newSpiArray[i] = ((AbstractJavaElementWithJdt<?>) oldSpiArray[i]).internalFindNewElement(newEnv);
    }
    //now update cores of api with new spi
    for (int i = 0; i < apiArray.length; i++) {
      if (apiArray[i] == null) {
        continue;
      }
      ((AbstractJavaElementImplementor) apiArray[i]).internalSetSpi(newSpiArray[i]);
      if (newSpiArray[i] != null) {
        ((AbstractJavaElementWithJdt) newSpiArray[i]).internalSetApi(apiArray[i]);
      }
    }
    return newEnv;
  }

  @Override
  public String getCompileErrors(String fqn) {
    TypeSpi typeSpi = findType(fqn);
    if (typeSpi == null) {
      throw new IllegalArgumentException("Cannot find type '" + fqn + "'.");
    }
    CompilationUnitSpi cuSpi = typeSpi.getCompilationUnit();
    if (!(cuSpi instanceof DeclarationCompilationUnitWithJdt)) {
      throw new IllegalArgumentException("Type '" + fqn + "' is not a source type.");
    }
    CompilationUnitDeclaration decl = ((DeclarationCompilationUnitWithJdt) cuSpi).getInternalCompilationUnitDeclaration();
    return m_compiler.getCompileErrors(decl);
  }

  @Override
  public synchronized boolean registerCompilationUnitOverride(String packageName, String fileName, char[] src) {
    Validate.notNull(fileName);
    Validate.notNull(src);

    StringBasedJdtCompilationUnit cu = new StringBasedJdtCompilationUnit(packageName, fileName, src);
    boolean reloadRequired = m_nameEnv.addOverrideCompilationUnit(cu);

    String fqn = getFqn(packageName, cu);
    m_performanceCache.remove(createTypeKey(fqn));// clear cache info for this element
    if (!reloadRequired) {
      // if not used in name-env: also check in compiler
      reloadRequired = m_compiler.lookupEnvironment.getCachedType(CharOperation.splitOn('.', fqn.toCharArray())) != null;
    }

    // ensure the package of the new override CU exists. It may be in the lookupEnv cache as 'notExisting' from a call before where it really did not exist.
    m_compiler.lookupEnvironment.createPackage(CharOperation.splitOn('.', packageName.toCharArray()));

    return reloadRequired;
  }

  protected static String getFqn(String packageName, ICompilationUnit cu) {
    StringBuilder fqnBuilder = new StringBuilder();
    if (StringUtils.isNotBlank(packageName)) {
      fqnBuilder.append(packageName);
      fqnBuilder.append('.');
    }
    fqnBuilder.append(cu.getMainTypeName());

    return fqnBuilder.toString();
  }

  public int nextHashCode() {
    return m_hashSeq.getAndIncrement();
  }

  /**
   * @param cu
   * @return the source of the compilation unit or null. The source is only available if the compilation unit is one of
   *         the following
   *         <ul>
   *         <li>source in workspace</li>
   *         <li>class in jar and source in same jar</li>
   *         <li>class in jar and source attachment to jar is defined</li>
   *         </ul>
   */
  public ISourceRange getSource(CompilationUnitSpi cu, int start, int end) {
    if (cu instanceof DeclarationCompilationUnitWithJdt) {
      org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit = m_compiler.getSource(((DeclarationCompilationUnitWithJdt) cu).getInternalCompilationUnitDeclaration());
      if (sourceUnit != null) {
        return new SourceRangeWithJdt(sourceUnit, start, end);
      }
    }
    return ISourceRange.NO_SOURCE;
  }

  public Map<Object, Object> getPerformanceCache() {
    return m_performanceCache;
  }

  @Override
  public List<ClasspathSpi> getClasspath() {
    List<ClasspathSpi> list = new ArrayList<>(m_classpaths.size());
    for (ClasspathEntry cp : m_classpaths) {
      list.add(new ClasspathWithJdt(cp));
    }
    return list;
  }

  public synchronized VoidTypeWithJdt createVoidType() {
    Object key = VoidTypeWithJdt.class;
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new VoidTypeWithJdt(this);
      m_compilerCache.put(key, elem);
    }
    return (VoidTypeWithJdt) elem;
  }

  public synchronized WildcardOnlyTypeWithJdt createWildcardOnlyType() {
    Object key = WildcardOnlyTypeWithJdt.class;
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new WildcardOnlyTypeWithJdt(this);
      m_compilerCache.put(key, elem);
    }
    return (WildcardOnlyTypeWithJdt) elem;
  }

  public synchronized BindingAnnotationWithJdt createBindingAnnotation(AnnotatableSpi owner, AnnotationBinding binding) {
    SameCompositeObject key = new SameCompositeObject(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new BindingAnnotationWithJdt(this, owner, binding);
      m_compilerCache.put(key, elem);
    }
    return (BindingAnnotationWithJdt) elem;
  }

  public synchronized BindingAnnotationElementWithJdt createBindingAnnotationValue(BindingAnnotationWithJdt owner, ElementValuePair bindingPair, boolean syntheticDefaultValue) {
    SameCompositeObject key = new SameCompositeObject(owner, bindingPair);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new BindingAnnotationElementWithJdt(this, owner, bindingPair, syntheticDefaultValue);
      m_compilerCache.put(key, elem);
    }
    return (BindingAnnotationElementWithJdt) elem;
  }

  public synchronized BindingArrayTypeWithJdt createBindingArrayType(ArrayBinding binding, boolean isWildcard) {
    SameCompositeObject key = new SameCompositeObject(binding, isWildcard);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new BindingArrayTypeWithJdt(this, binding, isWildcard);
      m_compilerCache.put(key, elem);
    }
    return (BindingArrayTypeWithJdt) elem;
  }

  public synchronized BindingBaseTypeWithJdt createBindingBaseType(BaseTypeBinding binding) {
    SameCompositeObject key = new SameCompositeObject(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new BindingBaseTypeWithJdt(this, binding);
      m_compilerCache.put(key, elem);
    }
    return (BindingBaseTypeWithJdt) elem;
  }

  public synchronized BindingFieldWithJdt createBindingField(AbstractTypeWithJdt declaringType, FieldBinding binding) {
    SameCompositeObject key = new SameCompositeObject(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new BindingFieldWithJdt(this, declaringType, binding);
      m_compilerCache.put(key, elem);
    }
    return (BindingFieldWithJdt) elem;
  }

  public synchronized BindingMethodWithJdt createBindingMethod(BindingTypeWithJdt declaringType, MethodBinding binding) {
    SameCompositeObject key = new SameCompositeObject(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new BindingMethodWithJdt(this, declaringType, binding);
      m_compilerCache.put(key, elem);
    }
    return (BindingMethodWithJdt) elem;
  }

  public synchronized BindingMethodParameterWithJdt createBindingMethodParameter(BindingMethodWithJdt declaringMethod, TypeBinding binding, char[] name, int index) {
    SameCompositeObject key = new SameCompositeObject(BindingMethodParameterWithJdt.class, declaringMethod, binding, index);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new BindingMethodParameterWithJdt(this, declaringMethod, binding, name, index);
      m_compilerCache.put(key, elem);
    }
    return (BindingMethodParameterWithJdt) elem;
  }

  public synchronized BindingTypeWithJdt createBindingType(ReferenceBinding binding, BindingTypeWithJdt declaringType, boolean isWildcard) {
    SameCompositeObject key = new SameCompositeObject(binding, isWildcard);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new BindingTypeWithJdt(this, binding, declaringType, isWildcard);
      m_compilerCache.put(key, elem);
    }
    return (BindingTypeWithJdt) elem;
  }

  public synchronized BindingTypeParameterWithJdt createBindingTypeParameter(AbstractMemberWithJdt<?> declaringMember, TypeVariableBinding binding, int index) {
    SameCompositeObject key = new SameCompositeObject(declaringMember, binding, index);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new BindingTypeParameterWithJdt(this, declaringMember, binding, index);
      m_compilerCache.put(key, elem);
    }
    return (BindingTypeParameterWithJdt) elem;
  }

  public synchronized DeclarationAnnotationWithJdt createDeclarationAnnotation(AnnotatableSpi owner, org.eclipse.jdt.internal.compiler.ast.Annotation astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new DeclarationAnnotationWithJdt(this, owner, astNode);
      m_compilerCache.put(key, elem);
    }
    return (DeclarationAnnotationWithJdt) elem;
  }

  public synchronized DeclarationAnnotationElementWithJdt createDeclarationAnnotationValue(DeclarationAnnotationWithJdt declaringAnnotation, MemberValuePair astNode, boolean syntheticDefaultValue) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new DeclarationAnnotationElementWithJdt(this, declaringAnnotation, astNode, syntheticDefaultValue);
      m_compilerCache.put(key, elem);
    }
    return (DeclarationAnnotationElementWithJdt) elem;
  }

  public synchronized DeclarationCompilationUnitWithJdt createDeclarationCompilationUnit(CompilationUnitDeclaration astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new DeclarationCompilationUnitWithJdt(this, astNode);
      m_compilerCache.put(key, elem);
    }
    return (DeclarationCompilationUnitWithJdt) elem;
  }

  public synchronized DeclarationFieldWithJdt createDeclarationField(DeclarationTypeWithJdt declaringType, FieldDeclaration astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new DeclarationFieldWithJdt(this, declaringType, astNode);
      m_compilerCache.put(key, elem);
    }
    return (DeclarationFieldWithJdt) elem;
  }

  public synchronized DeclarationImportWithJdt createDeclarationImport(DeclarationCompilationUnitWithJdt owner, ImportReference astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new DeclarationImportWithJdt(this, owner, astNode);
      m_compilerCache.put(key, elem);
    }
    return (DeclarationImportWithJdt) elem;
  }

  public synchronized DeclarationMethodWithJdt createDeclarationMethod(DeclarationTypeWithJdt declaringType, AbstractMethodDeclaration astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new DeclarationMethodWithJdt(this, declaringType, astNode);
      m_compilerCache.put(key, elem);
    }
    return (DeclarationMethodWithJdt) elem;
  }

  public synchronized DeclarationMethodParameterWithJdt createDeclarationMethodParameter(DeclarationMethodWithJdt declaringMethod, Argument astNode, int index) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new DeclarationMethodParameterWithJdt(this, declaringMethod, astNode, index);
      m_compilerCache.put(key, elem);
    }
    return (DeclarationMethodParameterWithJdt) elem;
  }

  public synchronized DeclarationTypeWithJdt createDeclarationType(CompilationUnitSpi cu, DeclarationTypeWithJdt declaringType, TypeDeclaration astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new DeclarationTypeWithJdt(this, cu, declaringType, astNode);
      m_compilerCache.put(key, elem);
    }
    return (DeclarationTypeWithJdt) elem;
  }

  public synchronized DeclarationTypeParameterWithJdt createDeclarationTypeParameter(AbstractMemberWithJdt<?> declaringMember, org.eclipse.jdt.internal.compiler.ast.TypeParameter astNode, int index) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new DeclarationTypeParameterWithJdt(this, declaringMember, astNode, index);
      m_compilerCache.put(key, elem);
    }
    return (DeclarationTypeParameterWithJdt) elem;
  }

  public synchronized PackageWithJdt createPackage(String name) {
    CompositeObject key = new CompositeObject(PackageWithJdt.class, name);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new PackageWithJdt(this, name);
      m_compilerCache.put(key, elem);
    }
    return (PackageWithJdt) elem;
  }

  public PackageWithJdt createDefaultPackage() {
    return createPackage(null);
  }

  public synchronized SyntheticCompilationUnitWithJdt createSyntheticCompilationUnit(BindingTypeWithJdt mainType) {
    SameCompositeObject key = new SameCompositeObject(SyntheticCompilationUnitWithJdt.class, mainType);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      elem = new SyntheticCompilationUnitWithJdt(this, mainType);
      m_compilerCache.put(key, elem);
    }
    return (SyntheticCompilationUnitWithJdt) elem;
  }
}
