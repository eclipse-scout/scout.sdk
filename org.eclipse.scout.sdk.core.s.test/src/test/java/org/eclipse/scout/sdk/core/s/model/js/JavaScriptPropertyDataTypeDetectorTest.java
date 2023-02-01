/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import static org.eclipse.scout.sdk.core.s.model.js.JavaScriptPropertyDataTypeDetector.AbstractPropertyOverride.parseMethodCallWithStringArguments;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class JavaScriptPropertyDataTypeDetectorTest {
  @Test
  public void testParseMethodCallWithStringArguments() {
    assertParsedMethodCallStringArgs(List.of(), "a; b; c;");
    assertParsedMethodCallStringArgs(List.of("abc", "def"), "a; this._addWidgetProperties('abc'); this._addWidgetProperties('def'); b;");
    assertParsedMethodCallStringArgs(List.of("abc", "def", "ghi"), "a; this._addWidgetProperties(['abc']); this._addWidgetProperties(['def', 'ghi']); b;");
    assertParsedMethodCallStringArgs(List.of("abc", "def", "ghi"), """
        a;
        this._addWidgetProperties(["abc"]);
        this._addWidgetProperties(["def", "ghi"]);
        b;
        """);
  }

  protected static void assertParsedMethodCallStringArgs(List<String> expectedElements, CharSequence source) {
    var pattern = JavaScriptPropertyDataTypeDetector.WidgetPropertyOverride.REGEX_WIDGET_PROPERTY_TYPE;
    var result = parseMethodCallWithStringArguments(source, pattern).toList();
    assertEquals(expectedElements, result);
  }
}
