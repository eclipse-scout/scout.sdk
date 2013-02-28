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

import java.text.ParseException;
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

  private final static Pattern CAMEL_CASE_PATTERN = Pattern.compile("[^abcdefghijklmnopqrstuvwxyz0123456789]");

  private NamingUtility() {
  }

  public static String getSimpleName(String qualifiedName) {
    int i = qualifiedName.lastIndexOf('.');
    if (i >= 0) {
      return qualifiedName.substring(i + 1);
    }
    else {
      return qualifiedName;
    }
  }

  public static String getPackage(String qualifiedClassName) {
    int i = qualifiedClassName.lastIndexOf('.');
    if (i >= 0) {
      return qualifiedClassName.substring(0, i);
    }
    else {
      return qualifiedClassName;
    }
  }

  public static String parseJavaTypeName(String name, String text, String[] discouragedSuffixes) throws ParseException {
    if (text == null) text = "";
    if (text.length() <= 1) {
      return null;
    }
    else if (!Character.isJavaIdentifierStart(text.charAt(0))) {
      throw new ParseException(name + " must start with upper case A-Z", 0);
    }
    else if (!Character.isUpperCase(text.charAt(0))) {
      text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
    else {
      String textLow = text.toLowerCase();
      if (discouragedSuffixes != null) {
        for (int i = 0; i < discouragedSuffixes.length; i++) {
          if (textLow.endsWith(discouragedSuffixes[i].toLowerCase())) {
            throw new ParseException(name + " must not end with '" + discouragedSuffixes[i] + "'", 0);
          }
        }
      }
      for (int i = 1; i < text.length(); i++) {
        if (!Character.isJavaIdentifierPart(text.charAt(i))) {
          throw new ParseException("'" + text + "' is not a valid java name", 0);
        }
      }
    }
    // all checks ok
    return text;
  }

  public static String removeSuffixes(String s, String... suffixes) {
    return StringUtility.removeSuffixes(s, suffixes);
  }

  /**
   * lowercase java-bean name
   */
  public static String toVariableName(String javaName) {
    if (javaName == null || javaName.length() == 0) return null;
    return Character.toLowerCase(javaName.charAt(0)) + javaName.substring(1);
  }

  /**
   * uppercase java-bean name
   */
  public static String toBeanName(String javaName) {
    if (javaName == null || javaName.length() == 0) return null;
    return Character.toUpperCase(javaName.charAt(0)) + javaName.substring(1);
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
          camel.append(Character.toUpperCase(t.charAt(0)));
          camel.append(t.substring(1));
        }
      }
    }
    return camel.toString();
  }
}
