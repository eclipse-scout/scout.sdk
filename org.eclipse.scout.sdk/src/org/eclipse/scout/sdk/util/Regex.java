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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;

public class Regex {
  public static final String REGEX_ORDER_ANNOTATION = "^[^;{]*@Order\\s*\\(\\s*([^)f]+)f*\\s*\\)";
  public static final String REGEX_WELLFORMD_JAVAFIELD = "\\b[A-Z][a-zA-Z0-9_]{0,200}\\b";
  public static final String REGEX_WELLFORMED_PROPERTY = "\\b[a-zA-Z0-9_]{0,200}\\b";
  public static final String REGEX_TYPE_CLOSING_CLASS_BRACKET = "}\\s*\\z";
  public static final String REGEX_JAVAFIELD = "\\b[A-Za-z][a-zA-Z0-9_]{0,200}\\b";
  public static final String REGEX_PROPERTY_BEAN_FIELD_MATCH = "^.*(m_)?#0#$";
  public static final String REGEX_PROPERTY_METHOD_TRIM = "^getConfigured(.*)$";

  public static final String REGEX_METHOD_RETURN_NLS_TEXT_DEFAULT = "\\{\\s*return\\s*ScoutTexts\\.get\\s*\\(\\s*null\\s*\\)\\s*\\;\\s*\\}";
  public static final String REGEX_METHOD_RETURN_NON_NLS_TEXT = "\\{\\s*return\\s*\"(.*)\"\\s*\\;\\s*\\}";
  public static final String REGEX_METHOD_RETURN_NLS_TEXT = "\\{\\s*return\\s*#0#\\.get\\(\\s*\\\"([^\\\"]*)\\\"\\s*\\)\\s*\\;\\s*\\}";
  public static final String REGEX_METHOD_RETURN_NLS_TEXT_WITH_KEY = "\\{\\s*return\\s*#0#\\.get\\(\\s*#0#\\.([^\\)\\s]*)\\)\\s*\\;\\s*\\}";

  public static final String REGEX_METHOD_RETURN_TYPE = "return\\s*(.*)\\.class\\s*";
  public static final String REGEX_METHOD_RETURN_BOOLEAN = "return\\s*(true|false)\\s*";
  public static final String REGEX_METHOD_CLASS_TYPE_OCCURRENCES = "([a-zA-Z0-9_.$]+)\\.class";
  public static final String REGEX_METHOD_NEW_TYPE_OCCURRENCES = "\\s*new\\s*([^\\(]*)\\([^\\)]*\\)\\s*";
  public static final String REGEX_METHOD_CONTENT = "\\A[^\\{]*\\{(.*)\\}\\Z";
  public static final String REGEX_METHOD_DEFINITION = "[ \\t]*(public|protected|private)?\\s*(static)?\\s*(void|[^\\s]*)\\s*[^\\s\\(]*\\s*\\([^\\)]*\\)\\s*\\{";

  public static final String REGEX_PROPERTY_METHOD_REPRESENTER_VALUE = "\\{\\s*return\\s*([^\\;]*)\\s*\\;.*\\}";
  public static final String REGEX_PROPERTY_METHOD_REPRESENTER_BOOLEAN = "\\{\\s*return\\s*(true|false)\\s*\\;\\s*\\}";

  public static final String REGEX_FIELD_GETTER_METHOD = "\\s*(public|private|protected)?\\s*(#0#)\\s*get#0#\\s*\\(\\s*\\).*";

  public static final String REGEX_ICON_NAME = "(\\\")?(([^\\\\/]*(/|\\\\))*)([^.\\\"]*)(\\.)?([^\"]*)(\\\")?";

  public static final String REGEX_FIELD_DECLARATION = "\\s*(public|private|protected)?\\s*(final\\s*|static\\s*|transient\\s*|volatile\\s*){0,4}([a-zA-Z0-9_]*(<([a-zA-Z0-9_]|(,\\s*))*>)?)\\s*([a-zA-Z0-9_]*)(\\s*=\\s*(.*))?\\s*;.*";

  /**
   * @param fieldSource
   *          something like public static String ICON_A = "aIcon";
   * @return aIcon
   */
  public static String getIconSimpleNameFromFieldSource(String fieldSource) {
    String regex = "\\=\\s*\\\"([^\"]*)\\\"";
    if (!StringUtility.isNullOrEmpty(fieldSource)) {
      Matcher matcher = Pattern.compile(regex).matcher(fieldSource);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return fieldSource;
  }

  public static String getIconSimpleName(String iconName) {
    if (iconName != null) {
      Matcher matcher = Pattern.compile(REGEX_ICON_NAME).matcher(iconName);
      if (matcher.find()) {
        return matcher.group(5);
      }
    }
    return iconName;
  }

  /**
   * A regex may contain several placeholders marked as #0# ... #x#. This method is used to replace
   * the placeholders.
   * 
   * @param regex
   *          the regex containing the exactly same amount of palceholders as the replacements items conatins.
   * @param replacements
   *          the array of replacements
   * @return replaced regex expression.
   */
  public static String replace(String regex, String... replacements) {
    String copy = new String(regex);
    for (int i = 0; i < replacements.length; i++) {
      if (copy.contains("#" + i + "#")) {
        copy = copy.replaceAll("#" + i + "#", replacements[i]);
      }
      else {
        ScoutSdk.logWarning("could not replace: " + "#" + i + "#" + " in regex: " + regex);
      }
    }
    return copy;
  }

  public static String createRegex(String searchPattern) {
    String regex = "";
    for (String part : searchPattern.split("\\*")) {
      regex = regex + (part + ".*");
    }
    return regex;
  }

  public static String quoteRegexSpecialCharacters(String input) {
    input = input.replace("\\", "\\\\");
    input = input.replace(".", "\\.");
    input = input.replace("+", "\\+");
    input = input.replace("?", "\\?");
    input = input.replace("^", "\\^");
    input = input.replace("$", "\\$");
    input = input.replace("[", "\\[");
    input = input.replace("]", "\\]");
    input = input.replace("(", "\\(");
    input = input.replace(")", "\\)");
    input = input.replace("{", "\\{");
    input = input.replace("}", "\\}");
    input = input.replace("*", "\\*");
    input = input.replace("|", "\\|");
    return input;
  }

  /**
   * Extracts the right-hand side of a field declaration.
   * 
   * @param fieldDeclaration
   * @return Returns the right-hand side of the field declaration or <code>null</code>.
   * @throws IllegalArgumentException
   *           if the given fieldDeclaration is invalid
   */
  public static String getFieldDeclarationRightHandSide(String fieldDeclaration) throws IllegalArgumentException {
    Pattern pattern = Pattern.compile(Regex.REGEX_FIELD_DECLARATION, Pattern.DOTALL);
    Matcher matcher = pattern.matcher(fieldDeclaration);
    if (matcher.matches()) {
      String match = matcher.group(9);
      if (match != null) {
        match = match.trim();
      }
      return match;
    }
    else {
      throw new IllegalArgumentException();
    }
  }
}
