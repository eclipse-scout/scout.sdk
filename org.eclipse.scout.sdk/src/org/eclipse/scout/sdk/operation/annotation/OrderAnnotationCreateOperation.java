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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;

/**
 *
 */
public class OrderAnnotationCreateOperation extends AnnotationCreateOperation {
  public OrderAnnotationCreateOperation(IMember annotationOwner, double orderNr) {
    super(annotationOwner, SignatureCache.createTypeSignature(RuntimeClasses.Order));
    addParameter("" + orderNr);
  }
}
