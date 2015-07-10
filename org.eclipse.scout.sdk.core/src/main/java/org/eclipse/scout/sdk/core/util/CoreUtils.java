/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.TruePredicate;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.FieldFilters;
import org.eclipse.scout.sdk.core.model.Flags;
import org.eclipse.scout.sdk.core.model.IAnnotatable;
import org.eclipse.scout.sdk.core.model.IAnnotation;
import org.eclipse.scout.sdk.core.model.IAnnotationValue;
import org.eclipse.scout.sdk.core.model.IField;
import org.eclipse.scout.sdk.core.model.IMethod;
import org.eclipse.scout.sdk.core.model.IMethodParameter;
import org.eclipse.scout.sdk.core.model.IPropertyBean;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.model.MethodFilters;
import org.eclipse.scout.sdk.core.model.PropertyBean;
import org.eclipse.scout.sdk.core.model.TypeFilters;
import org.eclipse.scout.sdk.core.parser.ILookupEnvironment;
import org.eclipse.scout.sdk.core.signature.ISignatureConstants;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.eclipse.scout.sdk.core.signature.SignatureUtils;

/**
 * <h3>{@link CoreUtils}</h3>
 * Holds core utilities.
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public final class CoreUtils {

  /**
   * Regular expression matching bean method names (is..., get..., set...)
   */
  public static final Pattern BEAN_METHOD_NAME = Pattern.compile("(get|set|is)([A-Z].*)");
  private static final ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<>();
  private static volatile Set<String> javaKeyWords = null;

  private CoreUtils() {
  }

  /**
   * Converts the given string into a string literal with leading and ending double-quotes including escaping of the
   * given string.<br>
   *
   * @param s
   *          the string to convert.
   * @return the literal string ready to be directly inserted into java source or null if the input string is null.
   */
  public static String toStringLiteral(String s) {
    if (s == null) {
      return null;
    }

    int len = s.length();
    StringBuilder b = new StringBuilder(len + 2);
    b.append('"'); // opening delimiter
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\b':
          b.append("\\b");
          break;
        case '\t':
          b.append("\\t");
          break;
        case '\n':
          b.append("\\n");
          break;
        case '\f':
          b.append("\\f");
          break;
        case '\r':
          b.append("\\r");
          break;
        case '\"':
          b.append("\\\"");
          break;
        case '\\':
          b.append("\\\\");
          break;
        case '\0':
          b.append("\\0");
          break;
        case '\1':
          b.append("\\1");
          break;
        case '\2':
          b.append("\\2");
          break;
        case '\3':
          b.append("\\3");
          break;
        case '\4':
          b.append("\\4");
          break;
        case '\5':
          b.append("\\5");
          break;
        case '\6':
          b.append("\\6");
          break;
        case '\7':
          b.append("\\7");
          break;
        default:
          b.append(c);
          break;
      }
    }
    b.append('"'); // closing delimiter
    return b.toString();
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
    if (StringUtils.isBlank(name)) {
      return name;
    }

    char firstChar = name.charAt(0);
    if (Character.isLowerCase(firstChar)) {
      return name;
    }

    StringBuilder sb = new StringBuilder(name.length());
    sb.append(Character.toLowerCase(firstChar));
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
    if (StringUtils.isBlank(name)) {
      return name;
    }
    char firstChar = name.charAt(0);
    if (Character.isUpperCase(firstChar)) {
      return name;
    }

    StringBuilder sb = new StringBuilder(name.length());
    sb.append(Character.toUpperCase(firstChar));
    if (name.length() > 1) {
      sb.append(name.substring(1));
    }
    return sb.toString();
  }

  /**
   * Gets a one line comment block with given text
   *
   * @param content
   *          The text content
   * @return The comment line.
   */
  public static String getCommentBlock(String content) {
    StringBuilder builder = new StringBuilder();
    builder.append("// TODO ");
    String username = getUsername();
    if (StringUtils.isNotBlank(username)) {
      builder.append("[" + username + "] ");
    }
    builder.append(content);
    return builder.toString();
  }

  public static String getCommentAutoGeneratedMethodStub() {
    return getCommentBlock("Auto-generated method stub.");
  }

  /**
   * Returns the user name of the current thread. If the current thread has no user name set, the system property is
   * returned.<br>
   * Use {@link ScoutUtility#setUsernameForThread(String)} to define the user name for the current thread.
   *
   * @return The user name of the thread or the system if no user name is defined on the thread.
   */
  public static String getUsername() {
    String name = CURRENT_USER_NAME.get();
    if (name == null) {
      name = System.getProperty("user.name");
    }
    return name;
  }

  /**
   * Gets the default value for the given signature data type.
   *
   * @param parameter
   *          The signature data type for which the default return value should be returned.
   * @return A {@link String} or <code>null</code> (for the void signature) holding the default value for the given data
   *         type.
   */
  public static String getDefaultValueOf(String parameter) {
    if (parameter.length() != 1) {
      // not a primitive type
      return "null";
    }

    switch (parameter.charAt(0)) {
      case ISignatureConstants.C_BOOLEAN:
        return "true";
      case ISignatureConstants.C_BYTE:
      case ISignatureConstants.C_CHAR:
      case ISignatureConstants.C_DOUBLE:
      case ISignatureConstants.C_INT:
      case ISignatureConstants.C_LONG:
      case ISignatureConstants.C_SHORT:
        return "0";
      case ISignatureConstants.C_FLOAT:
        return "0.0f";
      default: // e.g. void
        return null;
    }
  }

  /**
   * If the given name is a reserved java keyword a suffix is added to ensure it is a valid name to use e.g. for
   * variables or parameters.
   *
   * @param parameterName
   *          The original name.
   * @return The new value which probably has a suffix appended.
   */
  public static String ensureValidParameterName(String parameterName) {
    if (isReservedJavaKeyword(parameterName)) {
      return parameterName + "Value";
    }
    return parameterName;
  }

  /**
   * @return <code>true</code> if the given word is a reserved java keyword. Otherwise <code>false</code>.
   * @since 3.8.3
   */
  public static boolean isReservedJavaKeyword(String word) {
    if (word == null) {
      return false;
    }
    return getJavaKeyWords().contains(word.toLowerCase());
  }

  /**
   * Gets all reserved java keywords.
   *
   * @return An unmodifiable {@link Set} holding all reserved java keywords.
   */
  public static Set<String> getJavaKeyWords() {
    if (javaKeyWords == null) {
      synchronized (CoreUtils.class) {
        if (javaKeyWords == null) {
          String[] keyWords = new String[]{"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "false", "null", "true"};
          Set<String> tmp = new HashSet<>(keyWords.length);
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
   * Gets all type parameter arguments as resolved type signatures.
   *
   * @param focusType
   *          The origin focus type that defines the type argument.
   * @param levelFqn
   *          The level on which the value of a type parameter should be extracted.
   * @param typeParamIndex
   *          The index of the type parameter on the given level type whose value should be extracted.
   * @return A {@link ListOrderedSet} holding all type argument signatures of the given type parameter.
   * @see #getResolvedTypeParamValue(IType, String, int)
   */
  public static ListOrderedSet/*<String>*/ getResolvedTypeParamValueSignature(IType focusType, String levelFqn, int typeParamIndex) {
    ListOrderedSet/*<IType>*/ typeParamsValue = getResolvedTypeParamValue(focusType, levelFqn, typeParamIndex);
    List<String> result = new ArrayList<>(typeParamsValue.size());
    for (Object t : typeParamsValue) {
      result.add(SignatureUtils.getResolvedSignature((IType) t));
    }
    return ListOrderedSet.decorate(result);
  }

  /**
   * Sets the user name that should be returned by {@link ScoutUtility#getUsername()} for the current thread.
   *
   * @param newUsernameForCurrentThread
   *          the new user name
   */
  public static void setUsernameForThread(String newUsernameForCurrentThread) {
    CURRENT_USER_NAME.set(newUsernameForCurrentThread);
  }

  /**
   * Gets the first direct member {@link IType} of the given declaring type which matches the given {@link Predicate}.
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param filter
   *          The {@link Predicate} choosing the member {@link IType}.
   * @return The first inner {@link IType} or <code>null</code> if it cannot be found.
   */
  public static IType getInnerType(IType declaringType, Predicate/*<IType>*/ filter) {
    if (declaringType == null) {
      return null;
    }
    return (IType) CollectionUtils.find(declaringType.getTypes(), filter);
  }

  /**
   * Gets the first direct member {@link IType} with the given simple name.
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param typeName
   *          The simple name of the member {@link IType} to search.
   * @return The member {@link IType} with the given name or <code>null</code> if it cannot be found.
   */
  public static IType getInnerType(IType declaringType, String typeName) {
    return getInnerType(declaringType, TypeFilters.getElementNameFilter(typeName));
  }

  /**
   * Gets the immediate member {@link IType}s of the given {@link IType} which matches the given {@link Predicate} in
   * the order as it is defined in the source or class file.
   *
   * @param type
   *          The declaring {@link IType} holding the member {@link IType}s.
   * @param filter
   *          The {@link Predicate} to filter the member {@link IType}s.
   * @return A {@link ListOrderedSet} holding the selected member {@link IType}.
   */
  public static ListOrderedSet/*<IType>*/ getInnerTypes(IType type, Predicate/*<IType>*/ filter) {
    return getInnerTypes(type, filter, null);
  }

  /**
   * Returns the immediate member types declared by the given type. The results is filtered using the given
   * {@link Predicate} and
   * sorted using the given {@link Comparator}.
   *
   * @param type
   *          The type whose immediate inner types should be returned.
   * @param filter
   *          the filter to apply or null
   * @param comparator
   *          the comparator to sort the result or null
   * @return the immediate inner types declared in the given type.
   */
  public static ListOrderedSet/*<IType>*/ getInnerTypes(IType type, Predicate/*<IType>*/ filter, Comparator<IType> comparator) {
    ListOrderedSet/*<IType>*/ types = type.getTypes();

    if (filter == null) {
      filter = TruePredicate.getInstance();
    }

    if (comparator == null) {
      // no special order requested. keep order as it comes from the declaring type
      List<IType> l = new ArrayList<>(types.size());
      CollectionUtils.select(types, filter, l);
      return ListOrderedSet.decorate(l);
    }

    Set<IType> result = new TreeSet<>(comparator);
    CollectionUtils.select(types, filter, result);
    return ListOrderedSet.decorate(result);
  }

  /**
   * Searches for an {@link IType} with given simple name within the given type recursively checking all inner types.
   * The
   * given {@link IType} itself is checked as well.
   *
   * @param type
   *          The {@link IType} to start searching. All nested inner {@link IType}s are visited recursively.
   * @param innerTypeName
   *          The simple name (case sensitive) to search for.
   * @return The first {@link IType} found in the nested {@link IType} tree below the given start type that has the
   *         given simple name or <code>null</code> if nothing could be found.
   */
  public static IType findInnerType(IType type, String innerTypeName) {
    if (type == null) {
      return null;
    }
    else if (Objects.equals(type.getSimpleName(), innerTypeName)) {
      return type;
    }
    else {
      for (Object innerType : type.getTypes()) {
        IType found = findInnerType((IType) innerType, innerTypeName);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  /**
   * Gets all interfaces implemented by the given {@link IType} (recursively checking the super hierarchy).
   *
   * @param type
   *          The {@link IType} for which all interfaces should be returned.
   * @return A {@link Set} holding all super interfaces of the given {@link IType}.
   */
  public static Set<IType> getAllSuperInterfaces(IType type) {
    Set<IType> collector = new HashSet<>();
    for (Object t : type.getSuperInterfaces()) {
      getAllSuperInterfaces((IType) t, collector);
    }
    return collector;
  }

  private static void getAllSuperInterfaces(IType t, Set<IType> collector) {
    if (t == null) {
      return;
    }
    if (Flags.isInterface(t.getFlags())) {
      collector.add(t);
    }
    getAllSuperInterfaces(t.getSuperClass(), collector);
    for (Object superIfc : t.getSuperInterfaces()) {
      getAllSuperInterfaces((IType) superIfc, collector);
    }
  }

  /**
   * <code>null</code> safe check for parameterized types.
   *
   * @param type
   *          The {@link IType} to check or <code>null</code>.
   * @return <code>true</code> if the given {@link IType} is not <code>null</code> and has at least one type parameter.
   *         <code>false</code> otherwise.
   */
  public static boolean isGenericType(IType type) {
    if (type == null) {
      return false;
    }
    return type.hasTypeParameters();
  }

  /**
   * Gets the first {@link IField} with given name.
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param fieldName
   *          The field name.
   * @return The {@link IField} with given name or <code>null</code> if it could not be found.
   */
  public static IField getField(IType declaringType, String fieldName) {
    if (declaringType == null) {
      return null;
    }
    return (IField) CollectionUtils.find(declaringType.getFields(), FieldFilters.getNameFilter(fieldName));
  }

  /**
   * Gets all {@link IField} of the given declaring {@link IType} that matches the given {@link Predicate} in the order
   * as they appear in the source or class file.
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param filter
   *          The {@link Predicate} selecting the {@link IField}s.
   * @return A {@link ListOrderedSet} holding the {@link IField}s accepted by the {@link Predicate}.
   */
  public static ListOrderedSet/*<IField>*/ getFields(IType declaringType, Predicate/*<IField>*/ filter) {
    return getFields(declaringType, filter, null);
  }

  /**
   * Gets all {@link IField}s of the given declaring {@link IType} that matches the given {@link Predicate} sorted by
   * the given {@link Comparator}.
   *
   * @param declaringType
   *          The declaring {@link IType}.
   * @param filter
   *          The {@link Predicate} selecting the {@link IField}s.
   * @param comparator
   *          The {@link Comparator} to sort the {@link IField}s.
   * @return A {@link ListOrderedSet} holding the {@link IField}s accepted by the {@link Predicate} sorted by the given
   *         {@link Comparator}.
   */
  public static ListOrderedSet/*<IField>*/ getFields(IType declaringType, Predicate/*<IField>*/ filter, Comparator<IField> comparator) {
    ListOrderedSet/*<IField>*/ fields = declaringType.getFields();

    if (filter == null) {
      filter = TruePredicate.getInstance();
    }

    if (comparator == null) {
      // no special order requested. keep order as it comes from the declaring type
      List<IField> l = new ArrayList<>(fields.size());
      CollectionUtils.select(fields, filter, l);
      return ListOrderedSet.decorate(l);
    }

    Set<IField> result = new TreeSet<>(comparator);
    CollectionUtils.select(fields, filter, result);
    return ListOrderedSet.decorate(result);
  }

  /**
   * Gets all type parameter arguments.
   *
   * @param focusType
   *          The origin focus type that defines the type argument.
   * @param levelFqn
   *          The fully qualified name of the class on which the value of a type parameter should be extracted.
   * @param typeParamIndex
   *          The index of the type parameter on the given level type whose value should be extracted.
   * @return A {@link ListOrderedSet} holding all type arguments of the given type parameter.
   * @see #getResolvedTypeParamValueSignature(IType, String, int)
   */
  public static ListOrderedSet/*<IType>*/ getResolvedTypeParamValue(IType focusType, String levelFqn, int typeParamIndex) {
    IType levelType = findSuperType(focusType, levelFqn);
    if (levelType == null) {
      return null;
    }
    return getResolvedTypeParamValue(focusType, levelType, typeParamIndex);
  }

  /**
   * Gets all type parameter arguments.
   *
   * @param focusType
   *          The origin focus type that defines the type argument.
   * @param levelType
   *          The {@link IType} on which the value of a type parameter should be extracted.
   * @param typeParamIndex
   *          The index of the type parameter on the given level type whose value should be extracted.
   * @return A {@link ListOrderedSet} holding all type arguments of the given type parameter.
   */
  public static ListOrderedSet/*<IType>*/ getResolvedTypeParamValue(IType focusType, IType levelType, int typeParamIndex) {
    if (levelType == null) {
      return null;
    }
    IType item = levelType.getTypeArguments().get(typeParamIndex);
    if (!item.isAnonymous()) {
      // direct bind
      return ListOrderedSet.decorate(Arrays.asList(item));
    }

    IType superClassGeneric = item.getSuperClass();
    ListOrderedSet/*<IType>*/ superIfcGenerics = item.getSuperInterfaces();
    List<IType> result = null;
    if (superClassGeneric != null) {
      result = new ArrayList<>(superIfcGenerics.size() + 1);
      result.add(superClassGeneric);
    }
    else {
      result = new ArrayList<>(superIfcGenerics.size());
    }

    for (Object ifcGeneric : superIfcGenerics) {
      result.add((IType) ifcGeneric);
    }

    return ListOrderedSet.decorate(result);
  }

  /**
   * Searches for the first {@link IMethod} in the super hierarchy of the given {@link IType} matching the given
   * {@link Predicate}.
   *
   * @param startType
   *          The start {@link IType}.
   * @param filter
   *          The {@link Predicate} to select the {@link IMethod}.
   * @return The first {@link IMethod} or <code>null</code> if it cannot be found.
   */
  public static IMethod findMethodInSuperHierarchy(IType startType, Predicate/*<IMethod>*/ filter) {
    if (startType == null) {
      return null;
    }

    IMethod method = getMethod(startType, filter);
    if (method != null) {
      return method;
    }

    method = findMethodInSuperHierarchy(startType.getSuperClass(), filter);
    if (method != null) {
      return method;
    }

    for (Object ifc : startType.getSuperInterfaces()) {
      method = findMethodInSuperHierarchy((IType) ifc, filter);
      if (method != null) {
        return method;
      }
    }
    return null;
  }

  /**
   * Searches for the first direct inner {@link IType} matching the given {@link Predicate} checking the entire super
   * hierarchy of the given {@link IType}.
   *
   * @param declaringType
   *          The {@link IType} to start searching
   * @param filter
   *          The {@link Predicate} to select the member {@link IType}.
   * @return The first member {@link IType} or <code>null</code> if it cannot be found.
   */
  public static IType findInnerTypeInSuperHierarchy(IType declaringType, Predicate/*<IType>*/ filter) {
    if (declaringType == null) {
      return null;
    }

    ListOrderedSet/*<IType>*/ innerTypes = getInnerTypes(declaringType, filter);
    if (!innerTypes.isEmpty()) {
      return (IType) innerTypes.get(0);
    }
    return findInnerTypeInSuperHierarchy(declaringType.getSuperClass(), filter);
  }

  /**
   * Collects all property beans declared directly in the given type by search methods with the following naming
   * convention:
   *
   * <pre>
   * public <em>&lt;PropertyType&gt;</em> get<em>&lt;PropertyName&gt;</em>();
   * public void set<em>&lt;PropertyName&gt;</em>(<em>&lt;PropertyType&gt;</em> a);
   * </pre>
   *
   * If <code>PropertyType</code> is a boolean property, the following getter is expected
   *
   * <pre>
   * public boolean is<em>&lt;PropertyName&gt;</em>();
   * </pre>
   *
   * @param type
   *          the type within properties are searched
   * @param propertyFilter
   *          optional property bean {@link Predicate} used to filter the result
   * @param comparator
   *          optional property bean {@link Comparator} used to sort the result
   * @return Returns a {@link Set} of property bean descriptions.
   * @see <a href="http://www.oracle.com/technetwork/java/javase/documentation/spec-136004.html">JavaBeans Spec</a>
   */
  @SuppressWarnings("unchecked")
  public static Set<IPropertyBean> getPropertyBeans(IType type, Predicate/*<IPropertyBean>*/ propertyFilter, Comparator<IPropertyBean> comparator) {
    Predicate/*<IMethod>*/ filter = MethodFilters.getMultiMethodFilter(MethodFilters.getFlagsFilter(Flags.AccPublic), MethodFilters.getNameRegexFilter(BEAN_METHOD_NAME));
    ListOrderedSet/*<IMethod>*/ methods = getMethods(type, filter);
    Map<String, PropertyBean> beans = new HashMap<>(methods.size());
    for (Object m : methods) {
      Matcher matcher = BEAN_METHOD_NAME.matcher(((IMethod) m).getName());
      if (matcher.matches()) {
        String kind = matcher.group(1);
        String name = matcher.group(2);

        List<IMethodParameter> parameterTypes = ((IMethod) m).getParameters();
        IType returnType = ((IMethod) m).getReturnType();
        if ("get".equals(kind) && parameterTypes.size() == 0 && !returnType.equals(IType.VOID)) {
          PropertyBean desc = beans.get(name);
          if (desc == null) {
            desc = new PropertyBean(type, name);
            beans.put(name, desc);
          }
          if (desc.getReadMethod() == null) {
            desc.setReadMethod((IMethod) m);
          }
        }
        else {
          boolean isBool = Boolean.class.getName().equals(returnType.getName()) || "boolean".equals(returnType.getName());
          if ("is".equals(kind) && parameterTypes.size() == 0 && isBool) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.getReadMethod() == null) {
              desc.setReadMethod((IMethod) m);
            }
          }
          else if ("set".equals(kind) && parameterTypes.size() == 1 && returnType.equals(IType.VOID)) {
            PropertyBean desc = beans.get(name);
            if (desc == null) {
              desc = new PropertyBean(type, name);
              beans.put(name, desc);
            }
            if (desc.getWriteMethod() == null) {
              desc.setWriteMethod((IMethod) m);
            }
          }
        }
      }
    }

    // filter
    if (propertyFilter == null) {
      propertyFilter = TruePredicate.getInstance();
    }

    if (comparator == null) {
      List<IPropertyBean> l = new ArrayList<>(beans.size());
      CollectionUtils.select(beans.values(), propertyFilter, l);
      return ListOrderedSet.decorate(l);
    }

    Set<IPropertyBean> result = new TreeSet<>(comparator);
    CollectionUtils.select(beans.values(), propertyFilter, result);
    return ListOrderedSet.decorate(result);
  }

  /**
   * Gets the first {@link IAnnotation} on the given {@link IAnnotatable} having the given name.
   *
   * @param annotatable
   *          The {@link IAnnotation} holder.
   * @param name
   *          Simple or fully qualified name of the annotation type.
   * @return The first {@link IAnnotation} on the given {@link IAnnotatable} having the given name or <code>null</code>
   *         if it could not be found.
   */
  public static IAnnotation getAnnotation(IAnnotatable annotatable, String name) {
    if (annotatable == null) {
      return null;
    }
    ListOrderedSet/*<IAnnotation>*/ annotations = getAnnotations(annotatable, name, true);
    if (CollectionUtils.isEmpty(annotations)) {
      return null;
    }
    return (IAnnotation) annotations.get(0);
  }

  /**
   * Gets all {@link IAnnotation}s on the given {@link IAnnotatable} having the given name.
   *
   * @param annotatable
   *          The {@link IAnnotation} holder.
   * @param name
   *          Simple or fully qualified name of the annotation type.
   * @return A {@link ListOrderedSet} holding all {@link IAnnotation}s having the given name.
   */
  public static ListOrderedSet/*<IAnnotation>*/ getAnnotations(IAnnotatable annotatable, String name) {
    return getAnnotations(annotatable, name, false);
  }

  private static ListOrderedSet/*<IAnnotation>*/ getAnnotations(IAnnotatable annotatable, String name, boolean onlyFirst) {
    if (annotatable == null) {
      return null;
    }
    ListOrderedSet/*<IAnnotation>*/ candidates = annotatable.getAnnotations();
    if (name == null || candidates.size() == 0) {
      return candidates;
    }
    String simpleName = Signature.getSimpleName(name);

    List<IAnnotation> result = new ArrayList<>(onlyFirst ? 1 : candidates.size());
    for (Object candidate : candidates) {
      if (name.equals(((IAnnotation) candidate).getType().getName()) || simpleName.equals(((IAnnotation) candidate).getType().getSimpleName())) {
        result.add((IAnnotation) candidate);
        if (onlyFirst) {
          return ListOrderedSet.decorate(result); // cancel after first
        }
      }
    }
    return ListOrderedSet.decorate(result);
  }

  /**
   * Checks if a type with given name exists in the given {@link ILookupEnvironment} (classpath).
   *
   * @param typeToSearchFqn
   *          The fully qualified name to search. See {@link ILookupEnvironment#existsType(String)} for detailed
   *          constraints on the name.
   * @param context
   *          The context to search in.
   * @return <code>true</code> if the given type exists, <code>false</code> otherwise.
   */
  public static boolean isOnClasspath(String typeToSearchFqn, ILookupEnvironment context) {
    if (StringUtils.isBlank(typeToSearchFqn)) {
      return false;
    }
    return context.existsType(typeToSearchFqn);

  }

  /**
   * Checks if the given {@link IType} exists in the given {@link ILookupEnvironment} (classpath).
   *
   * @param typeToSearch
   *          The {@link IType} to search
   * @param context
   *          The context to search in.
   * @return <code>true</code> if the given type exists, <code>false</code> otherwise.
   */
  public static boolean isOnClasspath(IType typeToSearch, ILookupEnvironment context) {
    if (typeToSearch == null) {
      return false;
    }
    return isOnClasspath(typeToSearch.getName(), context);
  }

  /**
   * Finds the super {@link IType} of the given start {@link IType} having the given name.
   *
   * @param typeToCheck
   *          The start {@link IType}
   * @param queryType
   *          The fully qualified name of the super {@link IType} to find.
   * @return The {@link IType} having the given name if found in the super hierarchy of the given {@link IType} or
   *         <code>null</code> if it could not be found.
   */
  public static IType findSuperType(IType typeToCheck, String queryType) {
    if (queryType == null) {
      return null;
    }
    if (typeToCheck == null) {
      return null;
    }

    if (queryType.equals(typeToCheck.getName())) {
      return typeToCheck;
    }

    IType result = findSuperType(typeToCheck.getSuperClass(), queryType);
    if (result != null) {
      return result;
    }

    for (Object superInterface : typeToCheck.getSuperInterfaces()) {
      result = findSuperType((IType) superInterface, queryType);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * Checks if the given {@link IType} has the given queryType in its super hierarchy.
   *
   * @param typeToCheck
   *          The {@link IType} to check.
   * @param queryType
   *          The fully qualified name of the super type to check.
   * @return
   */
  public static boolean isInstanceOf(IType typeToCheck, String queryType) {
    return findSuperType(typeToCheck, queryType) != null;
  }

  /**
   * Gets the first {@link IMethod} which is directly in the given {@link IType} and accepts the given {@link Predicate}
   * .
   *
   * @param type
   *          The {@link IType} to search in.
   * @param filter
   *          The {@link Predicate} to select the {@link IMethod}.
   * @return The first {@link IMethod} or <code>null</code>.
   */
  public static IMethod getMethod(IType type, Predicate/*<IMethod>*/ filter) {
    if (type == null) {
      return null;
    }
    if (filter == null) {
      filter = TruePredicate.getInstance();
    }
    return (IMethod) CollectionUtils.find(type.getMethods(), filter);
  }

  /**
   * Searches and returns the first method with the given name in the given type.<br>
   * If multiple methods with the same name exist (overloads), the returned method is undefined.
   *
   * @param type
   *          The type in which the method should be searched.
   * @param methodName
   *          The name of the method.
   * @return The first method found or null.
   */
  public static IMethod getMethod(IType type, final String methodName) {
    return getMethod(type, MethodFilters.getNameFilter(methodName));
  }

  /**
   * Gets all methods in the given type.<br>
   * The methods are in no particular order.
   *
   * @param type
   *          The type to get all methods of.
   * @return A {@link Set} of all methods of the given type. Never returns null.
   */
  public static ListOrderedSet/*<IMethod>*/ getMethods(IType type) {
    return getMethods(type, null);
  }

  /**
   * Gets all methods in the given type that match the given filter.<br>
   * The methods are in no particular order.
   *
   * @param type
   *          The type to get all methods of.
   * @param filter
   *          The filter.
   * @return A {@link Set} of all methods of the given type matching the given filter. Never returns null.
   */
  public static ListOrderedSet/*<IMethod>*/ getMethods(IType type, Predicate/*<IMethod>*/ filter) {
    return getMethods(type, filter, null);
  }

  /**
   * Gets all methods in the given type (no methods of inner types) that match the given filter ordered by the given
   * comparator.<br>
   * If the given comparator is null, the order of the methods is undefined.
   *
   * @param type
   *          The type to get all methods of.
   * @param filter
   *          The filter to use or null for no filtering.
   * @param comparator
   *          The comparator to use or null to get the methods in undefined order.
   * @return an {@link Set} of all methods of the given type matching the given filter. Never returns null.
   */
  public static ListOrderedSet/*<IMethod>*/ getMethods(IType type, Predicate/*<IMethod>*/ filter, Comparator<IMethod> comparator) {
    @SuppressWarnings("unchecked")
    Set<IMethod> methods = type.getMethods();

    if (filter == null) {
      filter = TruePredicate.getInstance();
    }

    if (comparator == null) {
      List<IMethod> l = new ArrayList<>(methods.size());
      CollectionUtils.select(methods, filter, l);
      return ListOrderedSet.decorate(l);
    }

    Set<IMethod> result = new TreeSet<>(comparator);
    CollectionUtils.select(methods, filter, result);
    return ListOrderedSet.decorate(result);
  }

  /**
   * Gets the primary {@link IType} of the given {@link IType}.
   *
   * @param t
   *          The {@link IType} for which the primary {@link IType} should be returned.
   * @return The primary {@link IType} of t or <code>null</code>.
   */
  public static IType getPrimaryType(IType t) {
    IType result = null;
    IType tmp = t;
    while (tmp != null) {
      result = tmp;
      tmp = tmp.getDeclaringType();
    }

    return result;
  }

  /**
   * Gets the value of the given attribute in the given {@link IAnnotation} as a {@link String}.
   *
   * @param annotation
   *          The {@link IAnnotation} in which the attribute should be searched.
   * @param name
   *          The name of the attribute.
   * @return The value of the attribute with given name in the given {@link IAnnotation} or <code>null</code> if there
   *         is not such attribute or no value.
   */
  public static String getAnnotationValueString(IAnnotation annotation, String name) {
    if (annotation == null) {
      return null;
    }

    IAnnotationValue value = annotation.getValue(name);
    if (value == null) {
      return null;
    }

    Object rawVal = value.getValue();
    if (rawVal == null) {
      return null;
    }

    return rawVal.toString();
  }

  /**
   * Gets the value of the given attribute in the given {@link IAnnotation} as a {@link BigDecimal}.
   *
   * @param annotation
   *          The {@link IAnnotation} in which the attribute should be searched.
   * @param name
   *          The name of the attribute.
   * @return The value of the attribute with given name in the given {@link IAnnotation} or <code>null</code> if there
   *         is not such attribute or no value or it is not numeric.
   */
  public static BigDecimal getAnnotationValueNumeric(IAnnotation annotation, String name) {
    if (annotation == null) {
      return null;
    }

    IAnnotationValue value = annotation.getValue(name);
    if (value == null) {
      return null;
    }

    Object rawVal = value.getValue();
    if (rawVal == null) {
      return null;
    }

    switch (value.getValueType()) {
      case Int:
      case Byte:
      case Short:
        return new BigDecimal(((Number) rawVal).intValue());
      case Long:
        return new BigDecimal(((Long) rawVal).longValue());
      case Float:
      case Double:
        return new BigDecimal(((Number) rawVal).doubleValue());
      default:
        return null;
    }
  }
}
