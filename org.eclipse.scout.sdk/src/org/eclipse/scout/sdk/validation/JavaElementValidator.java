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
package org.eclipse.scout.sdk.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;

/**
 * <h3>{@link JavaElementValidator}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 11.03.2012
 */
public final class JavaElementValidator {

  private static Set<String> javaKeyWords = null;
  private final static Object LOCK = new Object();

  private final static Pattern REGEX_PACKAGE_NAME = Pattern.compile("^[0-9a-zA-Z\\.\\_]*$");
  private final static Pattern REGEX_PACKAGE_NAME_START = Pattern.compile("[a-zA-Z]{1}.*$");
  private final static Pattern REGEX_PACKAGE_NAME_END = Pattern.compile("^.*[a-zA-Z]{1}$");
  private final static Pattern REGEX_CONTAINS_UPPER_CASE = Pattern.compile(".*[A-Z].*");

  private JavaElementValidator() {
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

  public static IStatus validatePackageName(String pckName) {
    if (StringUtility.isNullOrEmpty(pckName)) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("DefaultPackageIsDiscouraged"));
    }
    // no double points
    if (pckName.contains("..")) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("PackageNameNotValid"));
    }
    // invalid characters
    if (!REGEX_PACKAGE_NAME.matcher(pckName).matches()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("PackageNameNotValid"));
    }
    // no start and end with number or special characters
    if (!REGEX_PACKAGE_NAME_START.matcher(pckName).matches() || !REGEX_PACKAGE_NAME_END.matcher(pckName).matches()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("PackageNameNotValid"));
    }
    // reserved java keywords
    String jkw = getContainingJavaKeyWord(pckName);
    if (jkw != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("PackageNotContainJavaKeyword", jkw));
    }
    // warn containing upper case characters
    if (REGEX_CONTAINS_UPPER_CASE.matcher(pckName).matches()) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("PackageOnlyLowerCase"));
    }
    return Status.OK_STATUS;
  }

  private static String getContainingJavaKeyWord(String s) {
    for (String keyWord : getJavaKeyWords()) {
      if (s.startsWith(keyWord + ".") || s.endsWith("." + keyWord) || s.contains("." + keyWord + ".")) {
        return keyWord;
      }
    }
    return null;
  }

  public static IStatus validateNewBundleName(String bundleName) {
    // validate name
    if (StringUtility.isNullOrEmpty(bundleName)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("ProjectNameMissing"));
    }
    // no double points
    if (bundleName.contains("..")) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("ProjectNameIsNotValid"));
    }
    // invalid characters
    if (!REGEX_PACKAGE_NAME.matcher(bundleName).matches()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("TheBundleNameContainsInvalidCharacters"));
    }
    // no start and end with number or special characters
    if (!REGEX_PACKAGE_NAME_START.matcher(bundleName).matches() || !REGEX_PACKAGE_NAME_END.matcher(bundleName).matches()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("BundleNameCanNotStartOrEndWithSpecialCharactersOrDigits"));
    }
    // reserved java keywords
    String jkw = getContainingJavaKeyWord(bundleName);
    if (jkw != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("TheProjectNameMayNotContainAReservedJavaKeyword", jkw));
    }
    // already existing bundle name
    if (Platform.getBundle(bundleName) != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("BundleAlreadyExists", bundleName));
    }
    if (ResourcesPlugin.getWorkspace().getRoot().getProject(bundleName).exists()) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("BundleAlreadyExists", bundleName));
    }
    // warn containing upper case characters
    if (REGEX_CONTAINS_UPPER_CASE.matcher(bundleName).matches()) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("ProjectNameShouldContainOnlyLowerCaseCharacters"));
    }
    return Status.OK_STATUS;
  }
}
