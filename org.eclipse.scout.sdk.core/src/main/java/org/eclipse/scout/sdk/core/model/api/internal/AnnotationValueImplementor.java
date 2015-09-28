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
package org.eclipse.scout.sdk.core.model.api.internal;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.spi.AnnotationValueSpi;

/**
 *
 */
public class AnnotationValueImplementor extends AbstractJavaElementImplementor<AnnotationValueSpi>implements IAnnotationValue {

  public AnnotationValueImplementor(AnnotationValueSpi spi) {
    super(spi);
  }

  @Override
  public IMetaValue metaValue() {
    return m_spi.getMetaValue();
  }

  @Override
  public IAnnotation declaringAnnotation() {
    return m_spi.getDeclaringAnnotation().wrap();
  }

  @Override
  public boolean isDefaultValue() {
    return m_spi.isDefaultValue();
  }

  @Override
  public ISourceRange sourceOfExpression() {
    return m_spi.getSourceOfExpression();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }

}
