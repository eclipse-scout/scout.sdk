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
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>BCSourceUtilities</h3> several source helper methods. Take a look at the public static methods.
 */
public class ScoutSourceUtilities {
  private static final ScoutSourceUtilities instance = new ScoutSourceUtilities();

  // private FileWriter m_fileWriter;

  private ScoutSourceUtilities() {
    // m_fileWriter = new FileWriter("D:\\Temp\\max24h\\bsiCaseLog.txt");
  }

  /**
   * @param type
   * @param methodName
   * @return simpleName (translatedName)
   */
  public static String getTranslatedMethodStringValue(IType type, String methodName) {
    return instance.getTranslatedMethodStringValueImpl(type, methodName);
  }

  private String getTranslatedMethodStringValueImpl(IType type, String methodName) {
    String name = type.getElementName();
    if (StringUtility.isNullOrEmpty(methodName)) {
      return name;
    }

    IMethod method = findMethodInHierarchy(type, methodName);
    if (method == null || !method.exists()) {
      return name;
    }
    IScoutBundle scoutBundle = ScoutSdk.getScoutWorkspace().getScoutBundle(type.getJavaProject().getProject());
    if (scoutBundle == null) {
      return name;
    }
    INlsProject nlsProject = scoutBundle.findBestMatchNlsProject();
    if (nlsProject == null) {
      return name;
    }
    IType[] referenceTypes = nlsProject.getReferenceTypes();
    if (referenceTypes != null && referenceTypes.length > 0) {
      StringBuilder classBuilder = new StringBuilder();
      //boolean returnTypeName = true;
      classBuilder.append("(");
      for (int i = 0; i < referenceTypes.length; i++) {
        classBuilder.append(referenceTypes[i].getElementName());
        if (i < (referenceTypes.length - 1)) {
          classBuilder.append("|");
        }
      }
      classBuilder.append(")");
      String methodSource = null;
      try {
        methodSource = method.getSource();
      }
      catch (Exception e) {
        ScoutSdk.logWarning("could not find nls text of method '" + method.getElementName() + "'.", e);
        return name;
      }
      Matcher m = Pattern.compile(Regex.REGEX_METHOD_RETURN_NLS_TEXT_DEFAULT, Pattern.DOTALL).matcher(methodSource);
      if (m.find()) {
        return name;
      }
      m = Pattern.compile(Regex.REGEX_METHOD_RETURN_NON_NLS_TEXT, Pattern.DOTALL).matcher(methodSource);
      if (m.find()) {
        String s = m.group(1);
        return name + " (" + s + ")";
      }
      m = Pattern.compile(Regex.replace(Regex.REGEX_METHOD_RETURN_NLS_TEXT, classBuilder.toString()), Pattern.DOTALL).matcher(methodSource);
      if (m.find()) {
        String key = m.group(2);
        String translation = key;
        INlsEntry entry = nlsProject.getEntry(key);
        if (entry != null) {
          translation = entry.getTranslation(nlsProject.getDevelopmentLanguage(), true);
        }
        return name + " (" + translation + ")";
      }
      m = Pattern.compile(Regex.replace(Regex.REGEX_METHOD_RETURN_NLS_TEXT_WITH_KEY, classBuilder.toString()), Pattern.DOTALL).matcher(methodSource);
      if (m.find()) {
        String key = m.group(3);
        String translation = key;
        INlsEntry entry = nlsProject.getEntry(key);
        if (entry != null) {
          translation = entry.getTranslation(nlsProject.getDevelopmentLanguage(), true);
        }
        return name + " (" + translation + ")";
      }
      else {
        // no label defined
      }
    }
    return name;
  }

  private IMethod findMethodInHierarchy(IType type, String methodName) {
    IMethod method = TypeUtility.getMethod(type, methodName);
    if (method != null && method.exists()) {
      return method;
    }
    try {
      ITypeHierarchy superTypeHierarchy = type.newSupertypeHierarchy(null);
      IType declaringType = superTypeHierarchy.getSuperclass(type);
      while (declaringType != null) {
        method = TypeUtility.getMethod(declaringType, methodName);
        if (method != null && method.exists()) {
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

  /**
   * @param type
   * @param value
   * @return
   * @throws JavaModelException
   * @Deprecated use {@link ScoutSourceUtilities#findReferencedFieldValue(IType, String, ITypeHierarchy)} instead
   */
  @Deprecated
  public static String findReferencedFieldValue(IType type, String value) throws JavaModelException {
    return instance.findReferencedFieldValueImpl(type, value, type.newSupertypeHierarchy(null));
  }

  public static String findReferencedFieldValue(IType type, String value, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    return instance.findReferencedFieldValueImpl(type, value, superTypeHierarchy);
  }

  private String findReferencedFieldValueImpl(IType type, String value, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    String retVal = findFieldValueInDeclaringHierarchy(type, value);
    if (retVal == null) {
      retVal = findFieldValueInHierarchyImpl(type, value, superTypeHierarchy);
    }
    return retVal;
  }

  private Pattern m_fieldValue = Pattern.compile("=\\s*([^\\s\\;]*)\\s*\\;", Pattern.DOTALL);

  private String findFieldValueInHierarchyImpl(IType type, String name, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    if (!TypeUtility.exists(type)) {
      return null;
    }
    IField field = type.getField(name);
    if (TypeUtility.exists(field)) {
      Matcher matcher = m_fieldValue.matcher(field.getSource());
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

  private String findFieldValueInDeclaringHierarchy(IType type, String value) throws JavaModelException {
    if (type == null) {
      return null;
    }
    IField field = type.getField(value);
    if (field == null || !field.exists()) {
      return findFieldValueInDeclaringHierarchy(type.getDeclaringType(), value);
    }
    else {
      Matcher matcher = m_fieldValue.matcher(field.getSource());
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return null;
  }

  private class P_CompareableSourceRange implements Comparable<P_CompareableSourceRange> {
    private final ISourceRange m_range;
    private final boolean m_ascending;

    public P_CompareableSourceRange(ISourceRange range, boolean ascending) {
      m_range = range;
      m_ascending = ascending;
    }

    public ISourceRange getRange() {
      return m_range;
    }

    @Override
    public int compareTo(P_CompareableSourceRange o) {
      if (m_ascending) {
        return m_range.getOffset() - o.getRange().getOffset();
      }
      else {
        return o.getRange().getOffset() - m_range.getOffset();
      }
    }
  } // end class P_CompareableSourceRange

  public static String removeLeadingCommentAndAnnotationLines(String methodBody) {
    Pattern methodDefinition = Pattern.compile(Regex.REGEX_METHOD_DEFINITION, Pattern.DOTALL);
    Matcher matcherMethodDefinition = methodDefinition.matcher(methodBody);
    if (matcherMethodDefinition.find()) {
      methodBody = methodBody.substring(matcherMethodDefinition.start());
    }
    return methodBody;
  }

  public static String removeLineLeadingTab(int i, String methodBlock) {
    Pattern p = Pattern.compile("^[\\t]{" + i + "}");
    BufferedReader reader = null;
    StringBuilder newBody = new StringBuilder();
    try {
      reader = new BufferedReader(new StringReader(methodBlock));
      String line = reader.readLine();
      boolean addNewLine = false;
      while (line != null) {
        if (addNewLine) {
          newBody.append("\n");
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
  public static IType findInnerType(IType refType, String simpleName, boolean ignoreCase) throws JavaModelException {
    return instance.findInnerTypeRec(refType, simpleName, ignoreCase);
  }

  private IType findInnerTypeRec(IType declaringType, String simpleName, boolean ignoreCase) throws JavaModelException {
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
      return findInnerTypeRec(innerType, simpleName, ignoreCase);
    }
    return null;
  }
}
