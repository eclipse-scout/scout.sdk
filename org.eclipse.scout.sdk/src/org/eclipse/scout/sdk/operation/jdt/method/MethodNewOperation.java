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
package org.eclipse.scout.sdk.operation.jdt.method;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;

/**
 * <h3>{@link MethodNewOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 05.12.2012
 */
public class MethodNewOperation extends AbstractMethodNewOperation {

  /**
   * @param methodName
   * @param declaringType
   */
  public MethodNewOperation(String methodName, IType declaringType) {
    super(methodName, declaringType);
  }

  /**
   * @param methodName
   * @param declaringType
   * @param formatSource
   */
  public MethodNewOperation(String methodName, IType declaringType, boolean formatSource) {
    super(methodName, declaringType, formatSource);
  }

  /**
   * @param sourceBuilder
   * @param declaringType
   */
  public MethodNewOperation(IMethodSourceBuilder sourceBuilder, IType declaringType) {
    super(sourceBuilder, declaringType);
  }

  /**
   * @param sourceBuilder
   * @param declaringType
   * @param formatSource
   */
  public MethodNewOperation(IMethodSourceBuilder sourceBuilder, IType declaringType, boolean formatSource) {
    super(sourceBuilder, declaringType, formatSource);
  }

  /**
   * @param returnTypeSignature
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#setReturnTypeSignature(java.lang.String)
   */
  public void setReturnTypeSignature(String returnTypeSignature) {
    getSourceBuilder().setReturnTypeSignature(returnTypeSignature);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#getReturnTypeSignature()
   */
  public String getReturnTypeSignature() {
    return getSourceBuilder().getReturnTypeSignature();
  }

}
