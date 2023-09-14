/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api;

import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * JavaScript and TypeScript modifiers with the corresponding source keyword.
 */
public enum Modifier {
  DYNAMIC("dynamic"),
  NATIVE("native"),
  OVERRIDE("override"),
  STATIC("static"),
  PROTO("proto"),
  FINAL("final"),
  VIRTUAL("virtual"),
  READONLY("readonly"),
  DECLARE("declare"),
  CONST("const"),
  ASYNC("async"),
  ABSTRACT("abstract"),
  EXPORT("export"),
  GENERATOR("*"),
  GET("get"),
  SET("set"),
  IN("in"),
  OUT("out");

  public final String keyword;

  Modifier(String keyword) {
    this.keyword = Ensure.notNull(keyword);
  }

  /**
   * @return The keyword of this modifier as it occurs in the source code.
   */
  public String keyword() {
    return keyword;
  }
}
