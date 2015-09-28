/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.MethodParameterSpi;
import org.eclipse.scout.sdk.core.model.sugar.AnnotationQuery;

/**
 * <h3>{@link MethodParameterImplementor}</h3>
 *
 * @author Ivan Motsch
 * @since 3.8.0 06.12.2012
 */
public class MethodParameterImplementor extends AbstractAnnotatableImplementor<MethodParameterSpi>implements IMethodParameter {

  public MethodParameterImplementor(MethodParameterSpi spi) {
    super(spi);
  }

  @Override
  public IMethod declaringMethod() {
    return m_spi.getDeclaringMethod().wrap();
  }

  @Override
  public IType dataType() {
    return JavaEnvironmentImplementor.wrapType(m_spi.getDataType());
  }

  @Override
  public int flags() {
    return m_spi.getFlags();
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
    return new AnnotationQuery<>(declaringMethod().declaringType(), m_spi);
  }

}
