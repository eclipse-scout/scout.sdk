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

import java.util.List;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.ISourceRange;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.spi.MethodSpi;
import org.eclipse.scout.sdk.core.model.sugar.AnnotationQuery;
import org.eclipse.scout.sdk.core.model.sugar.MethodParameterQuery;
import org.eclipse.scout.sdk.core.model.sugar.SuperMethodQuery;

/**
 *
 */
public class MethodImplementor extends AbstractMemberImplementor<MethodSpi> implements IMethod {

  public MethodImplementor(MethodSpi spi) {
    super(spi);
  }

  @Override
  public boolean isConstructor() {
    return m_spi.isConstructor();
  }

  @Override
  public IType returnType() {
    return JavaEnvironmentImplementor.wrapType(m_spi.getReturnType());
  }

  @Override
  public List<IType> exceptionTypes() {
    return new WrappedList<>(m_spi.getExceptionTypes());
  }

  @Override
  public IMethod originalMethod() {
    return m_spi.getOriginalMethod().wrap();
  }

  @Override
  public ISourceRange sourceOfBody() {
    return m_spi.getSourceOfBody();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    JavaModelPrinter.print(this, sb);
    return sb.toString();
  }

  //additional convenience methods

  @Override
  public SuperMethodQuery superMethods() {
    return new SuperMethodQuery(this);
  }

  @Override
  public AnnotationQuery<IAnnotation> annotations() {
    return new AnnotationQuery<>(declaringType(), m_spi);
  }

  @Override
  public MethodParameterQuery parameters() {
    return new MethodParameterQuery(m_spi);
  }

}
