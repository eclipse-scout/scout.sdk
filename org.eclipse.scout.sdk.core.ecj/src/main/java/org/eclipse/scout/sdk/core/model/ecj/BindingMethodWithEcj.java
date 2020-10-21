/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
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
import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.MethodImplementor;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 *
 */
public class BindingMethodWithEcj extends AbstractMemberWithEcj<IMethod> implements MethodSpi {
  private final BindingTypeWithEcj m_declaringType;
  private final MethodBinding m_binding;
  private final FinalValue<TypeSpi> m_returnType;
  private final FinalValue<List<BindingAnnotationWithEcj>> m_annotations;
  private final FinalValue<String> m_name;
  private final FinalValue<List<TypeSpi>> m_exceptions;
  private final FinalValue<List<MethodParameterSpi>> m_arguments;
  private final FinalValue<List<TypeParameterSpi>> m_typeParameters;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<ISourceRange> m_bodySource;
  private final FinalValue<ISourceRange> m_javaDocSource;
  private int m_flags;

  protected BindingMethodWithEcj(JavaEnvironmentWithEcj env, BindingTypeWithEcj declaringType, MethodBinding binding) {
    super(env);
    m_declaringType = Ensure.notNull(declaringType);
    m_binding = Ensure.notNull(binding);
    m_flags = -1;
    m_returnType = new FinalValue<>();
    m_annotations = new FinalValue<>();
    m_name = new FinalValue<>();
    m_exceptions = new FinalValue<>();
    m_arguments = new FinalValue<>();
    m_typeParameters = new FinalValue<>();
    m_source = new FinalValue<>();
    m_bodySource = new FinalValue<>();
    m_javaDocSource = new FinalValue<>();
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
    return SpiWithEcjUtils.findNewMethodIn(this);
  }

  @Override
  protected IMethod internalCreateApi() {
    return new MethodImplementor(this);
  }

  public MethodBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public List<BindingAnnotationWithEcj> getAnnotations() {
    return m_annotations.computeIfAbsentAndGet(() -> SpiWithEcjUtils.createBindingAnnotations(this, SpiWithEcjUtils.nvl(m_binding.original(), m_binding)));
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithEcjUtils.getMethodFlags(m_binding.modifiers, false, SpiWithEcjUtils.hasDeprecatedAnnotation(getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public String getElementName() {
    return m_name.computeIfAbsentAndGet(() -> {
      if (m_binding.isConstructor()) {
        return m_declaringType.getElementName();
      }
      return new String(m_binding.selector);
    });
  }

  @Override
  public List<TypeSpi> getExceptionTypes() {
    return m_exceptions.computeIfAbsentAndGet(() -> SpiWithEcjUtils.bindingsToTypes(javaEnvWithEcj(), m_binding.thrownExceptions));
  }

  @Override
  public TypeSpi getReturnType() {
    return m_returnType.computeIfAbsentAndGet(() -> {
      if (isConstructor()) {
        return null;
      }
      return SpiWithEcjUtils.bindingToType(javaEnvWithEcj(), m_binding.returnType);
    });
  }

  @Override
  public List<MethodParameterSpi> getParameters() {
    return m_arguments.computeIfAbsentAndGet(() -> {
      var arguments = m_binding.parameters;
      if (arguments == null || arguments.length < 1) {
        return emptyList();
      }

      List<MethodParameterSpi> result = new ArrayList<>(arguments.length);
      var originalBinding = m_binding.original();
      var sourceMethod = m_binding.sourceMethod();
      var originalSourceMethod = originalBinding.sourceMethod();

      for (var i = 0; i < arguments.length; i++) {
        var name = getParamName(m_binding, sourceMethod, i);
        if (name == null) {
          name = getParamName(originalBinding, originalSourceMethod, i);
          if (name == null) {
            name = ("arg" + i).toCharArray();
          }
        }

        var flags = Flags.AccDefault;
        if (sourceMethod != null && sourceMethod.arguments.length > i) {
          flags = sourceMethod.arguments[i].modifiers;
        }
        else if (originalSourceMethod != null && originalSourceMethod.arguments.length > i) {
          flags = originalSourceMethod.arguments[i].modifiers;
        }

        result.add(javaEnvWithEcj().createBindingMethodParameter(this, arguments[i], name, flags, i));
      }
      return result;
    });
  }

  @SuppressWarnings("squid:S1168") // return empty array instead of null
  protected static char[] getParamName(MethodBinding b, AbstractMethodDeclaration sourceMethod, int paramIndex) {
    if (b.parameterNames.length > paramIndex) {
      return b.parameterNames[paramIndex];
    }

    if (sourceMethod != null && sourceMethod.arguments.length > paramIndex) {
      return sourceMethod.arguments[paramIndex].name;
    }

    // if no parameter name info is in the class file
    return null;
  }

  protected TypeVariableBinding[] getTypeVariables() {
    //ask this or the actualType since we do not distinguish between the virtual parameterized type with arguments and the effective parameterized type with parameters
    return SpiWithEcjUtils.nvl(m_binding.original(), m_binding).typeVariables();
  }

  @Override
  public boolean hasTypeParameters() {
    var typeVariables = getTypeVariables();
    return typeVariables != null && typeVariables.length > 0;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    return m_typeParameters.computeIfAbsentAndGet(() -> SpiWithEcjUtils.createTypeParameters(this, getTypeVariables()));
  }

  @Override
  public BindingTypeWithEcj getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public boolean isConstructor() {
    return m_binding.isConstructor();
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var decl = SpiWithEcjUtils.sourceMethodOf(m_binding);
      if (decl == null) {
        return null;
      }
      var cu = m_declaringType.getCompilationUnit();
      return javaEnvWithEcj().getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    });
  }

  @Override
  public ISourceRange getSourceOfBody() {
    return m_bodySource.computeIfAbsentAndGet(() -> {
      var decl = SpiWithEcjUtils.sourceMethodOf(m_binding);
      if (decl == null) {
        return null;
      }
      var cu = m_declaringType.getCompilationUnit();
      return javaEnvWithEcj().getSource(cu, decl.bodyStart, decl.bodyEnd);
    });
  }

  @Override
  public ISourceRange getJavaDoc() {
    return m_javaDocSource.computeIfAbsentAndGet(() -> {
      var decl = SpiWithEcjUtils.sourceMethodOf(m_binding);
      if (decl == null) {
        return null;
      }
      return SpiWithEcjUtils.getJavaDocSource(decl.javadoc, m_declaringType, javaEnvWithEcj());
    });
  }
}
