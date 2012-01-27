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
package org.eclipse.scout.nls.sdk.model.workspace.translationResource;

/**
 * @see AbstractTranslationResource
 */
public class InvalidTranslationResourceException extends Exception {

  private static final long serialVersionUID = 460416914259029013L;

  public InvalidTranslationResourceException(String message) {
    super(message);
  }
}
