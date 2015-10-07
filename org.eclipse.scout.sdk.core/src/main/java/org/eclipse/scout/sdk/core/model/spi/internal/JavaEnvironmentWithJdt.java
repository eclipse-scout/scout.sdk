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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
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
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.SameCompositeObject;

/**
 * <h3>{@link JavaEnvironmentWithJdt}</h3>
 *
 * @author Ivan Motsch, Matthias Villiger
 * @since 5.1.0
 */
public class JavaEnvironmentWithJdt implements JavaEnvironmentSpi {
  private final ClasspathEntry[] m_classpaths;
  private final AstCompiler m_compiler;
  private final WorkspaceFileSystem m_nameEnv;
  private final Map<Object, JavaElementSpi> m_compilerCache = new HashMap<>();
  private final Map<Object, Object> m_performanceCache = new HashMap<>();
  private final AtomicInteger m_hashSeq = new AtomicInteger();

  private IJavaEnvironment m_api;

  public JavaEnvironmentWithJdt(ClasspathEntry[] classpaths) {
    m_classpaths = classpaths;
    m_nameEnv = new WorkspaceFileSystem(ClasspathEntry.toClassPaths(classpaths));
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

  @Override
  public synchronized TypeSpi findType(String fqn) {
    CompositeObject key = new CompositeObject(TypeSpi.class, fqn);
    Object elem = m_performanceCache.get(key);
    if (elem == null && !m_performanceCache.containsKey(key)) {
      String[] parts = SpiWithJdtUtils.splitToPrimaryType(fqn);
      TypeBinding binding = SpiWithJdtUtils.findTypeBinding(parts[0], m_compiler);
      if (parts.length < 2) {
        // no inner types: directly return answer
        elem = SpiWithJdtUtils.bindingToType(this, binding);
      }
      else {
        elem = SpiWithJdtUtils.bindingToInnerType(this, binding, parts);
      }
      m_performanceCache.put(key, elem);
    }
    return (TypeSpi) elem;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public synchronized JavaEnvironmentSpi reload() {
    JavaEnvironmentWithJdt oldEnv = this;
    Collection<ClasspathSpi> oldClasspath = oldEnv.getClasspath();
    Collection<ClasspathEntry> newClasspath = new ArrayList<>(oldClasspath.size());
    for (ClasspathSpi cp : oldClasspath) {
      ClasspathEntry entry = new ClasspathEntry(WorkspaceFileSystem.createClasspath(new File(cp.getPath()), cp.isSource(), cp.getEncoding()), cp.getEncoding());
      newClasspath.add(entry);
    }

    JavaEnvironmentWithJdt newEnv = new JavaEnvironmentWithJdt(newClasspath.toArray(new ClasspathEntry[newClasspath.size()]));
    for (org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu : oldEnv.m_nameEnv.getOverrideCompilationUnits()) {
      newEnv.m_nameEnv.addOverrideCompilationUnit(cu);
    }

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
      return "Cannot find type " + fqn;
    }
    CompilationUnitSpi cuSpi = typeSpi.getCompilationUnit();
    if (!(cuSpi instanceof DeclarationCompilationUnitWithJdt)) {
      return "Type " + fqn + " is not a source type";
    }
    CompilationUnitDeclaration decl = ((DeclarationCompilationUnitWithJdt) cuSpi).getInternalCompilationUnitDeclaration();
    return m_compiler.getCompileErrors(decl);
  }

  @Override
  public synchronized void registerCompilationUnitOverride(String packageName, String fileName, StringBuilder buf) {
    Validate.notNull(fileName);
    Validate.notNull(buf);

    StringBasedJdtCompilationUnit cu = new StringBasedJdtCompilationUnit(packageName, fileName, buf);
    m_nameEnv.addOverrideCompilationUnit(cu);
    m_performanceCache.clear();
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
      return sourceUnit != null ? new SourceRangeWithJdt(sourceUnit, start, end) : null;
    }
    return null;
  }

  public Map<Object, Object> getPerformanceCache() {
    return m_performanceCache;
  }

  @Override
  public Collection<ClasspathSpi> getClasspath() {
    Collection<ClasspathSpi> list = new ArrayList<>(m_classpaths.length);
    for (ClasspathEntry cp : m_classpaths) {
      list.add(new ClasspathWithJdt(cp));
    }
    return list;
  }

  public synchronized VoidTypeWithJdt createVoidType() {
    Object key = VoidTypeWithJdt.class;
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new VoidTypeWithJdt(this));
    }
    return (VoidTypeWithJdt) elem;
  }

  public synchronized WildcardOnlyTypeWithJdt createWildcardOnlyType() {
    Object key = WildcardOnlyTypeWithJdt.class;
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new WildcardOnlyTypeWithJdt(this));
    }
    return (WildcardOnlyTypeWithJdt) elem;
  }

  public synchronized BindingAnnotationWithJdt createBindingAnnotation(AnnotatableSpi owner, AnnotationBinding binding) {
    SameCompositeObject key = new SameCompositeObject(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingAnnotationWithJdt(this, owner, binding));
    }
    return (BindingAnnotationWithJdt) elem;
  }

  public synchronized BindingAnnotationElementWithJdt createBindingAnnotationValue(BindingAnnotationWithJdt owner, ElementValuePair bindingPair, boolean syntheticDefaultValue) {
    SameCompositeObject key = new SameCompositeObject(owner, bindingPair);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingAnnotationElementWithJdt(this, owner, bindingPair, syntheticDefaultValue));
    }
    return (BindingAnnotationElementWithJdt) elem;
  }

  public synchronized BindingArrayTypeWithJdt createBindingArrayType(ArrayBinding binding, boolean isWildcard) {
    SameCompositeObject key = new SameCompositeObject(binding, isWildcard);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingArrayTypeWithJdt(this, binding, isWildcard));
    }
    return (BindingArrayTypeWithJdt) elem;
  }

  public synchronized BindingBaseTypeWithJdt createBindingBaseType(BaseTypeBinding binding) {
    SameCompositeObject key = new SameCompositeObject(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingBaseTypeWithJdt(this, binding));
    }
    return (BindingBaseTypeWithJdt) elem;
  }

  public synchronized BindingFieldWithJdt createBindingField(AbstractTypeWithJdt declaringType, FieldBinding binding) {
    SameCompositeObject key = new SameCompositeObject(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingFieldWithJdt(this, declaringType, binding));
    }
    return (BindingFieldWithJdt) elem;
  }

  public synchronized BindingMethodWithJdt createBindingMethod(BindingTypeWithJdt declaringType, MethodBinding binding) {
    SameCompositeObject key = new SameCompositeObject(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingMethodWithJdt(this, declaringType, binding));
    }
    return (BindingMethodWithJdt) elem;
  }

  public synchronized BindingMethodParameterWithJdt createBindingMethodParameter(BindingMethodWithJdt declaringMethod, TypeBinding binding, char[] name, int index) {
    SameCompositeObject key = new SameCompositeObject(declaringMethod, binding, name, index);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingMethodParameterWithJdt(this, declaringMethod, binding, name, index));
    }
    return (BindingMethodParameterWithJdt) elem;
  }

  public synchronized BindingTypeWithJdt createBindingType(ReferenceBinding binding, BindingTypeWithJdt declaringType, boolean isWildcard) {
    SameCompositeObject key = new SameCompositeObject(binding, isWildcard);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingTypeWithJdt(this, binding, declaringType, isWildcard));
    }
    return (BindingTypeWithJdt) elem;
  }

  public synchronized BindingTypeParameterWithJdt createBindingTypeParameter(AbstractMemberWithJdt<?> declaringMember, TypeVariableBinding binding, int index) {
    SameCompositeObject key = new SameCompositeObject(declaringMember, binding, index);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingTypeParameterWithJdt(this, declaringMember, binding, index));
    }
    return (BindingTypeParameterWithJdt) elem;
  }

  public synchronized DeclarationAnnotationWithJdt createDeclarationAnnotation(AnnotatableSpi owner, org.eclipse.jdt.internal.compiler.ast.Annotation astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationAnnotationWithJdt(this, owner, astNode));
    }
    return (DeclarationAnnotationWithJdt) elem;
  }

  public synchronized DeclarationAnnotationElementWithJdt createDeclarationAnnotationValue(DeclarationAnnotationWithJdt declaringAnnotation, MemberValuePair astNode, boolean syntheticDefaultValue) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationAnnotationElementWithJdt(this, declaringAnnotation, astNode, syntheticDefaultValue));
    }
    return (DeclarationAnnotationElementWithJdt) elem;
  }

  public synchronized DeclarationCompilationUnitWithJdt createDeclarationCompilationUnit(CompilationUnitDeclaration astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationCompilationUnitWithJdt(this, astNode));
    }
    return (DeclarationCompilationUnitWithJdt) elem;
  }

  public synchronized DeclarationFieldWithJdt createDeclarationField(DeclarationTypeWithJdt declaringType, FieldDeclaration astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationFieldWithJdt(this, declaringType, astNode));
    }
    return (DeclarationFieldWithJdt) elem;
  }

  public synchronized DeclarationImportWithJdt createDeclarationImport(DeclarationCompilationUnitWithJdt owner, ImportReference astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationImportWithJdt(this, owner, astNode));
    }
    return (DeclarationImportWithJdt) elem;
  }

  public synchronized DeclarationMethodWithJdt createDeclarationMethod(DeclarationTypeWithJdt declaringType, AbstractMethodDeclaration astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationMethodWithJdt(this, declaringType, astNode));
    }
    return (DeclarationMethodWithJdt) elem;
  }

  public synchronized DeclarationMethodParameterWithJdt createDeclarationMethodParameter(DeclarationMethodWithJdt declaringMethod, Argument astNode, int index) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationMethodParameterWithJdt(this, declaringMethod, astNode, index));
    }
    return (DeclarationMethodParameterWithJdt) elem;
  }

  public synchronized DeclarationTypeWithJdt createDeclarationType(CompilationUnitSpi cu, DeclarationTypeWithJdt declaringType, TypeDeclaration astNode) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationTypeWithJdt(this, cu, declaringType, astNode));
    }
    return (DeclarationTypeWithJdt) elem;
  }

  public synchronized DeclarationTypeParameterWithJdt createDeclarationTypeParameter(AbstractMemberWithJdt<?> declaringMember, org.eclipse.jdt.internal.compiler.ast.TypeParameter astNode, int index) {
    SameCompositeObject key = new SameCompositeObject(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationTypeParameterWithJdt(this, declaringMember, astNode, index));
    }
    return (DeclarationTypeParameterWithJdt) elem;
  }

  public synchronized PackageWithJdt createPackage(String name) {
    CompositeObject key = new CompositeObject(PackageWithJdt.class, name);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new PackageWithJdt(this, name));
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
      m_compilerCache.put(key, elem = new SyntheticCompilationUnitWithJdt(this, mainType));
    }
    return (SyntheticCompilationUnitWithJdt) elem;
  }
}
