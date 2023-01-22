/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.findAnnotationValueDeclaration;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.findNewAnnotationElementIn;
import static org.eclipse.scout.sdk.core.java.ecj.SpiWithEcjUtils.resolveCompiledValue;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.scout.sdk.core.java.ecj.metavalue.MetaValueFactory;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.java.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.java.model.api.internal.AnnotationElementImplementor;
import org.eclipse.scout.sdk.core.java.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.java.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.SourceRange;

public class BindingAnnotationElementWithEcj extends AbstractJavaElementWithEcj<IAnnotationElement> implements AnnotationElementSpi {
  private final AnnotationSpi m_declaringAnnotation;
  private final ElementValuePair m_binding;
  private final String m_name;
  private final boolean m_syntheticDefaultValue;
  private final FinalValue<IMetaValue> m_value;
  private final FinalValue<SourceRange> m_source;
  private final FinalValue<SourceRange> m_expressionSource;
  private final FinalValue<MemberValuePair> m_memberValuePair;

  protected BindingAnnotationElementWithEcj(AbstractJavaEnvironment env, AnnotationSpi owner, ElementValuePair bindingPair, boolean syntheticDefaultValue) {
    super(env);
    m_declaringAnnotation = Ensure.notNull(owner);
    m_binding = Ensure.notNull(bindingPair);
    m_syntheticDefaultValue = syntheticDefaultValue;
    m_name = new String(m_binding.getName());
    m_value = new FinalValue<>();
    m_source = new FinalValue<>();
    m_expressionSource = new FinalValue<>();
    m_memberValuePair = new FinalValue<>();
  }

  @Override
  public AnnotationElementSpi internalFindNewElement() {
    return findNewAnnotationElementIn(getDeclaringAnnotation(), getElementName());
  }

  @Override
  protected IAnnotationElement internalCreateApi() {
    return new AnnotationElementImplementor(this);
  }

  public ElementValuePair getInternalBinding() {
    return m_binding;
  }

  @Override
  public String getElementName() {
    return m_name;
  }

  protected static Object getAnnotationElementValue(BindingAnnotationElementWithEcj a) {
    return a.m_binding.getValue();
  }

  @Override
  public IMetaValue getMetaValue() {
    return m_value.computeIfAbsentAndGet(() -> {
      var value = getAnnotationElementValue(this);
      var metaVal = resolveCompiledValue(javaEnvWithEcj(), m_declaringAnnotation.getOwner(), value, () -> withNewElement(BindingAnnotationElementWithEcj::getAnnotationElementValue));
      if (metaVal != null) {
        return metaVal;
      }
      return MetaValueFactory.createUnknown(value); // value cannot be determined. use unknown because annotation values cannot be null.
    });
  }

  @Override
  public boolean isDefaultValue() {
    return m_syntheticDefaultValue;
  }

  @Override
  public AnnotationSpi getDeclaringAnnotation() {
    return m_declaringAnnotation;
  }

  MemberValuePair memberValuePair() {
    return m_memberValuePair.computeIfAbsentAndGet(() -> findAnnotationValueDeclaration(this));
  }

  @Override
  public SourceRange getSource() {
    return m_source.computeIfAbsentAndGet(() -> {
      var pairDecl = memberValuePair();
      if (pairDecl == null) {
        return null;
      }
      return new SourceRange(pairDecl.toString(), pairDecl.sourceStart);
    });
  }

  protected Expression getValueExpression() {
    var pairDecl = memberValuePair();
    if (pairDecl == null) {
      return null;
    }
    return pairDecl.value;
  }

  @Override
  public SourceRange getSourceOfExpression() {
    return m_expressionSource.computeIfAbsentAndGet(() -> {
      var expr = getValueExpression();
      if (expr != null) {
        return new SourceRange(expr.toString(), expr.sourceStart);
      }
      return null;
    });
  }
}
