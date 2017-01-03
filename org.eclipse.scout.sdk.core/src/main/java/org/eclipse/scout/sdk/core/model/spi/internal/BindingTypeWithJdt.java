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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MissingTypeException;
import org.eclipse.scout.sdk.core.model.api.internal.TypeImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class BindingTypeWithJdt extends AbstractTypeWithJdt {
  private final ReferenceBinding m_binding;
  private final boolean m_isWildcard;
  private PackageSpi m_package;
  private BindingTypeWithJdt m_declaringType;
  private TypeSpi m_superClass;
  private String m_elementName;
  private String m_name;
  private List<TypeSpi> m_memberTypes;
  private List<TypeSpi> m_superInterfaces;
  private List<TypeParameterSpi> m_typeParameters;
  private List<TypeSpi> m_typeArguments;
  private TypeSpi m_originalType;
  private int m_flags;
  private List<BindingAnnotationWithJdt> m_annotations;
  private List<MethodSpi> m_methods;
  private List<FieldSpi> m_fields;
  private AtomicReference<SourceTypeBinding> m_sourceTypeBindingRef;
  private CompilationUnitSpi m_unit;
  private ISourceRange m_source;
  private ISourceRange m_javaDocSource;
  private ISourceRange m_staticInitSource;

  BindingTypeWithJdt(JavaEnvironmentWithJdt env, ReferenceBinding binding, BindingTypeWithJdt declaringType, boolean isWildcard) {
    super(env);
    if (binding == null || binding instanceof MissingTypeBinding || binding instanceof ProblemReferenceBinding) {
      // type not found -> parsing error. do not silently continue
      throw new MissingTypeException("" + binding);
    }
    m_binding = Validate.notNull(binding);
    m_isWildcard = isWildcard;
    m_declaringType = declaringType;
    m_flags = -1; // mark as not initialized yet
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    return newEnv.findType(getName());
  }

  @Override
  protected IType internalCreateApi() {
    return new TypeImplementor(this);
  }

  /**
   * if source is attached, this returns the SourceTypeBinding containing the {@link ClassScope}
   */
  public ClassScope getInternalClassScope() {
    SourceTypeBinding stb = getSourceTypeBinding();
    return stb != null ? stb.scope : null;
  }

  @Override
  public ReferenceBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public int getArrayDimension() {
    return 0;
  }

  @Override
  public TypeSpi getLeafComponentType() {
    return null;
  }

  @Override
  public CompilationUnitSpi getCompilationUnit() {
    if (m_unit == null) {
      ClassScope scope = getInternalClassScope();
      if (scope != null) {
        CompilationUnitScope cuScope = scope.compilationUnitScope();
        if (cuScope != null) {
          m_unit = m_env.createDeclarationCompilationUnit(cuScope.referenceContext);
        }
      }
      //binary
      if (m_unit == null) {
        TypeSpi parent = getDeclaringType();
        m_unit = parent != null ? parent.getCompilationUnit() : m_env.createSyntheticCompilationUnit(this);
      }
    }
    return m_unit;
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public PackageSpi getPackage() {
    if (m_package == null) {
      char[] qualifiedPackageName = m_binding.qualifiedPackageName();
      if (qualifiedPackageName == null || qualifiedPackageName.length < 1) {
        m_package = m_env.createDefaultPackage();
      }
      else {
        m_package = m_env.createPackage(new String(qualifiedPackageName));
      }
    }
    return m_package;
  }

  @Override
  public String getName() {
    if (m_name == null) {
      if (m_binding.compoundName == null) {
        // for type variable bindings
        m_name = new String(m_binding.sourceName);
      }
      else {
        m_name = CharOperation.toString(m_binding.compoundName);
      }
    }
    return m_name;
  }

  @Override
  public String getElementName() {
    if (m_elementName == null) {
      m_elementName = new String(m_binding.sourceName);
    }
    return m_elementName;
  }

  @Override
  public List<FieldSpi> getFields() {
    if (m_fields != null) {
      return m_fields;
    }

    getSourceTypeBinding();
    FieldBinding[] fields = m_binding.fields();
    if (fields == null || fields.length < 1) {
      m_fields = Collections.emptyList();
    }
    else {
      fields = Arrays.copyOf(fields, fields.length);
      Arrays.sort(fields, new SourcePositionComparator());
      List<FieldSpi> result = new ArrayList<>(fields.length);
      for (FieldBinding fd : fields) {
        if (fd.isSynthetic()) {
          continue;
        }
        result.add(m_env.createBindingField(this, fd));
      }
      m_fields = Collections.unmodifiableList(result);
    }
    return m_fields;
  }

  @Override
  public List<MethodSpi> getMethods() {
    if (m_methods != null) {
      return m_methods;
    }

    getSourceTypeBinding();
    MethodBinding[] methods = m_binding.methods();
    if (methods == null || methods.length < 1) {
      m_methods = Collections.emptyList();
    }
    else {
      methods = Arrays.copyOf(methods, methods.length);
      Arrays.sort(methods, new SourcePositionComparator());
      List<MethodSpi> result = new ArrayList<>(methods.length);
      for (MethodBinding a : methods) {
        if ((a.modifiers & ExtraCompilerModifiers.AccIsDefaultConstructor) != 0) {
          continue;
        }
        if (a.isBridge() || a.isSynthetic() || a.isDefaultAbstract()) {
          continue;
        }
        //bug in jdt: default constructors are not reported as 'isSynthetic'
        if (a.isConstructor() && (a.sourceMethod() != null && a.sourceMethod().bodyStart == 0)) {
          continue;
        }
        result.add(m_env.createBindingMethod(this, a));
      }
      m_methods = Collections.unmodifiableList(result);
    }
    return m_methods;
  }

  @Override
  public List<TypeSpi> getTypes() {
    if (m_memberTypes != null) {
      return m_memberTypes;
    }

    getSourceTypeBinding();
    ReferenceBinding[] memberTypes = m_binding.memberTypes();
    if (memberTypes == null || memberTypes.length < 1) {
      m_memberTypes = Collections.emptyList();
    }
    else {
      memberTypes = Arrays.copyOf(memberTypes, memberTypes.length);
      Arrays.sort(memberTypes, new SourcePositionComparator());
      List<TypeSpi> result = new ArrayList<>(memberTypes.length);
      for (ReferenceBinding d : memberTypes) {
        TypeSpi t = SpiWithJdtUtils.bindingToType(m_env, d, this);
        result.add(t);
      }
      m_memberTypes = Collections.unmodifiableList(result);
    }
    return m_memberTypes;
  }

  @Override
  public BindingTypeWithJdt getDeclaringType() {
    if (m_declaringType == null) {
      ReferenceBinding enclosingType = m_binding.enclosingType();
      if (enclosingType != null) {
        m_declaringType = new BindingTypeWithJdt(m_env, enclosingType, null, false);
      }
    }
    return m_declaringType;
  }

  @Override
  public TypeSpi getSuperClass() {
    if (m_superClass == null) {
      getSourceTypeBinding();
      ReferenceBinding superclass = m_binding.superclass();
      if (superclass != null) {
        m_superClass = SpiWithJdtUtils.bindingToType(m_env, superclass);
      }
    }
    return m_superClass;
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    if (m_superInterfaces != null) {
      return m_superInterfaces;
    }

    getSourceTypeBinding();
    ReferenceBinding[] superInterfaces = m_binding.superInterfaces();
    if (superInterfaces == null || superInterfaces.length < 1) {
      m_superInterfaces = Collections.emptyList();
    }
    else {
      List<TypeSpi> result = new ArrayList<>(superInterfaces.length);
      for (ReferenceBinding b : superInterfaces) {
        TypeSpi t = SpiWithJdtUtils.bindingToType(m_env, b);
        if (t != null) {
          result.add(t);
        }
      }
      m_superInterfaces = Collections.unmodifiableList(result);
    }
    return m_superInterfaces;
  }

  @Override
  public boolean isWildcardType() {
    return m_isWildcard;
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
    if (m_typeArguments != null) {
      return m_typeArguments;
    }

    getSourceTypeBinding();
    if (m_binding instanceof ParameterizedTypeBinding) {
      ParameterizedTypeBinding ptb = (ParameterizedTypeBinding) m_binding;
      TypeBinding[] arguments = ptb.arguments;
      if (arguments != null && arguments.length > 0) {
        List<TypeSpi> result = new ArrayList<>(arguments.length);
        for (TypeBinding b : arguments) {
          TypeSpi type = SpiWithJdtUtils.bindingToType(m_env, b);
          if (type != null) {
            result.add(type);
          }
        }
        m_typeArguments = Collections.unmodifiableList(result);
      }
    }

    if (m_typeArguments == null) {
      m_typeArguments = Collections.emptyList();
    }
    return m_typeArguments;
  }

  protected TypeVariableBinding[] getTypeVariables() {
    //ask this or the actualType since we do not distinguish between the virtual parameterized type with arguments and the effective parameterized type with parameters
    ReferenceBinding refType = m_binding;
    if (m_binding.actualType() != null) {
      refType = m_binding.actualType();
    }
    return refType.typeVariables();
  }

  @Override
  public boolean hasTypeParameters() {
    getSourceTypeBinding();
    TypeVariableBinding[] typeVariables = getTypeVariables();
    return typeVariables != null && typeVariables.length > 0;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    if (m_typeParameters != null) {
      return m_typeParameters;
    }

    getSourceTypeBinding();
    TypeVariableBinding[] typeParams = getTypeVariables();
    if (typeParams != null && typeParams.length > 0) {
      List<TypeParameterSpi> result = new ArrayList<>(typeParams.length);
      int index = 0;
      for (TypeVariableBinding param : typeParams) {
        result.add(m_env.createBindingTypeParameter(this, param, index));
        index++;
      }
      m_typeParameters = Collections.unmodifiableList(result);
    }
    else {
      m_typeParameters = Collections.emptyList();
    }
    return m_typeParameters;
  }

  @Override
  public TypeSpi getOriginalType() {
    if (m_originalType == null) {
      ReferenceBinding ref = m_binding.actualType();
      if (ref == null || ref == m_binding) {
        m_originalType = this;
      }
      else {
        m_originalType = SpiWithJdtUtils.bindingToType(m_env, ref);
      }
    }
    return m_originalType;
  }

  @Override
  public List<BindingAnnotationWithJdt> getAnnotations() {
    if (m_annotations != null) {
      return m_annotations;
    }

    getSourceTypeBinding();
    ReferenceBinding refType = m_binding;
    if (m_binding.actualType() != null) {
      refType = m_binding.actualType();
    }
    m_annotations = SpiWithJdtUtils.createBindingAnnotations(m_env, this, refType.getAnnotations());
    return m_annotations;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithJdtUtils.getTypeFlags(m_binding.modifiers, null, SpiWithJdtUtils.hasDeprecatedAnnotation(m_binding.getAnnotations()));
    }
    return m_flags;
  }

  /**
   * if the type is a source type then initialize all bindings and return it, otherwise return null
   */
  protected SourceTypeBinding getSourceTypeBinding() {
    if (m_sourceTypeBindingRef == null) {
      SourceTypeBinding stb = null;
      if (m_binding instanceof SourceTypeBinding) {
        // source bindings are not automatically fully built. lazily complete all bindings when needed.
        stb = (SourceTypeBinding) m_binding;
        CompilationUnitScope scope = stb.scope.compilationUnitScope();
        if (scope != null) {
          scope.environment.completeTypeBindings(scope.referenceContext, true);
        }
      }
      m_sourceTypeBindingRef = new AtomicReference<>(stb);
    }
    return m_sourceTypeBindingRef.get();
  }

  @Override
  public boolean isAnonymous() {
    return m_binding.compoundName == null || m_binding.compoundName.length < 1;
  }

  @Override
  public ISourceRange getSource() {
    if (m_source == null) {
      if (m_binding instanceof SourceTypeBinding) {
        TypeDeclaration decl = ((SourceTypeBinding) m_binding).scope.referenceContext;
        CompilationUnitSpi cu = getCompilationUnit();
        m_source = m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
      }
      else {
        m_source = ISourceRange.NO_SOURCE;
      }
    }
    return m_source;

  }

  @Override
  public ISourceRange getSourceOfStaticInitializer() {
    if (m_staticInitSource == null) {
      if (m_binding instanceof SourceTypeBinding) {
        TypeDeclaration decl = ((SourceTypeBinding) m_binding).scope.referenceContext;
        for (FieldDeclaration fieldDecl : decl.fields) {
          if (fieldDecl.type == null && fieldDecl.name == null) {
            CompilationUnitSpi cu = getCompilationUnit();
            m_staticInitSource = m_env.getSource(cu, fieldDecl.declarationSourceStart, fieldDecl.declarationSourceEnd);
            break;
          }
        }
      }
      if (m_staticInitSource == null) {
        m_staticInitSource = ISourceRange.NO_SOURCE;
      }
    }
    return m_staticInitSource;
  }

  @Override
  public ISourceRange getJavaDoc() {
    if (m_javaDocSource == null) {
      if (m_binding instanceof SourceTypeBinding) {
        TypeDeclaration decl = ((SourceTypeBinding) m_binding).scope.referenceContext;
        Javadoc doc = decl.javadoc;
        if (doc != null) {
          CompilationUnitSpi cu = getCompilationUnit();
          m_javaDocSource = m_env.getSource(cu, doc.sourceStart, doc.sourceEnd);
        }
      }
      if (m_javaDocSource == null) {
        m_javaDocSource = ISourceRange.NO_SOURCE;
      }
    }
    return m_javaDocSource;
  }

}
