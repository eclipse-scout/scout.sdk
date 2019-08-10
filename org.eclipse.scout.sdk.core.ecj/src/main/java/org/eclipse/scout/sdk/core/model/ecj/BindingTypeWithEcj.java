/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.eclipse.scout.sdk.core.model.ecj.SourcePositionComparators.FieldBindingComparator;
import org.eclipse.scout.sdk.core.model.ecj.SourcePositionComparators.MethodBindingComparator;
import org.eclipse.scout.sdk.core.model.ecj.SourcePositionComparators.TypeBindingComparator;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
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
  private final FinalValue<BindingTypeWithEcj> m_declaringType;
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
  private int m_flags;

  protected BindingTypeWithEcj(JavaEnvironmentWithEcj env, ReferenceBinding binding, BindingTypeWithEcj declaringType, boolean isWildcard) {
    super(env);
    if (binding == null || binding instanceof MissingTypeBinding || binding instanceof ProblemReferenceBinding) {
      // type not found -> parsing error. do not silently continue
      throw new MissingTypeException(String.valueOf(binding));
    }
    m_binding = Ensure.notNull(binding);
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
    char[] qualifiedPackageName = binding.qualifiedPackageName();
    if (qualifiedPackageName == null || qualifiedPackageName.length < 1) {
      return env.createDefaultPackage();
    }
    return env.createPackage(new String(qualifiedPackageName));
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
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
    SourceTypeBinding stb = getSourceTypeBinding();
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
      ClassScope scope = getInternalClassScope();
      if (scope != null) {
        CompilationUnitScope cuScope = scope.compilationUnitScope();
        if (cuScope != null) {
          return javaEnvWithEcj().createDeclarationCompilationUnit(cuScope.referenceContext);
        }
      }

      //binary
      TypeSpi parent = getDeclaringType();
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
      if (m_binding.compoundName == null) {
        // for type variable bindings
        return new String(m_binding.sourceName);
      }
      return CharOperation.toString(m_binding.compoundName);
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
      FieldBinding[] fields = m_binding.fields();
      if (fields == null || fields.length < 1) {
        return emptyList();
      }

      fields = Arrays.copyOf(fields, fields.length);
      Arrays.sort(fields, FieldBindingComparator.INSTANCE);
      List<FieldSpi> result = new ArrayList<>(fields.length);
      for (FieldBinding fd : fields) {
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
      getSourceTypeBinding();
      MethodBinding[] methods = m_binding.methods();
      if (methods == null || methods.length < 1) {
        return emptyList();
      }

      methods = Arrays.copyOf(methods, methods.length);
      Arrays.sort(methods, MethodBindingComparator.INSTANCE);
      List<MethodSpi> result = new ArrayList<>(methods.length);
      for (MethodBinding a : methods) {
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
    return m_memberTypes.computeIfAbsentAndGet(() -> {
      getSourceTypeBinding();
      ReferenceBinding[] memberTypes = m_binding.memberTypes();
      if (memberTypes == null || memberTypes.length < 1) {
        return emptyList();
      }

      memberTypes = Arrays.copyOf(memberTypes, memberTypes.length);
      Arrays.sort(memberTypes, TypeBindingComparator.INSTANCE);
      List<TypeSpi> result = new ArrayList<>(memberTypes.length);
      for (ReferenceBinding d : memberTypes) {
        TypeSpi t = SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), d, this);
        result.add(t);
      }
      return result;
    });
  }

  @Override
  public BindingTypeWithEcj getDeclaringType() {
    return m_declaringType.computeIfAbsentAndGet(() -> {
      ReferenceBinding enclosingType = m_binding.enclosingType();
      if (enclosingType == null) {
        return null;
      }
      return new BindingTypeWithEcj(javaEnvWithEcj(), enclosingType, null, false);
    });
  }

  @Override
  public TypeSpi getSuperClass() {
    return m_superClass.computeIfAbsentAndGet(() -> {
      getSourceTypeBinding();
      ReferenceBinding superclass = m_binding.superclass();
      if (superclass == null) {
        return null;
      }
      return SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), superclass);
    });
  }

  @Override
  public List<TypeSpi> getSuperInterfaces() {
    return m_superInterfaces.computeIfAbsentAndGet(() -> {
      getSourceTypeBinding();
      ReferenceBinding[] superInterfaces = m_binding.superInterfaces();
      if (superInterfaces == null || superInterfaces.length < 1) {
        return emptyList();
      }

      List<TypeSpi> result = new ArrayList<>(superInterfaces.length);
      for (ReferenceBinding b : superInterfaces) {
        TypeSpi t = SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), b);
        if (t != null) {
          result.add(t);
        }
      }
      return result;
    });
  }

  @Override
  public boolean isWildcardType() {
    return m_isWildcard;
  }

  @Override
  public List<TypeSpi> getTypeArguments() {
    return m_typeArguments.computeIfAbsentAndGet(() -> {
      getSourceTypeBinding();
      if (m_binding instanceof ParameterizedTypeBinding) {
        ParameterizedTypeBinding ptb = (ParameterizedTypeBinding) m_binding;
        TypeBinding[] arguments = ptb.arguments;
        if (arguments != null && arguments.length > 0) {
          List<TypeSpi> result = new ArrayList<>(arguments.length);
          for (TypeBinding b : arguments) {
            TypeSpi type = SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), b);
            if (type != null) {
              result.add(type);
            }
          }
          return result;
        }
      }
      return emptyList();
    });
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
    return m_typeParameters.computeIfAbsentAndGet(() -> {
      getSourceTypeBinding();
      TypeVariableBinding[] typeParams = getTypeVariables();
      if (typeParams == null || typeParams.length < 1) {
        return emptyList();
      }

      List<TypeParameterSpi> result = new ArrayList<>(typeParams.length);
      int index = 0;
      for (TypeVariableBinding param : typeParams) {
        result.add(javaEnvWithEcj().createBindingTypeParameter(this, param, index));
        index++;
      }
      return result;
    });
  }

  @Override
  public List<BindingAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> {
      getSourceTypeBinding();
      return SpiWithEcjUtils
          .createBindingAnnotations(javaEnvWithEcj(), this, SpiWithEcjUtils.nvl(m_binding.actualType(), m_binding)
              .getAnnotations());
    });
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithEcjUtils.getTypeFlags(m_binding.modifiers, null, SpiWithEcjUtils.hasDeprecatedAnnotation(m_binding.getAnnotations()));
    }
    return m_flags;
  }

  /**
   * if the type is a source type then return it, otherwise return null
   */
  protected SourceTypeBinding getSourceTypeBinding() {
    return m_sourceTypeBindingRef.computeIfAbsentAndGet(() -> {
      TypeBinding b = SpiWithEcjUtils.nvl(m_binding.original(), m_binding);
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
      TypeBinding reference = SpiWithEcjUtils.nvl(m_binding.original(), m_binding);
      if (reference instanceof SourceTypeBinding) {
        TypeDeclaration decl = ((SourceTypeBinding) reference).scope.referenceContext;
        return javaEnvWithEcj().getSource(getCompilationUnit(), decl.declarationSourceStart, decl.declarationSourceEnd);
      }
      return null;
    });
  }

  @Override
  public ISourceRange getSourceOfStaticInitializer() {
    return m_staticInitSource.computeIfAbsentAndGet(() -> {
      if (m_binding instanceof SourceTypeBinding) {
        TypeDeclaration decl = ((SourceTypeBinding) m_binding).scope.referenceContext;
        for (FieldDeclaration fieldDecl : decl.fields) {
          if (fieldDecl.type == null && fieldDecl.name == null) {
            CompilationUnitSpi cu = getCompilationUnit();
            return javaEnvWithEcj().getSource(cu, fieldDecl.declarationSourceStart, fieldDecl.declarationSourceEnd);
          }
        }
      }
      return null;
    });
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      if (m_binding instanceof SourceTypeBinding) {
        TypeDeclaration decl = ((SourceTypeBinding) m_binding).scope.referenceContext;
        Javadoc doc = decl.javadoc;
        if (doc != null) {
          CompilationUnitSpi cu = getCompilationUnit();
          return javaEnvWithEcj().getSource(cu, doc.sourceStart, doc.sourceEnd);
        }
      }
      return null;
    });
  }
}
