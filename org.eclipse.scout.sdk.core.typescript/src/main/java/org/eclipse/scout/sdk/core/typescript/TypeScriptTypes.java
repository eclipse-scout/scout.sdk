/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript;

public final class TypeScriptTypes {

  public static final String _string = "string";
  public static final String _number = "number";
  public static final String _bigint = "bigint";
  public static final String _boolean = "boolean";
  public static final String _undefined = "undefined";
  public static final String _symbol = "symbol";
  public static final String _null = "null";
  public static final String _object = "object";
  public static final String _any = "any";

  private TypeScriptTypes() {
  }

  /**
   * Checks if the given type name is a JavaScript primitive type.
   * 
   * @param type
   *          The type to check or {@code null}.
   * @return {@code true} if type is string, number, bigint, boolean, undefined, symbol or "null". {@code false} if the
   *         type is {@code null} or none of the mentioned primitives.
   */
  public static boolean isPrimitive(CharSequence type) {
    if (type == null) {
      return false;
    }
    return switch (type.toString()) {
      case _string, _number, _bigint, _boolean, _undefined, _symbol, _null -> true;
      default -> false;
    };
  }
}
