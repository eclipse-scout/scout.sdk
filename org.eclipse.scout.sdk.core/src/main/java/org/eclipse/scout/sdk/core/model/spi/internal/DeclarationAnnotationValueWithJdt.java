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

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationValueImplementor;
import org.eclipse.scout.sdk.core.model.spi.AnnotationValueSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;

/**
 *
 */
public class DeclarationAnnotationValueWithJdt extends AbstractJavaElementWithJdt<IAnnotationValue>implements AnnotationValueSpi {
  private final DeclarationAnnotationWithJdt m_declaringAnnotation;
  private final MemberValuePair m_astNode;
  private final String m_name;
  private final boolean m_syntheticDefaultValue;
  private IMetaValue m_value;

  DeclarationAnnotationValueWithJdt(JavaEnvironmentWithJdt env, DeclarationAnnotationWithJdt declaringAnnotation, MemberValuePair astNode, boolean syntheticDefaultValue) {
    super(env);
    m_declaringAnnotation = declaringAnnotation;
    m_astNode = astNode;
    m_syntheticDefaultValue = syntheticDefaultValue;
    m_name = new String(astNode.name);
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

  @Override
  public String getElementName() {
    return m_name;
  }

  @Override
  public IMetaValue getMetaValue() {
    if (m_value == null) {
      ClassScope scope = SpiWithJdtUtils.classScopeOf(m_declaringAnnotation.getOwner());
      m_value = SpiWithJdtUtils.resolveCompiledValue(m_env, m_declaringAnnotation.getOwner(), SpiWithJdtUtils.compileExpression(m_astNode.value, scope));
    }
    return m_value;
  }

  @Override
  public DeclarationAnnotationWithJdt getDeclaringAnnotation() {
    return m_declaringAnnotation;
  }

  @Override
  public boolean isSyntheticDefaultValue() {
    return m_syntheticDefaultValue;
  }

  @Override
  public ISourceRange getSource() {
    return new ISourceRange() {
      @Override
      public String toString() {
        return m_astNode.toString();
      }
    };
  }

  @Override
  public ISourceRange getSourceOfExpression() {
    final Expression expr = m_astNode.value;
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
