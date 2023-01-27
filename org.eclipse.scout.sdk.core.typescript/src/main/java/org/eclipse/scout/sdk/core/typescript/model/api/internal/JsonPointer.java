/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.internal;

import org.eclipse.scout.sdk.core.typescript.model.api.IConstantValue;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

public final class JsonPointer {

  private final String[] m_tokens;

  private JsonPointer(String[] tokens) {
    m_tokens = tokens;
  }

  public static JsonPointer compile(CharSequence pointer) {
    if (Strings.isEmpty(pointer)) {
      return new JsonPointer(null);
    }

    var tokens = CoreUtils.PATH_SEGMENT_SPLIT_PATTERN.splitAsStream(pointer)
        .map(JsonPointer::decode)
        .toArray(String[]::new);
    if (!Strings.isEmpty(tokens[0])) {
      throw new SdkException("A non-empty JSON Pointer must begin with a slash ('/').");
    }
    return new JsonPointer(tokens);
  }

  public IConstantValue find(IConstantValue start) {
    var result = start;
    if (m_tokens == null || m_tokens.length < 2) {
      return result;
    }

    var numSegments = m_tokens.length;
    for (var i = 1; i < numSegments; i++) { // Start with index 1, skipping the root token
      switch (result.type()) {
        case ObjectLiteral:
          var object = result.asObjectLiteral().orElseThrow();
          result = object.property(m_tokens[i]).orElse(null);
          if (result == null) {
            return null; // path not found
          }
          break;
        case Array:
          var index = getIndex(m_tokens[i]);
          var array = result.convertTo(IConstantValue[].class).orElseThrow();
          if (index > array.length) {
            return null; // path not found
          }
          result = array[index];
          break;
        default:
          return null; // path not found
      }
    }
    return result;
  }

  static int getIndex(String token) {
    if (Strings.isBlank(token)) {
      throw new SdkException("Array index format error, was '{}'.", token);
    }
    if ("0".equals(token)) {
      return 0;
    }
    if (token.charAt(0) == '+' || token.charAt(0) == '-') {
      throw new SdkException("Array index format error, was '{}'.", token);
    }
    try {
      return Integer.parseInt(token);
    }
    catch (NumberFormatException ex) {
      throw new SdkException("Illegal integer format, was '{}'.", token);
    }
  }

  @SuppressWarnings("AssignmentToForLoopParameter")
  static String decode(CharSequence escapedToken) {
    var unescapedToken = new StringBuilder(escapedToken.length());
    for (var j = 0; j < escapedToken.length(); j++) {
      var ch = escapedToken.charAt(j);
      if (ch == '~' && j < escapedToken.length() - 1) {
        var ch1 = escapedToken.charAt(j + 1);
        if (ch1 == '0') {
          j++;
        }
        else if (ch1 == '1') {
          ch = '/';
          j++;
        }
      }
      unescapedToken.append(ch);
    }
    return unescapedToken.toString();
  }
}
