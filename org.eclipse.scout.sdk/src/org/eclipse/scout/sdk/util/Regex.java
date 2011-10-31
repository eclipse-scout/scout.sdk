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
import org.eclipse.scout.sdk.RuntimeClasses;

public class Regex {
  //public static final Pattern REGEX_ORDER_ANNOTATION = Pattern.compile("^[^;{]*@Order\\s*\\(\\s*([^)f]+)f*\\s*\\)");
  public static final Pattern REGEX_WELLFORMD_JAVAFIELD = Pattern.compile("\\b[A-Z][a-zA-Z0-9_]{0,200}\\b");
  public static final Pattern REGEX_WELLFORMED_PROPERTY = Pattern.compile("\\b[a-zA-Z0-9_]{0,200}\\b");
  //public static final Pattern REGEX_TYPE_CLOSING_CLASS_BRACKET = Pattern.compile("}\\s*\\z");
  public static final Pattern REGEX_JAVAFIELD = Pattern.compile("\\b[A-Za-z][a-zA-Z0-9_]{0,200}\\b");
  //public static final String REGEX_PROPERTY_BEAN_FIELD_MATCH = "^.*(m_)?#0#$";
  public static final Pattern REGEX_PROPERTY_METHOD_TRIM = Pattern.compile("^getConfigured(.*)$");

  //public static final Pattern REGEX_METHOD_RETURN_NLS_TEXT_DEFAULT = Pattern.compile("\\{\\s*return\\s*ScoutTexts\\.get\\s*\\(\\s*null\\s*\\)\\s*\\;\\s*\\}", Pattern.DOTALL);
  public static final Pattern REGEX_METHOD_RETURN_NON_NLS_TEXT = Pattern.compile("\\{\\s*return\\s*\"(.*)\"\\s*\\;\\s*\\}", Pattern.DOTALL);
  public static final Pattern REGEX_METHOD_RETURN_NLS_TEXT = Pattern.compile("\\{\\s*return\\s*(" + RuntimeClasses.TEXTS + "|TEXTS|Texts|ScoutTexts)\\.get\\(\\s*\\\"([^\\\"]*)\\\"\\s*\\)\\s*\\;\\s*\\}", Pattern.DOTALL);
  //public static final Pattern REGEX_METHOD_RETURN_NLS_TEXT_WITH_KEY = Pattern.compile("\\{\\s*return\\s*(" + RuntimeClasses.TEXTS + "|TEXTS|Texts|ScoutTexts)\\.get\\(\\s*(" + RuntimeClasses.TEXTS + "|TEXTS|Texts|ScoutTexts)\\.([^\\)\\s]*)\\)\\s*\\;\\s*\\}", Pattern.DOTALL);

  //public static final Pattern REGEX_METHOD_RETURN_TYPE = Pattern.compile("return\\s*(.*)\\.class\\s*");
  //public static final Pattern REGEX_METHOD_RETURN_BOOLEAN = Pattern.compile("return\\s*(true|false)\\s*");
  public static final Pattern REGEX_METHOD_CLASS_TYPE_OCCURRENCES = Pattern.compile("([a-zA-Z0-9_.$]+)\\.class", Pattern.DOTALL);
  public static final Pattern REGEX_METHOD_NEW_TYPE_OCCURRENCES = Pattern.compile("\\s*new\\s*([^\\(]*)\\([^\\)]*\\)\\s*", Pattern.DOTALL);
  //public static final Pattern REGEX_METHOD_CONTENT = Pattern.compile("\\A[^\\{]*\\{(.*)\\}\\Z");
  public static final Pattern REGEX_METHOD_DEFINITION = Pattern.compile("[ \\t]*(public|protected|private)?\\s*(static)?\\s*(void|[^\\s]*)\\s*[^\\s\\(]*\\s*\\([^\\)]*\\)\\s*\\{", Pattern.DOTALL);

  public static final Pattern REGEX_PROPERTY_METHOD_REPRESENTER_VALUE = Pattern.compile("\\{\\s*return\\s*([^\\;]*)\\s*\\;.*\\}", Pattern.DOTALL);
  public static final Pattern REGEX_PROPERTY_METHOD_REPRESENTER_BOOLEAN = Pattern.compile("\\{\\s*return\\s*(true|false)\\s*\\;\\s*\\}", Pattern.DOTALL);

  //public static final String REGEX_FIELD_GETTER_METHOD = "\\s*(public|private|protected)?\\s*(#0#)\\s*get#0#\\s*\\(\\s*\\).*";

  public static final Pattern REGEX_ICON_NAME = Pattern.compile("(\\\")?(([^\\\\/]*(/|\\\\))*)([^.\\\"]*)(\\.)?([^\"]*)(\\\")?");
  public static final Pattern REGEX_ICON_NAME_FIELD_SOURCE = Pattern.compile("\\=\\s*\\\"([^\"]*)\\\"");

  public static final Pattern REGEX_FIELD_DECLARATION = Pattern.compile("\\s*(@Deprecated){0,1}\\s*(public|private|protected)?\\s*(final\\s*|static\\s*|transient\\s*|volatile\\s*){0,4}([a-zA-Z0-9_]*(<([a-zA-Z0-9_]|(,\\s*))*>)?)\\s*([a-zA-Z0-9_]*)(\\s*=\\s*(.*))?\\s*;.*", Pattern.DOTALL);

  /**
   * @param fieldSource
   *          something like public static String ICON_A = "aIcon";
   * @return aIcon
   */
  public static String getIconSimpleNameFromFieldSource(String fieldSource) {
    if (!StringUtility.isNullOrEmpty(fieldSource)) {
      Matcher matcher = REGEX_ICON_NAME_FIELD_SOURCE.matcher(fieldSource);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return fieldSource;
  }

  public static String getIconSimpleName(String iconName) {
    if (iconName != null) {
      Matcher matcher = REGEX_ICON_NAME.matcher(iconName);
      if (matcher.find()) {
        return matcher.group(5);
      }
    }
    return iconName;
  }

  /**
   * A regex may contain several placeholders marked as #0# ... #xx#. This method is used to replace
   * the placeholders.
   * 
   * @param regex
   *          the regex containing the exactly same amount of placeholders as the replacements conatins items.
   * @param replacements
   *          the array of replacements
   * @return replaced regex expression.
   */
  public static String replace(String regex, String... replacements) {
    StringBuilder sb = new StringBuilder(regex);
    for (int i = 0; i < replacements.length; i++) {
      int index = 0;
      String placeholder = "#" + i + "#";
      while ((index = sb.indexOf(placeholder, index)) >= 0) {
        sb.replace(index, index + placeholder.length(), replacements[i]);
      }
    }
    return sb.toString();
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
    Matcher matcher = REGEX_FIELD_DECLARATION.matcher(fieldDeclaration);
    if (matcher.matches()) {
      String match = matcher.group(10);
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
