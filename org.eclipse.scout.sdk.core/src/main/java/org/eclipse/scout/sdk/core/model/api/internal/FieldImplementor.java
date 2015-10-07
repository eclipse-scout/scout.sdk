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
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.FieldSpi;
import org.eclipse.scout.sdk.core.model.sugar.AnnotationQuery;

public class FieldImplementor extends AbstractMemberImplementor<FieldSpi> implements IField {

  public FieldImplementor(FieldSpi spi) {
    super(spi);
  }

  @Override
  public IMetaValue constantValue() {
    return m_spi.getConstantValue();
  }

  @Override
  public IType dataType() {
    return JavaEnvironmentImplementor.wrapType(m_spi.getDataType());
  }

  @Override
  public IField originalField() {
    return m_spi.getOriginalField().wrap();
  }

  @Override
  public ISourceRange sourceOfInitializer() {
    return m_spi.getSourceOfInitializer();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }

  //additional convenience methods

  @Override
  public AnnotationQuery<IAnnotation> annotations() {
    return new AnnotationQuery<>(declaringType(), m_spi);
  }

}
