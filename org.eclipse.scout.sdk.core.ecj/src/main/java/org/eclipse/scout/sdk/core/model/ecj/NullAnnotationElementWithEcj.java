/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.model.ecj;

import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.findNewAnnotationElementIn;

import org.eclipse.scout.sdk.core.model.api.IAnnotationElement;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.internal.AnnotationElementImplementor;
import org.eclipse.scout.sdk.core.model.ecj.metavalue.MetaValueFactory;
import org.eclipse.scout.sdk.core.model.spi.AbstractJavaEnvironment;
import org.eclipse.scout.sdk.core.model.spi.AnnotationElementSpi;
import org.eclipse.scout.sdk.core.model.spi.AnnotationSpi;
import org.eclipse.scout.sdk.core.util.Ensure;

public class NullAnnotationElementWithEcj extends AbstractJavaElementWithEcj<IAnnotationElement> implements AnnotationElementSpi {

  private final AnnotationSpi m_owner;
  private final String m_name;
  private final boolean m_syntheticDefaultValue;

  protected NullAnnotationElementWithEcj(AbstractJavaEnvironment env, AnnotationSpi owner, String name, boolean syntheticDefaultValue) {
    super(env);
    m_owner = Ensure.notNull(owner);
    m_name = Ensure.notBlank(name);
    m_syntheticDefaultValue = syntheticDefaultValue;
  }

  @Override
  protected IAnnotationElement internalCreateApi() {
    return new AnnotationElementImplementor(this);
  }

  @Override
  public AnnotationElementSpi internalFindNewElement() {
    return findNewAnnotationElementIn(getDeclaringAnnotation(), getElementName());
  }

  @Override
  public IMetaValue getMetaValue() {
    return MetaValueFactory.createNull();
  }

  @Override
  public AnnotationSpi getDeclaringAnnotation() {
    return m_owner;
  }

  @Override
  public boolean isDefaultValue() {
    return m_syntheticDefaultValue;
  }

  @Override
  public String getElementName() {
    return m_name;
  }

  @Override
  public ISourceRange getSourceOfExpression() {
    return null;
  }

  @Override
  public ISourceRange getSource() {
    return null;
  }
}
