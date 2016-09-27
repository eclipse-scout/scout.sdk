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
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationElementImplementor;
import org.eclipse.scout.sdk.core.model.api.internal.SourceRange;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.model.spi.internal.metavalue.MetaValueFactory;

/**
 *
 */
public class DeclarationAnnotationElementWithJdt extends AbstractJavaElementWithJdt<IAnnotationElement> implements AnnotationElementSpi {
  private final DeclarationAnnotationWithJdt m_declaringAnnotation;
  private final MemberValuePair m_astNode;
  private final String m_name;
  private final boolean m_syntheticDefaultValue;
  private IMetaValue m_value;
  private ISourceRange m_source;
  private ISourceRange m_expressionSource;

  DeclarationAnnotationElementWithJdt(JavaEnvironmentWithJdt env, DeclarationAnnotationWithJdt declaringAnnotation, MemberValuePair astNode, boolean syntheticDefaultValue) {
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
  protected IAnnotationElement internalCreateApi() {
    return new AnnotationElementImplementor(this);
  }

  @Override
  public String getElementName() {
    return m_name;
  }

  @Override
  public IMetaValue getMetaValue() {
    if (m_value == null) {
      ClassScope scope = SpiWithJdtUtils.classScopeOf(m_declaringAnnotation.getOwner());
      Object compiledValue = SpiWithJdtUtils.compileExpression(m_astNode.value, scope);
      m_value = SpiWithJdtUtils.resolveCompiledValue(m_env, m_declaringAnnotation.getOwner(), compiledValue);
      if (m_value == null) {
        // value cannot be determined. use unknown because annotation values cannot be null.
        m_value = MetaValueFactory.createUnknown(compiledValue);
      }
    }
    return m_value;
  }

  @Override
  public DeclarationAnnotationWithJdt getDeclaringAnnotation() {
    return m_declaringAnnotation;
  }

  @Override
  public boolean isDefaultValue() {
    return m_syntheticDefaultValue;
  }

  @Override
  public ISourceRange getSource() {
    if (m_source == null) {
      m_source = new SourceRange(m_astNode.toString(), m_astNode.sourceStart, m_astNode.sourceEnd);
    }
    return m_source;
  }

  @Override
  public ISourceRange getSourceOfExpression() {
    if (m_expressionSource == null) {
      Expression expr = m_astNode.value;
      if (expr == null) {
        m_expressionSource = ISourceRange.NO_SOURCE;
      }
      else {
        m_expressionSource = new SourceRange(expr.toString(), expr.sourceStart, expr.sourceEnd);
      }
    }
    return m_expressionSource;
  }
}
