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

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationValueImplementor;
import org.eclipse.scout.sdk.core.model.spi.AnnotationValueSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;

/**
 *
 */
public class BindingAnnotationValueWithJdt extends AbstractJavaElementWithJdt<IAnnotationValue>implements AnnotationValueSpi {
  private final BindingAnnotationWithJdt m_declaringAnnotation;
  private final ElementValuePair m_binding;
  private final String m_name;
  private final boolean m_syntheticDefaultValue;
  private Expression m_sourceExpression;
  private IMetaValue m_value;

  BindingAnnotationValueWithJdt(JavaEnvironmentWithJdt env, BindingAnnotationWithJdt owner, ElementValuePair bindingPair, boolean syntheticDefaultValue) {
    super(env);
    m_declaringAnnotation = Validate.notNull(owner);
    m_binding = Validate.notNull(bindingPair);
    m_syntheticDefaultValue = syntheticDefaultValue;
    m_name = new String(m_binding.getName());
  }

  @Override
  protected JavaElementSpi internalFindNewElement(JavaEnvironmentWithJdt newEnv) {
    //not supported
    return null;
  }

  @Override
  protected IAnnotationValue internalCreateApi() {
    return new AnnotationValueImplementor(this);
  }

  public ElementValuePair getInternalBinding() {
    return m_binding;
  }

  @Override
  public String getElementName() {
    return m_name;
  }

  @Override
  public IMetaValue getMetaValue() {
    if (m_value == null) {
      m_value = SpiWithJdtUtils.resolveCompiledValue(m_env, m_declaringAnnotation.getOwner(), m_binding.getValue());
    }
    return m_value;
  }

  @Override
  public boolean isSyntheticDefaultValue() {
    return m_syntheticDefaultValue;

  }

  @Override
  public BindingAnnotationWithJdt getDeclaringAnnotation() {
    return m_declaringAnnotation;
  }

  @Override
  public ISourceRange getSource() {
    final MemberValuePair pairDecl = SpiWithJdtUtils.findAnnotationValueDeclaration(this);
    if (pairDecl != null) {
      return new ISourceRange() {
        @Override
        public String toString() {
          return pairDecl.toString();
        }
      };
    }
    return null;
  }

  protected Expression getSourceExpression() {
    if (m_sourceExpression == null) {
      MemberValuePair pairDecl = SpiWithJdtUtils.findAnnotationValueDeclaration(this);
      if (pairDecl != null) {
        m_sourceExpression = pairDecl.value;
      }
    }
    return m_sourceExpression;
  }

  @Override
  public ISourceRange getSourceOfExpression() {
    final Expression expr = getSourceExpression();
    if (expr != null) {
      return new ISourceRange() {
        @Override
        public String toString() {
          return expr.toString();
        }
      };
    }
    return null;
  }

}
