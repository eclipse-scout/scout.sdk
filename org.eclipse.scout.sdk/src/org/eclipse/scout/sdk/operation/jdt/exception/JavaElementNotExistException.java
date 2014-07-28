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
package org.eclipse.scout.sdk.operation.jdt.exception;

/**
 * <h3>{@link JavaElementNotExistException}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 26.02.2013
 */
public class JavaElementNotExistException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  /**
   *
   */
  public JavaElementNotExistException() {
    super();
  }

  /**
   * @param message
   * @param cause
   */
  public JavaElementNotExistException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param s
   */
  public JavaElementNotExistException(String s) {
    super(s);
  }

  /**
   * @param cause
   */
  public JavaElementNotExistException(Throwable cause) {
    super(cause);
  }

}
