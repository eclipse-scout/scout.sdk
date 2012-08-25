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
package org.eclipse.scout.sdk.operation.annotation;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.RuntimeClasses;

/**
 *
 */
public class OrderAnnotationCreateOperation extends AnnotationCreateOperation {

  public OrderAnnotationCreateOperation(IMember annotationOwner, double orderNr) {
    this(annotationOwner, orderNr, false);
  }

  /**
   * @param annotationOwner
   * @param replaceExisting
   */
  public OrderAnnotationCreateOperation(IMember annotationOwner, double orderNr, boolean replaceExisting) {
    super(annotationOwner, Signature.createTypeSignature(RuntimeClasses.Order, true), replaceExisting);
    addParameter("" + orderNr);
  }

}
