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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.util.NoSourceException;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.ScoutSourceUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public final class PropertyMethodSourceUtility {

  /**
   * with "com.bsiag.test.ClassA.Field"
   * group1 = com.bsiag.test.ClassA.
   * group2 = ClassA.
   * group3 = Field
   * with "com.bsiag.test.ClassA.Field"
   * group1 = ""
   * group2 = null
   * group3 = Field
   */
  private final static Pattern REGEX_FIELD_NEW = Pattern.compile("\\b(([A-Za-z][a-zA-Z0-9_]*\\.)*)?([A-Za-z][a-zA-Z0-9_]*)\\b");
  private final static Pattern REGEX_NUMBER_PREFIX = Pattern.compile("^(\\+|\\-)?([A-Za-z0-9_\\.]*)$");
  private final static Pattern REGEX_NUMBER_INFINITY = Pattern.compile("^(\\-)?(inf)$");
  private final static Pattern REGEX_SIMPLE_DOUBLE = Pattern.compile("^[\\+\\-0-9eEf\\.\\,']*[Dd]?$");
  private final static Pattern REGEX_SIMPLE_INTEGER = Pattern.compile("^[\\+\\-0-9eE']*$");
  private final static Pattern REGEX_SIMPLE_LONG = Pattern.compile("^[\\+\\-0-9eE']*[lL]?$");
  private final static Pattern REGEX_SIMPLE_BOOLEAN = Pattern.compile("^(false|true)$");
  /**
   * with com.bsiag.Test.class
   * group1 = com.bsiag.Test.
   * group2 = Test.
   * group3 = class
   * with Test.class
   * group1 = Test.
   * group2 = Test.
   * group3 = class
   */
  private final static Pattern REGEX_CLASS_REFERENCE = Pattern.compile("\\b(([A-Za-z][a-zA-Z0-9_]*\\.)*)?(class)\\b");
  private final static Pattern REGEX_NULL = Pattern.compile("\\bnull\\b");

  private final static Pattern REGEX_METHOD_RETURN_NON_NLS_TEXT = Pattern.compile("\\s*\"(.*)\"\\s*");
  private final static Pattern REGEX_METHOD_RETURN_NLS_TEXT = Pattern.compile("[A-Za-z0-9_-]*\\.get\\(\\s*\\\"([^\\\"]*)\\\"\\s*\\)\\s*");

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
    String source = method.getSource();
    if (source == null) {
      String type = method.getDeclaringType().getElementName();
      throw new NoSourceException(type);
    }
    Matcher m = Regex.REGEX_PROPERTY_METHOD_REPRESENTER_VALUE.matcher(source);
    if (m.find()) {
      return m.group(1).trim();
    }
    else {
      ScoutSdk.logInfo("could not find return value of method: " + method.getElementName());
      throw new CustomImplementationException();
    }
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
    if (parameter.equals("Double.MAX_VALUE")) {
      return Double.MAX_VALUE;
    }
    // handle MIN_VAL / MAX_VAL
    if (parameter.equals("-Double.MAX_VALUE")) {
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
        return DecimalFormat.getInstance().parse(parameter).doubleValue();
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
          return DecimalFormat.getInstance().parse(prefix + referencedValue).doubleValue();
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
          return DecimalFormat.getInstance().parse(prefix + referencedValue).intValue();
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
    if (parameter.equals("Integer.MAX_VALUE")) {
      return Integer.MAX_VALUE;
    }
    // handle MIN_VAL / MAX_VAL
    if (parameter.equals("-Integer.MAX_VALUE")) {
      return -Integer.MAX_VALUE;
    }
    Matcher infMatcher = REGEX_NUMBER_INFINITY.matcher(parameter);
    if (infMatcher.find()) {
      if (infMatcher.group(1) != null) {
        return -Integer.MAX_VALUE;
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
      IType referencedType = ScoutUtility.getReferencedType(method.getDeclaringType(), className);
      if (referencedType == null) {
        ScoutSdk.logWarning("referenced type could not be found '" + method.getElementName() + "' in class '" + method.getDeclaringType().getFullyQualifiedName() + "'");
        throw new CoreException(new ScoutStatus(Status.WARNING, "Referenced type '" + parameter + "' could not be found.", null));
      }
      return referencedType;
    }
    throw new CustomImplementationException();
  }

  private static String findReferencedValue(String parameter, IMethod method, ITypeHierarchy superTypeHierarchy) throws CoreException {
    Matcher matcher = REGEX_FIELD_NEW.matcher(parameter);
    if (matcher.find()) {
      if (matcher.group(2) != null) {
        String typeName = matcher.group(1);
        typeName = typeName.substring(0, typeName.length() - 1);
        IType referencedType = ScoutUtility.getReferencedType(method.getDeclaringType(), typeName);
        if (referencedType == null) {
          ScoutSdk.logWarning("referenced type could not be found '" + method.getElementName() + "' in class '" + method.getDeclaringType().getFullyQualifiedName() + "'");
          throw new CoreException(new ScoutStatus(Status.WARNING, "Reference '" + parameter + "' could not be found.", null));
        }
        String fieldValue = ScoutSourceUtility.findReferencedFieldValue(referencedType, matcher.group(3), superTypeHierarchy);
        return fieldValue;
      }
      else {
        String fieldValue = ScoutSourceUtility.findReferencedFieldValue(method.getDeclaringType(), matcher.group(3), superTypeHierarchy);
        return fieldValue;
      }
    }
    throw new CustomImplementationException();
  }

  public static String parseReturnParameterIcon(String input, IMethod method) throws CoreException {
    if (input.equals("null")) {
      return "";
    }
    Matcher matcher = REGEX_FIELD_NEW.matcher(input);
    if (matcher.find()) {
      if (matcher.group(2) != null) {
        String typeName = matcher.group(1);
        String fieldName = matcher.group(3);
        typeName = typeName.substring(0, typeName.length() - 1);
        IType iconsType = TypeUtility.getReferencedType(method.getDeclaringType(), typeName);
        if (TypeUtility.exists(iconsType)) {
          ITypeHierarchy iconsSuperTypeHierarchy = iconsType.newSupertypeHierarchy(null);
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
   * Tries to find a text value in the method body:
   * <xmp>
   * public String a(){
   * return "abc";
   * }
   * // a.getConfiguredTextValue() returns abc
   * public String b(){
   * return Texts.get("abc");
   * }
   * // b.getConfiguredTextValue() returns the translation of abc
   * public String c(){
   * return TEXTS.get("abc");
   * }
   * // c.getConfiguredTextValue() returns the translation of the value abc
   * </xmp>
   * 
   * @param defaultValue
   * @return
   * @throws JavaModelException
   */
  public static String parseReturnParameterNlsKey(String input) throws CoreException {
    return parseReturnParameterNlsKey(input, new BooleanHolder());
  }

  public static String parseReturnParameterNlsKey(String input, BooleanHolder isNlsText) throws CoreException {
    isNlsText.setValue(false);
    if (input == null || input.trim().equals("null")) {
      return null;
    }
    Matcher m = REGEX_METHOD_RETURN_NON_NLS_TEXT.matcher(input);
    if (m.matches()) {
      String s = m.group(1);
      return s;
    }
    m = REGEX_METHOD_RETURN_NLS_TEXT.matcher(input);
    if (m.matches()) {
      String key = m.group(1);
      isNlsText.setValue(true);
      return key;
    }
    throw new CustomImplementationException();
  }
}
