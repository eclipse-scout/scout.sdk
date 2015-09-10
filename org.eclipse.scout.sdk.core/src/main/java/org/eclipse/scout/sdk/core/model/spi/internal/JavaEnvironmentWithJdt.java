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
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
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
import org.eclipse.scout.sdk.core.model.api.internal.JavaEnvironmentImplementor;
import org.eclipse.scout.sdk.core.model.spi.AnnotatableSpi;
import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaEnvironmentSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.CompositeObject;

/**
 * <h3>{@link JavaEnvironmentWithJdt}</h3>
 *
 * @author imo
 * @since 5.1.0
 */
public class JavaEnvironmentWithJdt implements JavaEnvironmentSpi {
  private final Classpath[] m_classpaths;
  private final AstCompiler m_compiler;
  private final WorkspaceFileSystem m_nameEnv;
  private final Map<Object, JavaElementSpi> m_compilerCache = new HashMap<>();
  private final Map<Object, Object> m_performanceCache = new HashMap<>();
  private final AtomicInteger m_hashSeq = new AtomicInteger();

  private IJavaEnvironment m_api;

  public JavaEnvironmentWithJdt(Classpath[] classpaths) {
    m_classpaths = classpaths;
    m_nameEnv = new WorkspaceFileSystem(m_classpaths);
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
  public TypeSpi findType(String fqn) {
    Object key = new CompositeObject(TypeSpi.class, fqn);
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
  public JavaEnvironmentSpi reload() {
    JavaEnvironmentWithJdt oldEnv = this;
    Collection<ClasspathSpi> oldClasspath = oldEnv.getClasspath();
    Collection<FileSystem.Classpath> newClasspath = new ArrayList<>(oldClasspath.size());
    for (ClasspathSpi cp : oldClasspath) {
      newClasspath.add(WorkspaceFileSystem.createClasspath(new File(cp.getPath()), cp.isSource()));
    }

    JavaEnvironmentWithJdt newEnv = new JavaEnvironmentWithJdt(newClasspath.toArray(new FileSystem.Classpath[0]));
    for (org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu : oldEnv.m_nameEnv.getOverrideCompilationUnits()) {
      newEnv.m_nameEnv.addOverrideCompilationUnit(cu);
    }

    //reload spi of all objects that origin from this environment
    newEnv.m_api = oldEnv.m_api;
    newEnv.m_api.internalSetSpi(newEnv);
    JavaElementSpi[] oldSpiArray = oldEnv.m_compilerCache.values().toArray(new JavaElementSpi[0]);
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
      apiArray[i].internalSetSpi(newSpiArray[i]);
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
  public void registerCompilationUnitOverride(String packageName, String fileName, StringBuilder buf) {
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
   *         <li>class in jar and source attachement to jar is defined</li>
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
    ArrayList<ClasspathSpi> list = new ArrayList<>(m_classpaths.length);
    for (Classpath cp : m_classpaths) {
      list.add(new ClasspathWithJdt(cp));
    }
    return list;
  }

  public VoidTypeWithJdt createVoidType() {
    Object key = VoidTypeWithJdt.class;
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new VoidTypeWithJdt(this));
    }
    return (VoidTypeWithJdt) elem;
  }

  public WildcardOnlyTypeWithJdt createWildcardOnlyType() {
    Object key = WildcardOnlyTypeWithJdt.class;
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new WildcardOnlyTypeWithJdt(this));
    }
    return (WildcardOnlyTypeWithJdt) elem;
  }

  public BindingAnnotationWithJdt createBindingAnnotation(AnnotatableSpi owner, AnnotationBinding binding) {
    SameReferences key = new SameReferences(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingAnnotationWithJdt(this, owner, binding));
    }
    return (BindingAnnotationWithJdt) elem;
  }

  public BindingAnnotationValueWithJdt createBindingAnnotationValue(BindingAnnotationWithJdt owner, ElementValuePair bindingPair, boolean syntheticDefaultValue) {
    SameReferences key = new SameReferences(owner, bindingPair);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingAnnotationValueWithJdt(this, owner, bindingPair, syntheticDefaultValue));
    }
    return (BindingAnnotationValueWithJdt) elem;
  }

  public BindingArrayTypeWithJdt createBindingArrayType(ArrayBinding binding, boolean isWildcard) {
    SameReferences key = new SameReferences(binding, isWildcard);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingArrayTypeWithJdt(this, binding, isWildcard));
    }
    return (BindingArrayTypeWithJdt) elem;
  }

  public BindingBaseTypeWithJdt createBindingBaseType(BaseTypeBinding binding) {
    SameReferences key = new SameReferences(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingBaseTypeWithJdt(this, binding));
    }
    return (BindingBaseTypeWithJdt) elem;
  }

  public BindingFieldWithJdt createBindingField(AbstractTypeWithJdt declaringType, FieldBinding binding) {
    SameReferences key = new SameReferences(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingFieldWithJdt(this, declaringType, binding));
    }
    return (BindingFieldWithJdt) elem;
  }

  public BindingMethodWithJdt createBindingMethod(BindingTypeWithJdt declaringType, MethodBinding binding) {
    SameReferences key = new SameReferences(binding);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingMethodWithJdt(this, declaringType, binding));
    }
    return (BindingMethodWithJdt) elem;
  }

  public BindingMethodParameterWithJdt createBindingMethodParameter(BindingMethodWithJdt declaringMethod, TypeBinding binding, char[] name, int index) {
    SameReferences key = new SameReferences(declaringMethod, binding, name, index);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingMethodParameterWithJdt(this, declaringMethod, binding, name, index));
    }
    return (BindingMethodParameterWithJdt) elem;
  }

  public BindingTypeWithJdt createBindingType(ReferenceBinding binding, BindingTypeWithJdt declaringType, boolean isWildcard) {
    SameReferences key = new SameReferences(binding, isWildcard);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingTypeWithJdt(this, binding, declaringType, isWildcard));
    }
    return (BindingTypeWithJdt) elem;
  }

  public BindingTypeParameterWithJdt createBindingTypeParameter(AbstractMemberWithJdt<?> declaringMember, TypeVariableBinding binding, int index) {
    SameReferences key = new SameReferences(declaringMember, binding, index);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new BindingTypeParameterWithJdt(this, declaringMember, binding, index));
    }
    return (BindingTypeParameterWithJdt) elem;
  }

  public DeclarationAnnotationWithJdt createDeclarationAnnotation(AnnotatableSpi owner, org.eclipse.jdt.internal.compiler.ast.Annotation astNode) {
    SameReferences key = new SameReferences(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationAnnotationWithJdt(this, owner, astNode));
    }
    return (DeclarationAnnotationWithJdt) elem;
  }

  public DeclarationAnnotationValueWithJdt createDeclarationAnnotationValue(DeclarationAnnotationWithJdt declaringAnnotation, MemberValuePair astNode, boolean syntheticDefaultValue) {
    SameReferences key = new SameReferences(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationAnnotationValueWithJdt(this, declaringAnnotation, astNode, syntheticDefaultValue));
    }
    return (DeclarationAnnotationValueWithJdt) elem;
  }

  public DeclarationCompilationUnitWithJdt createDeclarationCompilationUnit(CompilationUnitDeclaration astNode) {
    SameReferences key = new SameReferences(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationCompilationUnitWithJdt(this, astNode));
    }
    return (DeclarationCompilationUnitWithJdt) elem;
  }

  public DeclarationFieldWithJdt createDeclarationField(DeclarationTypeWithJdt declaringType, FieldDeclaration astNode) {
    SameReferences key = new SameReferences(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationFieldWithJdt(this, declaringType, astNode));
    }
    return (DeclarationFieldWithJdt) elem;
  }

  public DeclarationImportWithJdt createDeclarationImport(DeclarationCompilationUnitWithJdt owner, ImportReference astNode) {
    SameReferences key = new SameReferences(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationImportWithJdt(this, owner, astNode));
    }
    return (DeclarationImportWithJdt) elem;
  }

  public DeclarationMethodWithJdt createDeclarationMethod(DeclarationTypeWithJdt declaringType, AbstractMethodDeclaration astNode) {
    SameReferences key = new SameReferences(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationMethodWithJdt(this, declaringType, astNode));
    }
    return (DeclarationMethodWithJdt) elem;
  }

  public DeclarationMethodParameterWithJdt createDeclarationMethodParameter(DeclarationMethodWithJdt declaringMethod, Argument astNode, int index) {
    SameReferences key = new SameReferences(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationMethodParameterWithJdt(this, declaringMethod, astNode, index));
    }
    return (DeclarationMethodParameterWithJdt) elem;
  }

  public DeclarationTypeWithJdt createDeclarationType(CompilationUnitSpi cu, DeclarationTypeWithJdt declaringType, TypeDeclaration astNode) {
    SameReferences key = new SameReferences(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationTypeWithJdt(this, cu, declaringType, astNode));
    }
    return (DeclarationTypeWithJdt) elem;
  }

  public DeclarationTypeParameterWithJdt createDeclarationTypeParameter(AbstractMemberWithJdt<?> declaringMember, org.eclipse.jdt.internal.compiler.ast.TypeParameter astNode, int index) {
    SameReferences key = new SameReferences(astNode);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new DeclarationTypeParameterWithJdt(this, declaringMember, astNode, index));
    }
    return (DeclarationTypeParameterWithJdt) elem;
  }

  public PackageWithJdt createPackage(String name) {
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

  public SyntheticCompilationUnitWithJdt createSyntheticCompilationUnit(BindingTypeWithJdt mainType) {
    SameReferences key = new SameReferences(SyntheticCompilationUnitWithJdt.class, mainType);
    JavaElementSpi elem = m_compilerCache.get(key);
    if (elem == null) {
      m_compilerCache.put(key, elem = new SyntheticCompilationUnitWithJdt(this, mainType));
    }
    return (SyntheticCompilationUnitWithJdt) elem;
  }
}
