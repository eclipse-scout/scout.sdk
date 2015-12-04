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
package org.eclipse.scout.sdk.s2e.nls.resource;

/**
 * @see AbstractTranslationResource
 */
public class InvalidTranslationResourceException extends Exception {

  private static final long serialVersionUID = 460416914259029013L;

  public InvalidTranslationResourceException(String message) {
    super(message);
  }
}
