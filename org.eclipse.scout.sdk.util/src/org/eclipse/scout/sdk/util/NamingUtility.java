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

import org.eclipse.scout.commons.StringUtility;

/**
 * <h3>{@link NamingUtility}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 24.11.2010
 */
public final class NamingUtility {

  private NamingUtility() {
  }

  public static String getSimpleName(String qualifiedName) {
    int i = qualifiedName.lastIndexOf(".");
    if (i >= 0) {
      return qualifiedName.substring(i + 1);
    }
    else {
      return qualifiedName;
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

}
