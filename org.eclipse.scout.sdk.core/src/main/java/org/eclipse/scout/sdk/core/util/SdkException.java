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
package org.eclipse.scout.sdk.core.util;

/**
 * <h3>{@link SdkException}</h3>
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class SdkException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SdkException(String message, Throwable cause) {
    super(message, cause);
  }

  public SdkException(String message) {
    super(message);
  }

  public SdkException(Throwable cause) {
    super(cause);
  }
}
