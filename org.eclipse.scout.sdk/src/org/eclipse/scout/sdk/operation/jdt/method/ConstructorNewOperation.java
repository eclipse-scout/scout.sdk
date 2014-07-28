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
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;

/**
 * <h3>{@link ConstructorNewOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 13.03.2013
 */
public class ConstructorNewOperation extends AbstractMethodNewOperation {

  /**
   * @param sourceBuilder
   * @param declaringType
   */
  public ConstructorNewOperation(IMethodSourceBuilder sourceBuilder, IType declaringType) {
    super(sourceBuilder, declaringType);
  }

  /**
   * @param methodName
   * @param declaringType
   */
  public ConstructorNewOperation(IType declaringType) {
    super(declaringType.getElementName(), declaringType);
  }

  @Override
  public String getOperationName() {
    return "Create constructor '" + getElementName() + "'...";
  }

  @Override
  public void validate() {
    if (!CompareUtility.equals(getElementName(), getDeclaringType().getElementName())) {
      throw new IllegalArgumentException("Constuctor and declaring type must have the same name!");
    }
    super.validate();
  }

}
