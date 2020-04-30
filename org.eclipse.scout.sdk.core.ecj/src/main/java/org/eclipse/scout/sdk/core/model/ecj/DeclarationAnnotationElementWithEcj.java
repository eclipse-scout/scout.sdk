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

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationElementImplementor;
import org.eclipse.scout.sdk.core.model.api.internal.SourceRange;
import org.eclipse.scout.sdk.core.model.ecj.metavalue.MetaValueFactory;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.model.spi.JavaElementSpi;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 *
 */
public class DeclarationAnnotationElementWithEcj extends AbstractJavaElementWithEcj<IAnnotationElement> implements AnnotationElementSpi {
  private final AnnotationSpi m_declaringAnnotation;
  private final MemberValuePair m_astNode;
  private final String m_name;
  private final boolean m_syntheticDefaultValue;
  private final FinalValue<IMetaValue> m_value;
  private final FinalValue<ISourceRange> m_source;
  private final FinalValue<ISourceRange> m_expressionSource;

  protected DeclarationAnnotationElementWithEcj(AbstractJavaEnvironment env, AnnotationSpi declaringAnnotation, MemberValuePair astNode, boolean syntheticDefaultValue) {
    super(env);
    m_declaringAnnotation = declaringAnnotation;
    m_astNode = astNode;
    m_syntheticDefaultValue = syntheticDefaultValue;
    m_name = new String(astNode.name);
    m_value = new FinalValue<>();
    m_source = new FinalValue<>();
    m_expressionSource = new FinalValue<>();
  }

  @Override
  public JavaElementSpi internalFindNewElement() {
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
    return m_value.computeIfAbsentAndGet(() -> {
      ClassScope scope = SpiWithEcjUtils.classScopeOf(m_declaringAnnotation.getOwner());
      Object compiledValue = SpiWithEcjUtils.compileExpression(m_astNode.value, scope, javaEnvWithEcj());
      IMetaValue value = SpiWithEcjUtils.resolveCompiledValue(javaEnvWithEcj(), m_declaringAnnotation.getOwner(), compiledValue);
      if (value != null) {
        return value;
      }

      // value cannot be determined. use unknown because annotation values cannot be null.
      return MetaValueFactory.createUnknown(compiledValue);
    });
  }

  @Override
  public AnnotationSpi getDeclaringAnnotation() {
    return m_declaringAnnotation;
  }

  @Override
  public boolean isDefaultValue() {
    return m_syntheticDefaultValue;
  }

  @Override
  public ISourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> new SourceRange(m_astNode.toString(), m_astNode.sourceStart, m_astNode.sourceEnd));
  }

  @Override
  public ISourceRange getSourceOfExpression() {
    return m_expressionSource.computeIfAbsentAndGet(() -> {
      Expression expr = m_astNode.value;
      if (expr == null) {
        return null;
      }
      return new SourceRange(expr.toString(), expr.sourceStart, expr.sourceEnd);
    });
  }
}
