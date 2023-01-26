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

import static org.eclipse.scout.sdk.core.typescript.TypeScriptTypes.isPrimitive;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TypeScriptTypesTest {

  @Test
  public void testIsPrimitive() {
    assertFalse(isPrimitive(null));
    assertTrue(isPrimitive(TypeScriptTypes._string));
    assertTrue(isPrimitive(TypeScriptTypes._number));
    assertTrue(isPrimitive(TypeScriptTypes._bigint));
    assertTrue(isPrimitive(TypeScriptTypes._boolean));
    assertTrue(isPrimitive(TypeScriptTypes._symbol));
    assertTrue(isPrimitive(TypeScriptTypes._undefined));
    assertTrue(isPrimitive(TypeScriptTypes._null));
    assertFalse(isPrimitive(TypeScriptTypes._object));
    assertFalse(isPrimitive(TypeScriptTypes._any));
    assertFalse(isPrimitive("FancyType"));
  }
}
