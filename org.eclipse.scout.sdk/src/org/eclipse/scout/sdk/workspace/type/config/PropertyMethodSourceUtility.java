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
package org.eclipse.scout.sdk.workspace.type.config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.NoSourceException;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

public final class PropertyMethodSourceUtility {
  private static final Pattern REGEX_REFERENCED_VALUE = Pattern.compile("\\b(([A-Za-z][a-zA-Z0-9_]*\\.)*)?([A-Za-z][a-zA-Z0-9_]*)\\b");
  private static final Pattern REGEX_NUMBER_PREFIX = Pattern.compile("^(\\+|\\-)?([A-Za-z0-9_\\.]*)$");
  private static final Pattern REGEX_NUMBER_INFINITY = Pattern.compile("^(\\-)?(inf)$");
  private static final Pattern REGEX_SIMPLE_DOUBLE = Pattern.compile("^[\\+\\-0-9eE\\.']*[DdfF]?$");
  private static final Pattern REGEX_SIMPLE_INTEGER = Pattern.compile("^[\\+\\-0-9eE']*$");
  private static final Pattern REGEX_SIMPLE_BIG_INTEGER = Pattern.compile("^(new (java\\.math\\.)?BigInteger\\(\\\")?([\\+\\-0-9]*)(\\\"\\))?$");
  private static final Pattern REGEX_SIMPLE_BIG_DECIMAL = Pattern.compile("^(new (java\\.math\\.)?BigDecimal\\(\\\")?([\\+\\-0-9eE\\.]*)(\\\"\\))?$");
  private static final Pattern REGEX_SIMPLE_LONG = Pattern.compile("^[\\+\\-0-9eE']*[lL]?$");
  private static final Pattern REGEX_SIMPLE_BOOLEAN = Pattern.compile("^(false|true)$");
  private static final Pattern REGEX_FIELD_VALUE = Pattern.compile("=\\s*(.*)\\s*\\;", Pattern.DOTALL);
  private static final Pattern REGEX_CLASS_REFERENCE = Pattern.compile("\\b(([A-Za-z][a-zA-Z0-9_]*\\.)*)?(class)\\b");
  private static final Pattern REGEX_NULL = Pattern.compile("\\bnull\\b");
  private static final Pattern REGEX_METHOD_RETURN_NON_NLS_TEXT = Pattern.compile("\\s*\"(.*)\"\\s*");
  private static final Pattern REGEX_METHOD_RETURN_NLS_TEXT = Pattern.compile("[A-Za-z0-9_-]*\\.get\\(\\s*\\\"([^\\\"]*)\\\"\\s*\\)\\s*");

  private PropertyMethodSourceUtility() {
  }

  public static class CustomImplementationException extends CoreException {
    private static final long serialVersionUID = -2524492192516521856L;

    public CustomImplementationException() {
      super(new ScoutStatus(Status.INFO, Texts.get("CustomImplementation"), null));
    }
  }

  /**
   * <xmp>
   * String methodA(){
   * return "a string";
   * } // result: a string
   * int methodB(){
   * return Integer.MAX_VALUE;
   * } // result: Integer.MAX_VALUE;
   * </xmp>
   *
   * @param method
   * @return
   * @throws CoreException
   */
  public static String getMethodReturnValue(IMethod method) throws CoreException {
    String methodReturnValue = ScoutUtility.getMethodReturnValue(method);
    if (methodReturnValue != null) {
      return methodReturnValue;
    }
    else {
      ScoutSdk.logInfo("could not find return value of method: " + method.getElementName());
      throw new CustomImplementationException();
    }
  }

  private static IMethod findMethodInHierarchy(IType type, String methodName) {
    IMethod method = TypeUtility.getMethod(type, methodName);
    if (TypeUtility.exists(method)) {
      return method;
    }
    ITypeHierarchy superTypeHierarchy = ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(type);
    IType declaringType = superTypeHierarchy.getSuperclass(type);
    while (declaringType != null) {
      method = TypeUtility.getMethod(declaringType, methodName);
      if (TypeUtility.exists(method)) {
        return method;
      }
      declaringType = superTypeHierarchy.getSuperclass(declaringType);
    }
    return null;
  }

  /**
   * @param type
   * @param methodName
   * @return simpleName (translatedName)
   */
  public static String getTranslatedMethodStringValue(IType type, String methodName) {
    String name = type.getElementName();
    if (StringUtility.hasText(methodName)) {
      IMethod method = findMethodInHierarchy(type, methodName);
      if (TypeUtility.exists(method)) {
        String retValue = ScoutUtility.getMethodReturnValue(method);
        if (StringUtility.hasText(retValue) && !"null".equals(retValue)) {
          String nlsKey = null;
          try {
            nlsKey = PropertyMethodSourceUtility.parseReturnParameterNlsKey(retValue);
          }
          catch (CustomImplementationException e) {
            // ignore
          }
          if (nlsKey == null) {
            return name + " (" + retValue + ")";
          }
          else {
            INlsProject nlsProject = ScoutTypeUtility.findNlsProject(type);
            if (nlsProject != null) {
              INlsEntry entry = nlsProject.getEntry(nlsKey);
              String translation = nlsKey;
              if (entry != null) {
                translation = entry.getTranslation(nlsProject.getDevelopmentLanguage(), true);
              }
              return name + " (" + translation + ")";
            }
          }
        }
      }
    }

    return name;
  }

  /**
   * <xmp>
   * input: "AString" output: AString
   * input: IConstants.A output: the value of A
   * input: null output: null
   * </xmp>
   *
   * @param parameter
   * @param method
   * @return
   * @throws CoreException
   */
  public static String parseReturnParameterString(String parameter, IMethod method, ITypeHierarchy superTypeHierarchy) throws CoreException {
    if (REGEX_NULL.matcher(parameter).matches()) {
      return null;
    }
    String val = JdtUtility.fromStringLiteral(parameter);
    if (val != null) {
      return val;
    }
    else {
      String referencedValue = findReferencedValue(parameter, method, superTypeHierarchy);
      if (referencedValue != null) {
        val = JdtUtility.fromStringLiteral(referencedValue);
        if (val != null) {
          return val;
        }
      }
    }
    throw new CustomImplementationException();
  }

  /**
   * <xmp>
   * input: "AString" output: AString
   * input: IConstants.A output: the value of A
   * </xmp>
   *
   * @param parameter
   * @param method
   * @return
   * @throws CoreException
   */
  public static Double parseReturnParameterDouble(String parameter, IMethod method, ITypeHierarchy superTypeHierarchy) throws CoreException {
    if (REGEX_NULL.matcher(parameter).matches() || parameter.equals("")) {
      return null;
    }
    if ("Double.MAX_VALUE".equals(parameter)) {
      return Double.MAX_VALUE;
    }
    if ("-Double.MAX_VALUE".equals(parameter)) {
      return -Double.MAX_VALUE;
    }
    Matcher infMatcher = REGEX_NUMBER_INFINITY.matcher(parameter);
    if (infMatcher.find()) {
      if (infMatcher.group(1) != null) {
        return -Double.MAX_VALUE;
      }
      else {
        return Double.MAX_VALUE;
      }
    }
    if (REGEX_SIMPLE_DOUBLE.matcher(parameter).matches()) {
      parameter = parameter.replace('e', 'E');
      parameter = parameter.replace("E+", "E");
      try {
        return DecimalFormat.getInstance(Locale.ENGLISH).parse(parameter).doubleValue();
      }
      catch (ParseException e) {
        throw new CoreException(new ScoutStatus("Error parsing parameter '" + parameter + "' to a decimal.", e));
      }
    }
    String prefix = "";
    Matcher matcher = REGEX_NUMBER_PREFIX.matcher(parameter);
    if (matcher.find()) {
      if (matcher.group(1) != null) {
        prefix = matcher.group(1);
      }
      parameter = matcher.group(2);
      String referencedValue = findReferencedValue(parameter, method, superTypeHierarchy);
      if (referencedValue != null) {
        referencedValue = referencedValue.replace('e', 'E');
        referencedValue = referencedValue.replace("E+", "E");
        try {
          return DecimalFormat.getInstance(Locale.ENGLISH).parse(prefix + referencedValue).doubleValue();
        }
        catch (ParseException e) {
          throw new CoreException(new ScoutStatus("Error parsing parameter '" + prefix + referencedValue + "' to a decimal.", e));
        }
      }
    }
    throw new CustomImplementationException();
  }

  /**
   * @param parameter
   * @param method
   * @return
   * @throws CoreException
   */
  public static Integer parseReturnParameterInteger(String parameter, IMethod method, ITypeHierarchy superTypeHierarchy) throws CoreException {
    try {
      return parseReturnParameterInteger(parameter);
    }
    catch (CoreException e) {
      // void try to find referenced value
    }
    String prefix = "";
    Matcher matcher = REGEX_NUMBER_PREFIX.matcher(parameter);
    if (matcher.find()) {
      if (matcher.group(1) != null) {
        prefix = matcher.group(1);
      }
      parameter = matcher.group(2);
      String referencedValue = findReferencedValue(parameter, method, superTypeHierarchy);
      if (referencedValue != null) {
        referencedValue = referencedValue.replace('e', 'E');
        referencedValue = referencedValue.replace("E+", "E");
        try {
          return DecimalFormat.getInstance(Locale.ENGLISH).parse(prefix + referencedValue).intValue();
        }
        catch (ParseException e) {
          throw new CoreException(new ScoutStatus("Error parsing parameter '" + prefix + referencedValue + "' to a decimal.", e));
        }
      }
    }
    throw new CustomImplementationException();
  }

  public static Integer parseReturnParameterInteger(String parameter) throws CoreException {
    if (REGEX_NULL.matcher(parameter).matches() || parameter.equals("")) {
      return null;
    }
    // handle MIN_VAL / MAX_VAL
    if (parameter.equals("Integer.MAX_VALUE")) {
      return Integer.MAX_VALUE;
    }
    if (parameter.equals("Integer.MIN_VALUE")) {
      return Integer.MIN_VALUE;
    }
    Matcher infMatcher = REGEX_NUMBER_INFINITY.matcher(parameter);
    if (infMatcher.find()) {
      if (infMatcher.group(1) != null) {
        return Integer.MIN_VALUE;
      }
      else {
        return Integer.MAX_VALUE;
      }
    }
    if (REGEX_SIMPLE_INTEGER.matcher(parameter).matches()) {
      parameter = parameter.replace('e', 'E');
      parameter = parameter.replace("E+", "E");
      try {
        return DecimalFormat.getIntegerInstance().parse(parameter).intValue();
      }
      catch (ParseException e) {
        throw new CoreException(new ScoutStatus("Error parsing parameter '" + parameter + "' to a decimal.", e));
      }
    }
    throw new CustomImplementationException();
  }

  /**
   * @param parameter
   * @param method
   * @return
   * @throws CoreException
   */
  public static Long parseReturnParameterLong(String parameter, IMethod method, ITypeHierarchy superTypeHierarchy) throws CoreException {
    try {
      return parseReturnParameterLong(parameter);
    }
    catch (CoreException e) {
      // void work on with referenced values
    }
    String prefix = "";
    Matcher matcher = REGEX_NUMBER_PREFIX.matcher(parameter);
    if (matcher.find()) {
      if (matcher.group(1) != null) {
        prefix = matcher.group(1);
      }
      parameter = matcher.group(2);
      String referencedValue = findReferencedValue(parameter, method, superTypeHierarchy);
      if (referencedValue != null) {
        referencedValue = referencedValue.replace('e', 'E');
        referencedValue = referencedValue.replace('x', 'X');
        referencedValue = referencedValue.replace("E+", "E");
        try {
          return DecimalFormat.getNumberInstance().parse(prefix + referencedValue).longValue();
        }
        catch (ParseException e) {
          throw new CoreException(new ScoutStatus("Error parsing parameter '" + prefix + referencedValue + "' to a decimal.", e));
        }
      }
    }
    throw new CustomImplementationException();
  }

  /**
   * parses the given return clause input string into a big decimal.<br>
   * <br>
   * Example:<br>
   * Input: new BigDecimal("-999999999999999999999999999999999999999999999999999999999999.056978")<br>
   * Output: -999999999999999999999999999999999999999999999999999999999999.056978<br>
   * <br>
   * Input: 2358.2357<br>
   * Output: 2358.2357
   *
   * @param parameter
   * @return
   * @throws CoreException
   */
  public static BigDecimal parseReturnParameterBigDecimal(String parameter) throws CoreException {
    if (REGEX_NULL.matcher(parameter).matches() || parameter.equals("")) {
      return null;
    }
    Matcher m = REGEX_SIMPLE_BIG_DECIMAL.matcher(parameter);
    if (m.find()) {
      return new BigDecimal(m.group(3));
    }
    throw new CustomImplementationException();
  }

  /**
   * parses the given return clause input string into the corresponding rounding mode enum value.
   *
   * @param parameter
   *          The return clause
   * @return The bigdecimal value of the return clause.
   * @throws CoreException
   *           In case the value could not be parsed.
   */
  public static RoundingMode parseReturnParameterRoundingMode(String parameter) throws CoreException {
    if (REGEX_NULL.matcher(parameter).matches() || parameter.equals("")) {
      return null;
    }
    if (parameter.startsWith(RoundingMode.class.getName()) && parameter.length() > RoundingMode.class.getName().length()) {
      parameter = parameter.substring(RoundingMode.class.getName().length() + 1);
    }
    if (parameter.startsWith(RoundingMode.class.getSimpleName()) && parameter.length() > RoundingMode.class.getSimpleName().length()) {
      parameter = parameter.substring(RoundingMode.class.getSimpleName().length() + 1);
    }
    RoundingMode mode = RoundingMode.valueOf(parameter);
    if (mode != null) {
      return mode;
    }
    throw new CustomImplementationException();
  }

  /**
   * parses the given return clause input string into a big integer.<br>
   * <br>
   * Example:<br>
   * Input: new BigInteger("-999999999999999999999999999999999999999999999999999999999999")<br>
   * Output: -999999999999999999999999999999999999999999999999999999999999<br>
   * <br>
   * Input: 2358<br>
   * Output: 2358
   *
   * @param parameter
   * @return
   * @throws CoreException
   */
  public static BigInteger parseReturnParameterBigInteger(String parameter) throws CoreException {
    if (REGEX_NULL.matcher(parameter).matches() || parameter.equals("")) {
      return null;
    }
    Matcher m = REGEX_SIMPLE_BIG_INTEGER.matcher(parameter);
    if (m.find()) {
      return new BigInteger(m.group(3));
    }
    throw new CustomImplementationException();
  }

  public static Long parseReturnParameterLong(String parameter) throws CoreException {
    if (REGEX_NULL.matcher(parameter).matches() || parameter.equals("")) {
      return null;
    }
    // handle MIN_VAL / MAX_VAL
    if (parameter.equals("Long.MAX_VALUE")) {
      return Long.MAX_VALUE;
    }
    // handle MIN_VAL / MAX_VAL
    if (parameter.equals("Long.MIN_VALUE")) {
      return Long.MIN_VALUE;
    }
    Matcher infMatcher = REGEX_NUMBER_INFINITY.matcher(parameter);
    if (infMatcher.find()) {
      if (infMatcher.group(1) != null) {
        return Long.MIN_VALUE;
      }
      else {
        return Long.MAX_VALUE;
      }
    }
    if (REGEX_SIMPLE_LONG.matcher(parameter).matches()) {
      parameter = parameter.replace('e', 'E');
      parameter = parameter.replace("E+", "E");
      try {
        return DecimalFormat.getNumberInstance().parse(parameter).longValue();
      }
      catch (ParseException e) {
        throw new CoreException(new ScoutStatus("Error parsing parameter '" + parameter + "' to a decimal.", e));
      }
    }
    throw new CustomImplementationException();
  }

  public static boolean parseReturnParameterBoolean(String parameter, IMethod method, ITypeHierarchy superTypeHierarchy) throws CoreException {
    if (REGEX_SIMPLE_BOOLEAN.matcher(parameter).matches()) {
      return Boolean.parseBoolean(parameter);
    }
    Matcher matcher = REGEX_NUMBER_PREFIX.matcher(parameter);
    if (matcher.find()) {
      parameter = matcher.group(2);
      String referencedValue = findReferencedValue(parameter, method, superTypeHierarchy);
      if (referencedValue != null) {
        return Boolean.parseBoolean(referencedValue);
      }
    }
    throw new CustomImplementationException();
  }

  /**
   * @param input
   * @param method
   * @return
   * @throws CoreException
   */
  public static IType parseReturnParameterClass(String parameter, IMethod method) throws CoreException {
    if (REGEX_NULL.matcher(parameter).matches()) {
      return null;
    }
    Matcher matcher = REGEX_CLASS_REFERENCE.matcher(parameter);
    if (matcher.find()) {
      String className = matcher.group(1);
      className = className.substring(0, className.length() - 1);
      IType referencedType = TypeUtility.getReferencedType(method.getDeclaringType(), className, true);
      if (referencedType == null) {
        ScoutSdk.logWarning("referenced type could not be found '" + method.getElementName() + "' in class '" + method.getDeclaringType().getFullyQualifiedName() + "'");
        throw new CoreException(new ScoutStatus(Status.WARNING, "Referenced type '" + parameter + "' could not be found.", null));
      }
      return referencedType;
    }
    throw new CustomImplementationException();
  }

  private static String findReferencedValue(String parameter, IMethod method, ITypeHierarchy superTypeHierarchy) throws CoreException {
    Matcher matcher = REGEX_REFERENCED_VALUE.matcher(parameter);
    if (matcher.find()) {
      IType referencedType = null;
      if (matcher.group(2) != null) {
        String typeName = matcher.group(1);
        typeName = typeName.substring(0, typeName.length() - 1);
        referencedType = TypeUtility.getReferencedType(method.getDeclaringType(), typeName, true);
        if (!TypeUtility.exists(referencedType)) {
          throw new CoreException(new ScoutStatus(Status.WARNING, "Reference '" + parameter + "' could not be found.", null));
        }
      }
      else {
        referencedType = method.getDeclaringType();
      }
      return findReferencedFieldValue(referencedType, matcher.group(3), superTypeHierarchy);
    }
    throw new CustomImplementationException();
  }

  private static String findReferencedFieldValue(IType type, String value, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    String retVal = findFieldValueInDeclaringHierarchy(type, value);
    if (retVal == null) {
      retVal = findFieldValueInHierarchy(type, value, superTypeHierarchy);
    }
    return retVal;
  }

  public static String getFieldValue(IField field) throws JavaModelException {
    String source = field.getSource();
    if (source == null) {
      throw new NoSourceException(field.getDeclaringType().getElementName());
    }
    Matcher matcher = REGEX_FIELD_VALUE.matcher(ScoutUtility.removeComments(source));
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  public static String findFieldValueInHierarchy(IType type, String name, ITypeHierarchy superTypeHierarchy) throws JavaModelException {
    if (!TypeUtility.exists(type)) {
      return null;
    }
    IField field = type.getField(name);
    if (TypeUtility.exists(field)) {
      String val = getFieldValue(field);
      if (val != null) {
        return val;
      }
    }
    String value = null;
    for (IType supertype : superTypeHierarchy.getSupertypes(type)) {
      value = findFieldValueInHierarchy(supertype, name, superTypeHierarchy);
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

  public static String parseReturnParameterIcon(String input, IMethod method) throws CoreException {
    if (input.equals("null")) {
      return "";
    }
    Matcher matcher = REGEX_REFERENCED_VALUE.matcher(input);
    if (matcher.find()) {
      if (matcher.group(2) != null) {
        String typeName = matcher.group(1);
        String fieldName = matcher.group(3);
        typeName = typeName.substring(0, typeName.length() - 1);
        IType iconsType = TypeUtility.getReferencedType(method.getDeclaringType(), typeName, true);
        if (TypeUtility.exists(iconsType)) {
          ITypeHierarchy iconsSuperTypeHierarchy = ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(iconsType);
          while (TypeUtility.exists(iconsType)) {
            IField field = iconsType.getField(fieldName);
            if (TypeUtility.exists(field)) {
              Object val = TypeUtility.getFieldConstant(field);
              if (val instanceof String) {
                return val.toString();
              }
            }
            iconsType = iconsSuperTypeHierarchy.getSuperclass(iconsType);
          }
        }
      }
    }
    throw new CustomImplementationException();
  }

  /**
   * Tries to find a NLS key in the input string.<br>
   * <br>
   * <code>
   * Input: "" or null<br>
   * Output: null<br><br>
   * Input "abc"<br>
   * Output: null<br><br>
   * Input: Texts.get("abc")<br>
   * Output: abc<br><br>
   * Input: TEXTS.get("abc")<br>
   * Output: abc
   * </code>
   *
   * @param input
   *          The String to parse.
   * @return
   * @throws CustomImplementationException
   *           When the content could not be parsed.
   */
  public static String parseReturnParameterNlsKey(String input) throws CustomImplementationException {
    if (input == null || "null".equals(input.trim())) {
      return null;
    }
    Matcher m = REGEX_METHOD_RETURN_NON_NLS_TEXT.matcher(input);
    if (m.matches()) {
      return null;
    }
    m = REGEX_METHOD_RETURN_NLS_TEXT.matcher(input);
    if (m.matches()) {
      String key = m.group(1);
      return key;
    }
    throw new CustomImplementationException();
  }
}
