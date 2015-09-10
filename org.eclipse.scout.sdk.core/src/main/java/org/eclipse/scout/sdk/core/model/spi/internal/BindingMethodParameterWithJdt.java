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

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.MethodParameterImplementor;
import org.eclipse.scout.sdk.core.model.spi.CompilationUnitSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.spi.TypeSpi;

/**
 *
 */
public class BindingMethodParameterWithJdt extends AbstractJavaElementWithJdt<IMethodParameter>implements MethodParameterSpi {
  private final BindingMethodWithJdt m_declaringMethod;
  private final TypeBinding m_binding;
  private final int m_index;
  private final char[] m_name;
  private TypeSpi m_dataType;

  private String m_nameS;
  private List<BindingAnnotationWithJdt> m_annotations;

  BindingMethodParameterWithJdt(JavaEnvironmentWithJdt env, BindingMethodWithJdt declaringMethod, TypeBinding binding, char[] name, int index) {
    super(env);
    m_index = index;
    m_name = Validate.notNull(name);
    m_binding = Validate.notNull(binding);
    m_declaringMethod = Validate.notNull(declaringMethod);
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    MethodSpi newMethod = (MethodSpi) getDeclaringMethod().internalFindNewElement(newEnv);
    if (newMethod != null) {
      if (newMethod.getParameters().size() > m_index) {
        return newMethod.getParameters().get(m_index);
      }
    }
    return null;
  }

  @Override
  protected IMethodParameter internalCreateApi() {
    return new MethodParameterImplementor(this);
  }

  public TypeBinding getInternalBinding() {
    return m_binding;
  }

  @Override
  public String getElementName() {
    if (m_nameS == null && m_name != null) {
      m_nameS = new String(m_name);
    }
    return m_nameS;
  }

  @Override
  public TypeSpi getDataType() {
    if (m_dataType == null) {
      m_dataType = SpiWithJdtUtils.bindingToType(m_env, m_binding);
    }
    return m_dataType;
  }

  @Override
  public int getFlags() {
    return Flags.AccDefault;
  }

  @Override
  public BindingMethodWithJdt getDeclaringMethod() {
    return m_declaringMethod;
  }

  @Override
  public List<BindingAnnotationWithJdt> getAnnotations() {
    if (m_annotations == null) {
      AnnotationBinding[][] a = m_declaringMethod.getInternalBinding().getParameterAnnotations();
      m_annotations = SpiWithJdtUtils.createBindingAnnotations(m_env, this, a != null ? a[m_index] : new AnnotationBinding[0]);
    }
    return m_annotations;
  }

  @Override
  public ISourceRange getSource() {
    AbstractMethodDeclaration declMethod = m_declaringMethod.getInternalBinding().sourceMethod();
    if (declMethod != null) {
      CompilationUnitSpi cu = m_declaringMethod.getDeclaringType().getCompilationUnit();
      Argument decl = declMethod.arguments[m_index];
      return m_env.getSource(cu, decl.declarationSourceStart, decl.declarationSourceEnd);
    }
    return null;
  }

}
