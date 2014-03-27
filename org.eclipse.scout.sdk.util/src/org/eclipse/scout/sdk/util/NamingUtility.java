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
package org.eclipse.scout.sdk.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;

/**
 * <h3>{@link NamingUtility}</h3> ...
 *
 * @author Andreas Hoegger
 * @since 1.0.8 24.11.2010
 */
public final class NamingUtility {

  private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("[^abcdefghijklmnopqrstuvwxyz0123456789]");
  private static final Object LOCK = new Object();
  private static Set<String> javaKeyWords = null;

  private NamingUtility() {
  }

  public static String ensureValidParameterName(String parameterName) {
    if (isReservedJavaKeyword(parameterName)) {
      return parameterName + "Value";
    }
    return parameterName;
  }

  public static Set<String> getJavaKeyWords() {
    if (javaKeyWords == null) {
      synchronized (LOCK) {
        if (javaKeyWords == null) {
          String[] keyWords = new String[]{"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum",
              "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected",
              "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "false", "null", "true"};
          HashSet<String> tmp = new HashSet<String>(keyWords.length);
          for (String s : keyWords) {
            tmp.add(s);
          }
          javaKeyWords = Collections.unmodifiableSet(tmp);
        }
      }
    }
    return javaKeyWords;
  }

  /**
   * @return Returns <code>true</code> if the given word is a reserved java keyword. Otherwise <code>false</code>.
   * @throws NullPointerException
   *           if the given word is <code>null</code>.
   * @since 3.8.3
   */
  public static boolean isReservedJavaKeyword(String word) {
    return getJavaKeyWords().contains(word.toLowerCase());
  }

  /**
   * ensures the given java name starts with a lower case character.
   *
   * @param name
   *          The name to handle.
   * @return null if the input is null, an empty string if the given string is empty or only contains white spaces.
   *         Otherwise the input string is returned with the first character modified to lower case.
   */
  public static String ensureStartWithLowerCase(String name) {
    if (name == null) {
      return null;
    }
    name = name.trim();
    if (name.length() == 0) {
      return name;
    }

    StringBuilder sb = new StringBuilder(name.length());
    sb.append(Character.toLowerCase(name.charAt(0)));
    if (name.length() > 1) {
      sb.append(name.substring(1));
    }
    return sb.toString();
  }

  /**
   * ensures the given java name starts with an upper case character.
   *
   * @param name
   *          The name to handle.
   * @return null if the input is null, an empty string if the given string is empty or only contains white spaces.
   *         Otherwise the input string is returned with the first character modified to upper case.
   */
  public static String ensureStartWithUpperCase(String name) {
    if (name == null) {
      return null;
    }
    name = name.trim();
    if (name.length() == 0) {
      return name;
    }
    StringBuilder sb = new StringBuilder(name.length());
    sb.append(Character.toUpperCase(name.charAt(0)));
    if (name.length() > 1) {
      sb.append(name.substring(1));
    }
    return sb.toString();
  }

  public static String toJavaCamelCase(String input) {
    return toJavaCamelCase(input, true);
  }

  /**
   * Method calculates the levenshtein distance, also known as string edit distance.
   *
   * @param s1
   *          The first string.
   * @param s2
   *          The second string.
   * @return Amount of edit operations to transform s1 into s2.
   */
  public static int stringDistance(String s1, String s2) {
    if (s1 == null || s2 == null || CompareUtility.equals(s1, s2)) {
      return 0;
    }

    int l1 = s1.length();
    int l2 = s2.length();

    if (l1 == 0) {
      return l2;
    }
    else if (l2 == 0) {
      return l1;
    }

    int col0[] = new int[l1 + 1];
    int col1[] = new int[l1 + 1];
    int col[];

    // indexes into strings s1 and s2
    int i;
    int j;
    char jth;
    int cost;

    for (i = 0; i <= l1; i++) {
      col0[i] = i;
    }

    for (j = 1; j <= l2; j++) {
      jth = s2.charAt(j - 1);
      col1[0] = j;

      for (i = 1; i <= l1; i++) {
        cost = s1.charAt(i - 1) == jth ? 0 : 1;
        col1[i] = Math.min(Math.min(col1[i - 1] + 1, col0[i] + 1), col0[i - 1] + cost);
      }

      col = col0;
      col0 = col1;
      col1 = col;
    }

    return col0[l1];
  }

  /**
   * converts the given input string into a valid java camel case name.<br>
   *
   * @param input
   * @param lowerCaseFirstToken
   *          if true, the first token uses lower case as e.g. used for method names.
   * @return the camel case string.
   */
  public static String toJavaCamelCase(String input, boolean lowerCaseFirstToken) {
    if (!StringUtility.hasText(input)) {
      return null;
    }
    StringBuilder camel = new StringBuilder(input.length());
    String[] tokens = CAMEL_CASE_PATTERN.split(input.toLowerCase());
    for (int i = 0; i < tokens.length; i++) {
      String t = tokens[i];
      if (StringUtility.hasText(t)) {
        if (i == 0 && lowerCaseFirstToken) {
          camel.append(t);
        }
        else {
          camel.append(ensureStartWithUpperCase(t));
        }
      }
    }
    return camel.toString();
  }
}
