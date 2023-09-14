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

import java.util.Arrays;

import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * Implementation for <a href="https://www.rfc-editor.org/rfc/rfc6901">JavaScript Object Notation (JSON)
 * Pointer</a>.<br>
 * See {@link #compile(CharSequence)} for examples on how to use it.
 */
public final class JsonPointer {

  private final String[] m_tokens; // may be null
  private final CharSequence m_raw;

  private JsonPointer(String[] tokens, CharSequence pointer) {
    m_tokens = tokens;
    m_raw = pointer;
  }

  /**
   * Creates a new pointer based on the given path.<br>
   * <br>
   * Examples:
   * <ul>
   * <li>/objectAttrib/subObjectAttrib/attribute</li>
   * <li>/arrayAttribute/1/subArray/0/subSubArray/2</li>
   * <li>/obj/m~0n (tilde needs to be escaped: ~0 = ~, so the attributes name is "m~n")</li>
   * <li>/obj/m~1n (slash needs to be escaped: ~1 = /, so the attributes name is "m/n")</li>
   * <li>null (points to the root)</li>
   * </ul>
   * 
   * @param pointer
   *          The pointer path. It points to the root element of the json structure if {@code null}.
   * @return The compiled {@link JsonPointer}
   */
  public static JsonPointer compile(CharSequence pointer) {
    if (Strings.isEmpty(pointer)) {
      return new JsonPointer(null, pointer);
    }

    var tokens = CoreUtils.PATH_SEGMENT_SPLIT_PATTERN.splitAsStream(pointer)
        .map(JsonPointer::decode)
        .toArray(String[]::new);
    if (!Strings.isEmpty(tokens[0])) {
      throw new SdkException("A non-empty JSON Pointer must begin with a slash ('/').");
    }
    return new JsonPointer(tokens, pointer);
  }

  /**
   * Adapter for generic json-structure like elements.
   */
  public interface IJsonPointerElement {
    int arrayLength();

    boolean isObject();

    IJsonPointerElement element(String name);

    IJsonPointerElement element(int index);
  }

  public IJsonPointerElement find(IJsonPointerElement start) {
    var result = start;
    if (m_tokens == null || m_tokens.length < 2) {
      return result;
    }

    var numSegments = m_tokens.length;
    for (var i = 1; i < numSegments; i++) { // Start with index 1, skipping the root token
      if (result.isObject()) {
        result = result.element(m_tokens[i]);
        if (result == null) {
          return null; // path not found
        }
      }
      else {
        var arrayLen = result.arrayLength();
        if (arrayLen > 0) {
          var index = getIndex(m_tokens[i]);
          if (index > arrayLen) {
            return null; // path not found
          }
          result = result.element(index);
        }
        else {
          return null; // path not found
        }
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return Arrays.equals(m_tokens, ((JsonPointer) o).m_tokens);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(m_tokens);
  }

  @Override
  public String toString() {
    if (m_raw == null) {
      return "null";
    }
    return m_raw.toString();
  }
}
