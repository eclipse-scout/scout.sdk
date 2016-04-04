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
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.MethodImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class BindingMethodWithJdt extends AbstractMemberWithJdt<IMethod> implements MethodSpi {
  private final BindingTypeWithJdt m_declaringType;
  private final MethodBinding m_binding;
  private TypeSpi m_returnType;
  private List<BindingAnnotationWithJdt> m_annotations;
  private int m_flags;
  private String m_name;
  private List<TypeSpi> m_exceptions;
  private List<MethodParameterSpi> m_arguments;
  private List<TypeParameterSpi> m_typeParameters;
  private MethodSpi m_originalMethod;
  private ISourceRange m_source;
  private ISourceRange m_bodySource;
  private ISourceRange m_javaDocSource;

  BindingMethodWithJdt(JavaEnvironmentWithJdt env, BindingTypeWithJdt declaringType, MethodBinding binding) {
    super(env);
    m_declaringType = Validate.notNull(declaringType);
    m_binding = Validate.notNull(binding);
    m_flags = -1;
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    TypeSpi newType = (TypeSpi) getDeclaringType().internalFindNewElement(newEnv);
    if (newType != null) {
      final String oldSig = SpiWithJdtUtils.createMethodId(this);
      for (MethodSpi newM : newType.getMethods()) {
        if (oldSig.equals(SpiWithJdtUtils.createMethodId(newM))) {
          return newM;
        }
      }
    }
    return null;
  }

  @Override
  protected IMethod internalCreateApi() {
    return new MethodImplementor(this);
  }

  public MethodBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public List<BindingAnnotationWithJdt> getAnnotations() {
    if (m_annotations == null) {
      MethodBinding refMethod = m_binding.original() != null ? m_binding.original() : m_binding;
      m_annotations = SpiWithJdtUtils.createBindingAnnotations(m_env, this, refMethod.getAnnotations());
    }
    return m_annotations;
  }

  @Override
  public int getFlags() {
    if (m_flags < 0) {
      m_flags = SpiWithJdtUtils.getMethodFlags(m_binding.modifiers, false, SpiWithJdtUtils.hasDeprecatedAnnotation(m_binding.getAnnotations()));
    }
    return m_flags;
  }

  @Override
  public String getElementName() {
    if (m_name == null) {
      if (m_binding.isConstructor()) {
        m_name = m_declaringType.getElementName();
      }
      else {
        m_name = new String(m_binding.selector);
      }
    }
    return m_name;
  }

  @Override
  public List<TypeSpi> getExceptionTypes() {
    if (m_exceptions == null) {
      ReferenceBinding[] exceptions = m_binding.thrownExceptions;
      if (exceptions == null || exceptions.length < 1) {
        m_exceptions = new ArrayList<>(0);
      }
      else {
        List<TypeSpi> result = new ArrayList<>(exceptions.length);
        for (ReferenceBinding r : exceptions) {
          TypeSpi t = SpiWithJdtUtils.bindingToType(m_env, r);
          if (t != null) {
            result.add(t);
          }
        }
        m_exceptions = result;
      }
    }
    return m_exceptions;
  }

  @Override
  public TypeSpi getReturnType() {
    if (m_returnType == null && !isConstructor()) {
      m_returnType = SpiWithJdtUtils.bindingToType(m_env, m_binding.returnType);
    }
    return m_returnType;
  }

  @Override
  public List<MethodParameterSpi> getParameters() {
    if (m_arguments == null) {
      TypeBinding[] arguments = m_binding.parameters;
      if (arguments == null || arguments.length < 1) {
        m_arguments = new ArrayList<>(0);
      }
      else {
        List<MethodParameterSpi> result = new ArrayList<>(arguments.length);
        for (int i = 0; i < arguments.length; i++) {
          char[] name = getParamName(m_binding, i);
          if (name == null) {
            name = getParamName(m_binding.original(), i);
            if (name == null) {
              name = ("arg" + i).toCharArray();
            }
          }
          result.add(m_env.createBindingMethodParameter(this, arguments[i], name, i));
        }
        m_arguments = result;
      }
    }
    return m_arguments;
  }

  protected static char[] getParamName(MethodBinding b, int paramIndex) {
    if (b.parameterNames.length > paramIndex) {
      return b.parameterNames[paramIndex];
    }

    AbstractMethodDeclaration sourceMethod = b.sourceMethod();
    if (sourceMethod != null && sourceMethod.arguments.length > paramIndex) {
      return sourceMethod.arguments[paramIndex].name;
    }

    // if no parameter name info is in the class file
    return null;
  }

  protected TypeVariableBinding[] getTypeVariables() {
    //ask this or the actualType since we do not distinguish between the virtual parameterized type with arguments and the effective parameterized type with parameters
    MethodBinding refMethod = m_binding.original() != null ? m_binding.original() : m_binding;
    return refMethod.typeVariables();
  }

  @Override
  public boolean hasTypeParameters() {
    TypeVariableBinding[] typeVariables = getTypeVariables();
    return typeVariables != null && typeVariables.length > 0;
  }

  @Override
  public List<TypeParameterSpi> getTypeParameters() {
    if (m_typeParameters == null) {
      if (hasTypeParameters()) {
        TypeVariableBinding[] typeParams = getTypeVariables();

        List<TypeParameterSpi> result = new ArrayList<>(typeParams.length);
        int index = 0;
        for (TypeVariableBinding param : typeParams) {
          result.add(m_env.createBindingTypeParameter(this, param, index));
          index++;
        }
        m_typeParameters = result;
      }
      else {
        m_typeParameters = new ArrayList<>(0);
      }
    }
    return m_typeParameters;
  }

  @Override
  public MethodSpi getOriginalMethod() {
    if (m_originalMethod == null) {
      MethodBinding ref = m_binding.original();
      if (ref == null || ref == m_binding) {
        m_originalMethod = this;
      }
      else {
        BindingTypeWithJdt refType = (BindingTypeWithJdt) m_declaringType.getOriginalType();
        m_originalMethod = m_env.createBindingMethod(refType, ref);
      }
    }
    return m_originalMethod;
  }

  @Override
  public BindingTypeWithJdt getDeclaringType() {
    return m_declaringType;
  }

  @Override
  public boolean isConstructor() {
    return m_binding.isConstructor();
  }

  @Override
  public ISourceRange getSource() {
    if (m_source == null) {
      AbstractMethodDeclaration decl = m_binding.sourceMethod();
      if (decl != null) {
        CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
        m_source = m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
      }
      else {
        m_source = ISourceRange.NO_SOURCE;
      }
    }
    return m_source;
  }

  @Override
  public ISourceRange getSourceOfBody() {
    if (m_bodySource == null) {
      AbstractMethodDeclaration decl = m_binding.sourceMethod();
      if (decl != null) {
        CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
        m_bodySource = m_env.getSource(cu, decl.bodyStart, decl.bodyEnd);
      }
      else {
        m_bodySource = ISourceRange.NO_SOURCE;
      }
    }
    return m_bodySource;
  }

  @Override
  public ISourceRange getJavaDoc() {
    if (m_javaDocSource == null) {
      AbstractMethodDeclaration decl = m_binding.sourceMethod();
      if (decl != null) {
        Javadoc doc = decl.javadoc;
        if (doc != null) {
          CompilationUnitSpi cu = m_declaringType.getCompilationUnit();
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
