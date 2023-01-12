/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.fixture;

/**
 * <h3>{@link ClassWithUnicode}</h3>
 *
 * @since 9.0.0
 */
public class ClassWithUnicode {

  public static final String UNICODE = "☂✌☀❀☺☺☺☺☺☺";

  /**
   * ઓઓઊઊઊઊ
   */
  public static String getValue() {
    return UNICODE;
  }
}
