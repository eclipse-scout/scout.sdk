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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>BCSourceUtilities</h3> several source helper methods. Take a look at the public static methods.
 */
public class ScoutSourceUtility {

  private static final Pattern REGEX_FIELD_VALUE = Pattern.compile("=\\s*(.*)\\s*\\;", Pattern.DOTALL);

  private ScoutSourceUtility() {
  }

  /**
   * @param type
   * @param methodName
   * @return simpleName (translatedName)
   */
  public static String getTranslatedMethodStringValue(IType type, String methodName) {
    String name = type.getElementName();
    if (StringUtility.isNullOrEmpty(methodName)) {
      return name;
    }
    IMethod method = findMethodInHierarchy(type, methodName);
    if (method == null || !method.exists()) {
      return name;
    }
    IScoutBundle scoutBundle = ScoutSdkCore.getScoutWorkspace().getScoutBundle(type.getJavaProject().getProject());
    if (scoutBundle == null) {
      return name;
    }
    INlsProject nlsProject = scoutBundle.findBestMatchNlsProject();
    if (nlsProject == null) {
      return name;
    }
    IType referenceTypes = nlsProject.getNlsAccessorType();
    if (referenceTypes != null) {
      String methodSource = null;
      try {
        methodSource = method.getSource();
        if (methodSource == null) {
          return name;
        }
        else {
          methodSource = ScoutUtility.removeComments(methodSource);
        }
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not find nls text of method '" + method.getElementName() + "'.", e);
        return name;
      }

      // check for TEXTS.get("...") value
      Matcher m = Regex.REGEX_METHOD_RETURN_NLS_TEXT.matcher(methodSource);
      if (m.find()) {
        String key = m.group(2);
        String translation = key;
        INlsEntry entry = nlsProject.getEntry(key);
        if (entry != null) {
          translation = entry.getTranslation(nlsProject.getDevelopmentLanguage(), true);
        }
        return name + " (" + translation + ")";
      }

      // check for constant value
      m = Regex.REGEX_METHOD_RETURN_NON_NLS_TEXT.matcher(methodSource);
      if (m.find()) {
        String s = m.group(1);
        return name + " (" + s + ")";
      }
    }
    return name;
  }

  private static IMethod findMethodInHierarchy(IType type, String methodName) {
    IMethod method = TypeUtility.getMethod(type, methodName);
    if (TypeUtility.exists(method)) {
      return method;
    }
    try {
      ITypeHierarchy superTypeHierarchy = type.newSupertypeHierarchy(null);
      IType declaringType = superTypeHierarchy.getSuperclass(type);
      while (declaringType != null) {
        method = TypeUtility.getMethod(declaringType, methodName);
        if (TypeUtility.exists(method)) {
          return method;
        }
        declaringType = superTypeHierarchy.getSuperclass(declaringType);
      }
    }
    catch (JavaModelException e) {
      ScoutSdk.logWarning("could not find method '" + methodName + "' on super type hierarchy of '" + type.getFullyQualifiedName() + "'.", e);
    }
    return null;
  }

  public static String findReferencedFieldValue(IType type, String value, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    String retVal = findFieldValueInDeclaringHierarchy(type, value);
    if (retVal == null) {
      retVal = findFieldValueInHierarchyImpl(type, value, superTypeHierarchy);
    }
    return retVal;
  }

  private static String findFieldValueInHierarchyImpl(IType type, String name, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    if (!TypeUtility.exists(type)) {
      return null;
    }
    IField field = type.getField(name);
    if (TypeUtility.exists(field)) {
      String source = field.getSource();
      if (source == null) {
        throw new NoSourceException(type.getElementName());
      }
      Matcher matcher = REGEX_FIELD_VALUE.matcher(ScoutUtility.removeComments(source));
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    String value = null;
    for (IType supertype : superTypeHierarchy.getSupertypes(type)) {
      value = findFieldValueInHierarchyImpl(supertype, name, superTypeHierarchy);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private static String findFieldValueInDeclaringHierarchy(IType type, String value) throws JavaModelException {
    if (!TypeUtility.exists(type)) {
      return null;
    }
    IField field = type.getField(value);
    if (!TypeUtility.exists(field)) {
      return findFieldValueInDeclaringHierarchy(type.getDeclaringType(), value);
    }
    else {
      Matcher matcher = REGEX_FIELD_VALUE.matcher(ScoutUtility.removeComments(field.getSource()));
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return null;
  }

  public static String removeLeadingCommentAndAnnotationLines(String methodBody) {
    Matcher matcherMethodDefinition = Regex.REGEX_METHOD_DEFINITION.matcher(methodBody);
    if (matcherMethodDefinition.find()) {
      methodBody = methodBody.substring(matcherMethodDefinition.start());
    }
    return methodBody;
  }

  public static String removeLineLeadingTab(int i, String methodBlock, final String newLine) {
    Pattern p = Pattern.compile("^[\\t]{" + i + "}");
    BufferedReader reader = null;
    StringBuilder newBody = new StringBuilder();
    try {
      reader = new BufferedReader(new StringReader(methodBlock));
      String line = reader.readLine();
      boolean addNewLine = false;
      while (line != null) {
        if (addNewLine) {
          newBody.append(newLine);
        }
        else {
          addNewLine = true;
        }
        Matcher matcher = p.matcher(line);
        if (matcher.find()) {
          line = line.substring(matcher.end());
        }
        newBody.append(line);
        line = reader.readLine();
      }
    }
    catch (IOException e) {
    }
    finally {
      try {
        if (reader != null) reader.close();
      }
      catch (IOException e1) {
      }
    }
    return newBody.toString();
  }

  /**
   * to find any name matching inner type in the inner type hierarchy of the given type.
   * 
   * @param refType
   *          the type searching starts.
   * @param simpleName
   *          the name to look at.
   * @param ignoreCase
   *          false for a case sensitive match.
   * @return the found inner type or null if not found.
   * @throws JavaModelException
   */
  public static IType findInnerType(IType declaringType, String simpleName, boolean ignoreCase) throws JavaModelException {
    if (ignoreCase) {
      if (declaringType.getElementName().equalsIgnoreCase(simpleName)) {
        return declaringType;
      }
    }
    else {
      if (declaringType.getElementName().equals(simpleName)) {
        return declaringType;
      }
    }
    for (IType innerType : declaringType.getTypes()) {
      return findInnerType(innerType, simpleName, ignoreCase);
    }
    return null;
  }
}
