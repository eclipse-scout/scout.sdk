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
package org.eclipse.scout.sdk.operation.method;

import org.eclipse.jdt.core.IType;

/**
 *
 */
public class ConstructorCreateOperation extends MethodCreateOperation {

  /**
   * @param declaringType
   */
  public ConstructorCreateOperation(IType declaringType) {
    this(declaringType, false);
  }

  /**
   * @param declaringType
   * @param formatSource
   */
  public ConstructorCreateOperation(IType declaringType, boolean formatSource) {
    super(declaringType, declaringType.getElementName(), null, formatSource);
  }

}
