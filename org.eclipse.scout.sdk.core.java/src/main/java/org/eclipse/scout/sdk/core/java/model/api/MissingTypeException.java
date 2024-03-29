/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

/**
 * <h3>{@link MissingTypeException}</h3>
 *
 * @since 5.1.0
 */
public class MissingTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MissingTypeException(String s) {
    super(s);
  }

  public MissingTypeException(String s, Throwable t) {
    super(s, t);
  }
}
