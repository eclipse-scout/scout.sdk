/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.bindingToType;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.bindingsToTypes;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.createBindingAnnotations;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.createSourceRange;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.createTypeParameters;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.getTypeFlags;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.hasDeprecatedAnnotation;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.nvl;
import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.qualifiedNameOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
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
import org.eclipse.scout.sdk.core.model.ecj.SourcePositionComparators.FieldBindingComparator;
import org.eclipse.scout.sdk.core.model.ecj.SourcePositionComparators.MethodBindingComparator;
import org.eclipse.scout.sdk.core.model.ecj.SourcePositionComparators.TypeBindingComparator;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.PackageSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 *
 */
public class BindingTypeWithEcj extends AbstractTypeWithEcj {
  private final ReferenceBinding m_binding;
  private final boolean m_isWildcard;
  private final FinalValue<PackageSpi> m_package;
  private final FinalValue<TypeSpi> m_declaringType;
  private final FinalValue<TypeSpi> m_superClass;
  private final FinalValue<String> m_elementName;
  private final FinalValue<String> m_name;
  private final FinalValue<List<TypeSpi>> m_memberTypes;
  private final FinalValue<List<TypeSpi>> m_superInterfaces;
  private final FinalValue<List<TypeParameterSpi>> m_typeParameters;
  private final FinalValue<List<TypeSpi>> m_typeArguments;
  private final FinalValue<List<BindingAnnotationWithEcj>> m_annotations;
  private final FinalValue<List<MethodSpi>> m_methods;
  private final FinalValue<List<FieldSpi>> m_fields;
  private final FinalValue<SourceTypeBinding> m_sourceTypeBindingRef;
  private final FinalValue<CompilationUnitSpi> m_unit;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<ISourceRange> m_javaDocSource;
  private final FinalValue<ISourceRange> m_staticInitSource;
  private final Supplier<? extends ReferenceBinding> m_newElementLookupStrategy;
  private int m_flags;

  protected BindingTypeWithEcj(AbstractJavaEnvironment env, ReferenceBinding binding, TypeSpi declaringType, boolean isWildcard, Supplier<? extends ReferenceBinding> newElementLookupStrategy) {
    super(env);
    if (binding == null || binding instanceof MissingTypeBinding || binding instanceof ProblemReferenceBinding) {
      // type not found -> parsing error. do not silently continue
      throw new MissingTypeException(String.valueOf(binding));
    }
    m_binding = Ensure.notNull(binding);
    m_newElementLookupStrategy = Ensure.notNull(newElementLookupStrategy);
    m_isWildcard = isWildcard;
    m_declaringType = new FinalValue<>();
    if (declaringType != null) {
      m_declaringType.set(declaringType);
    }
    m_flags = -1; // mark as not initialized yet
    m_package = new FinalValue<>();
    m_superClass = new FinalValue<>();
    m_elementName = new FinalValue<>();
    m_name = new FinalValue<>();
    m_memberTypes = new FinalValue<>();
    m_superInterfaces = new FinalValue<>();
    m_typeParameters = new FinalValue<>();
    m_typeArguments = new FinalValue<>();
    m_annotations = new FinalValue<>();
    m_methods = new FinalValue<>();
    m_fields = new FinalValue<>();
    m_sourceTypeBindingRef = new FinalValue<>();
    m_unit = new FinalValue<>();
    m_source = new FinalValue<>();
    m_javaDocSource = new FinalValue<>();
    m_staticInitSource = new FinalValue<>();
  }

  static PackageSpi packageOf(TypeBinding binding, JavaEnvironmentWithEcj env) {
    var qualifiedPackageName = binding.qualifiedPackageName();
    if (qualifiedPackageName == null || qualifiedPackageName.length < 1) {
      return env.createDefaultPackage();
    }
    return env.createPackage(new String(qualifiedPackageName));
  }

  @Override
  public TypeSpi internalFindNewElement() {
    var same = bindingToType(javaEnvWithEcj(), m_newElementLookupStrategy.get(), getDeclaringType(), m_isWildcard, m_newElementLookupStrategy);
    if (same != null) {
      return same;
    }
    if (m_binding instanceof ParameterizedTypeBinding) {
      // the original used arguments. but the same cannot be found anymore
      return null;
    }
    return getJavaEnvironment().findType(getName());
  }

  @Override
  protected IType internalCreateApi() {
    return new TypeImplementor(this);
  }

  /**
   * if source is attached, this returns the SourceTypeBinding containing the {@link ClassScope}
   */
  public ClassScope getInternalClassScope() {
    var stb = getSourceTypeBinding();
    return stb != null ? stb.scope : null;
  }

  @Override
  public ReferenceBinding getInternalBinding() {
    return m_binding;
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
    return m_unit.computeIfAbsentAndGet(() -> {
      var scope = getInternalClassScope();
      if (scope != null) {
        var cuScope = scope.compilationUnitScope();
        if (cuScope != null) {
          return javaEnvWithEcj().createDeclarationCompilationUnit(cuScope.referenceContext);
        }
      }

      //binary
      var parent = getDeclaringType();
      if (parent == null) {
        return javaEnvWithEcj().createSyntheticCompilationUnit(this);
      }
      return parent.getCompilationUnit();
    });
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  @Override
  public PackageSpi getPackage() {
    return m_package.computeIfAbsentAndGet(() -> packageOf(m_binding, javaEnvWithEcj()));
  }

  @Override
  public String getName() {
    return m_name.computeIfAbsentAndGet(() -> {
      if (isAnonymous()) {
        // for type variable bindings
        return new String(m_binding.sourceName);
      }
      return qualifiedNameOf(m_binding.qualifiedPackageName(), m_binding.qualifiedSourceName());
    });
  }

  @Override
  public String getElementName() {
    return m_elementName.computeIfAbsentAndGet(() -> new String(m_binding.sourceName));
  }

  @Override
  public List<FieldSpi> getFields() {
    return m_fields.computeIfAbsentAndGet(() -> {
      getSourceTypeBinding();
      FieldBinding[] fields;
      synchronized (javaEnvWithEcj().lock()) {
        fields = m_binding.fields();
      }
      if (fields == null || fields.length < 1) {
        return emptyList();
      }

      fields = Arrays.copyOf(fields, fields.length);
      Arrays.sort(fields, FieldBindingComparator.INSTANCE);
      List<FieldSpi> result = new ArrayList<>(fields.length);
      for (var fd : fields) {
        if (fd.isSynthetic()) {
          continue;
        }
        result.add(javaEnvWithEcj().createBindingField(this, fd));
      }
      return result;
    });
  }

  @Override
  @SuppressWarnings("squid:S1067")
  public List<MethodSpi> getMethods() {
    return m_methods.computeIfAbsentAndGet(() -> {
      MethodBinding[] methods;
      synchronized (javaEnvWithEcj().lock()) {
        getSourceTypeBinding();
        methods = m_binding.methods();
      }
      if (methods == null || methods.length < 1) {
        return emptyList();
      }

      methods = Arrays.copyOf(methods, methods.length);
      Arrays.sort(methods, MethodBindingComparator.INSTANCE);
      List<MethodSpi> result = new ArrayList<>(methods.length);
      for (var a : methods) {
        if ((a.modifiers & ExtraCompilerModifiers.AccIsDefaultConstructor) != 0) {
          continue;
        }
        if (a.isBridge() || a.isSynthetic() || a.isDefaultAbstract()) {
          continue;
        }

        // jdt: default constructors are not reported as 'isSynthetic'
        if (a.isConstructor() && a.sourceMethod() != null && a.sourceMethod().bodyStart == 0) {
          continue;
        }
        result.add(javaEnvWithEcj().createBindingMethod(this, a));
      }
      return result;

    });
  }

  @Override
  public List<TypeSpi> getTypes() {
    return m_memberTypes.computeIfAbsentAndGet(() -> bindingsToTypes(javaEnvWithEcj(), computeMemberTypes(this), this, () -> withNewElement(BindingTypeWithEcj::computeMemberTypes)));
  }

  protected static ReferenceBinding[] computeMemberTypes(BindingTypeWithEcj owner) {
    ReferenceBinding[] memberTypes;
    synchronized (owner.javaEnvWithEcj().lock()) {
      owner.getSourceTypeBinding();
      memberTypes = owner.m_binding.memberTypes();
    }
    if (memberTypes == null || memberTypes.length < 1) {
      return Binding.NO_MEMBER_TYPES;
    }

    memberTypes = Arrays.copyOf(memberTypes, memberTypes.length);
    Arrays.sort(memberTypes, TypeBindingComparator.INSTANCE);
    return memberTypes;
  }

  protected static ReferenceBinding getEnclosingTypeBinding(BindingTypeWithEcj t) {
    return t.m_binding.enclosingType();
  }

  @Override
  public TypeSpi getDeclaringType() {
    return m_declaringType.computeIfAbsentAndGet(() -> bindingToType(javaEnvWithEcj(), getEnclosingTypeBinding(this), null, false, () -> withNewElement(BindingTypeWithEcj::getEnclosingTypeBinding)));
  }

  protected static ReferenceBinding getSuperClassBinding(BindingTypeWithEcj ref) {
    synchronized (ref.javaEnvWithEcj().lock()) {
      ref.getSourceTypeBinding();
      return ref.m_binding.superclass();
    }
  }

  @Override
  public TypeSpi getSuperClass() {
    return m_superClass.computeIfAbsentAndGet(() -> bindingToType(javaEnvWithEcj(), getSuperClassBinding(this), () -> withNewElement(BindingTypeWithEcj::getSuperClassBinding)));
  }

  protected static ReferenceBinding[] getSuperInterfaceBindings(BindingTypeWithEcj ref) {
    synchronized (ref.javaEnvWithEcj().lock()) {
      ref.getSourceTypeBinding();
      return ref.m_binding.superInterfaces();
    }
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    return m_superInterfaces.computeIfAbsentAndGet(() -> bindingsToTypes(javaEnvWithEcj(), getSuperInterfaceBindings(this), () -> withNewElement(BindingTypeWithEcj::getSuperInterfaceBindings)));
  }

  @Override
  public boolean isWildcardType() {
    return m_isWildcard;
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
    return m_typeArguments.computeIfAbsentAndGet(() -> bindingsToTypes(javaEnvWithEcj(), computeTypeArguments(this), () -> withNewElement(BindingTypeWithEcj::computeTypeArguments)));
  }

  protected static TypeBinding[] computeTypeArguments(BindingTypeWithEcj type) {
    type.getSourceTypeBinding();
    var owner = type.m_binding;
    if (owner instanceof ParameterizedTypeBinding) {
      return ((ParameterizedTypeBinding) owner).arguments;
    }
    return Binding.NO_TYPES;
  }

  protected TypeVariableBinding[] getTypeVariables() {
    //ask this or the actualType since we do not distinguish between the virtual parameterized type with arguments and the effective parameterized type with parameters
    var refType = m_binding;
    if (m_binding.actualType() != null) {
      refType = m_binding.actualType();
    }
    return refType.typeVariables();
  }

  @Override
  public boolean hasTypeParameters() {
    getSourceTypeBinding();
    var typeVariables = getTypeVariables();
    return typeVariables != null && typeVariables.length > 0;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return m_typeParameters.computeIfAbsentAndGet(() -> {
      getSourceTypeBinding();
      return createTypeParameters(this, getTypeVariables());
    });
  }

  @Override
  public List<BindingAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> {
      getSourceTypeBinding();
      return createBindingAnnotations(this, nvl(m_binding.actualType(), m_binding));
    });
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = getTypeFlags(m_binding.modifiers, null, hasDeprecatedAnnotation(getAnnotations()));
    }
    return m_flags;
  }

  /**
   * if the type is a source type then return it, otherwise return null
   */
  protected SourceTypeBinding getSourceTypeBinding() {
    return m_sourceTypeBindingRef.computeIfAbsentAndGet(() -> {
      var b = nvl(m_binding.original(), m_binding);
      if (b instanceof SourceTypeBinding) {
        return (SourceTypeBinding) b;
      }
      return null;
    });
  }

  @Override
  public boolean isAnonymous() {
    return m_binding.compoundName == null || m_binding.compoundName.length < 1;
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var reference = nvl(m_binding.original(), m_binding);
      if (reference instanceof SourceTypeBinding) {
        var decl = ((SourceTypeBinding) reference).scope.referenceContext;
        return javaEnvWithEcj().getSource(getCompilationUnit(), decl.declarationSourceStart, decl.declarationSourceEnd);
      }
      return null;
    });
  }

  @Override
  public ISourceRange getSourceOfStaticInitializer() {
    return m_staticInitSource.computeIfAbsentAndGet(() -> {
      if (m_binding instanceof SourceTypeBinding) {
        var decl = ((SourceTypeBinding) m_binding).scope.referenceContext;
        return Arrays.stream(decl.fields)
            .filter(fieldDecl -> fieldDecl.type == null && fieldDecl.name == null)
            .findAny()
            .map(fieldDecl -> javaEnvWithEcj().getSource(getCompilationUnit(), fieldDecl.declarationSourceStart, fieldDecl.declarationSourceEnd))
            .orElse(null);
      }
      return null;
    });
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      if (m_binding instanceof SourceTypeBinding) {
        var decl = ((SourceTypeBinding) m_binding).scope.referenceContext;
        return createSourceRange(decl.javadoc, getCompilationUnit(), javaEnvWithEcj());
      }
      return null;
    });
  }
}
