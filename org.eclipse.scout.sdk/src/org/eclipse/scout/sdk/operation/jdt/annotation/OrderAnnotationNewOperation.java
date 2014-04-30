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
package org.eclipse.scout.sdk.operation.jdt.annotation;

import org.eclipse.jdt.core.IMember;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.util.signature.SignatureCache;

/**
 *
 */
public class OrderAnnotationNewOperation extends AnnotationNewOperation {
  public OrderAnnotationNewOperation(IMember annotationOwner, double orderNr) {
    super(SignatureCache.createTypeSignature(IRuntimeClasses.Order), annotationOwner);
    addParameter("" + orderNr);
  }
}
