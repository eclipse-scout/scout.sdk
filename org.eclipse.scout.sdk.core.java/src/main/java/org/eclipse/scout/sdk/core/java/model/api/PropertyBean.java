/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.builder.IJavaBuilderContext;
import org.eclipse.scout.sdk.core.java.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link PropertyBean}</h3><br>
 * Description of a Java bean property.
 */
public class PropertyBean {

  public static final String GETTER_PREFIX = "get";
  public static final String SETTER_PREFIX = "set";
  public static final String GETTER_BOOL_PREFIX = "is";
  public static final String CHAINED_SETTER_PREFIX = "with";

  /**
   * Regular expression matching bean method names (is..., get..., set...)
   */
  public static final Pattern BEAN_METHOD_NAME = Pattern.compile('(' + GETTER_PREFIX + '|' + SETTER_PREFIX + '|' + GETTER_BOOL_PREFIX + ")([A-Z]\\w*)");
  public static final Pattern GETTER_METHOD_NAME = Pattern.compile('(' + GETTER_PREFIX + '|' + GETTER_BOOL_PREFIX + ")([A-Z]\\w*)");
  public static final Pattern SETTER_METHOD_NAME = Pattern.compile('(' + SETTER_PREFIX + ")([A-Z]\\w*)");

  private final String m_beanName;
  private final IType m_declaringType;

  private final FinalValue<IMethod> m_readMethod;
  private final FinalValue<IMethod> m_writeMethod;

  protected PropertyBean(IType declaringType, String beanName) {
    m_declaringType = Ensure.notNull(declaringType);
    m_beanName = Ensure.notNull(beanName);
    m_readMethod = new FinalValue<>();
    m_writeMethod = new FinalValue<>();
  }

  /**
   * Collects all property beans declared directly in the given type by search methods with the following naming
   * convention:
   * <p>
   *
   * <pre>
   * public <em>&lt;PropertyType&gt;</em> get<em>&lt;PropertyName&gt;</em>();
   * public void set<em>&lt;PropertyName&gt;</em>(<em>&lt;PropertyType&gt;</em> a);
   * </pre>
   * <p>
   * If {@code PropertyType} is a boolean or {@link Boolean} property, the following getter is expected
   * <p>
   *
   * <pre>
   * public [B|b]oolean is<em>&lt;PropertyName&gt;</em>();
   * </pre>
   *
   * @param type
   *          the type within properties are searched
   * @return Returns a {@link Stream} of property bean descriptions.
   * @see <a href="https://www.oracle.com/java/technologies/javase/javabeans-spec.html">JavaBeans Spec</a>
   */
  public static Stream<PropertyBean> of(IType type) {
    var methods = type.methods().withFlags(Flags.AccPublic).stream().toList();
    Map<String, PropertyBean> beans = new HashMap<>();
    for (var m : methods) {
      var getterName = getterName(m);
      if (getterName.isPresent()) {
        beans.computeIfAbsent(getterName.orElseThrow(),
            key -> new PropertyBean(type, key))
            .setReadMethodIfAbsent(m);
      }
      else {
        setterName(m)
            .ifPresent(name -> beans.computeIfAbsent(name, key -> new PropertyBean(type, key))
                .setWriteMethodIfAbsent(m));
      }
    }

    return beans.values().stream();
  }

  /**
   * Gets the method prefix to use for getter methods having the given return type.
   *
   * @param returnType
   *          The return type of the method this prefix belongs to
   * @return "is" if the return type is {@code boolean}. "get" otherwise. Note: {@code java.lang.Boolean} does also
   *         return "get" to ensure bean specification compliance.
   */
  public static String getterPrefixFor(CharSequence returnType) {
    if (Strings.equals(JavaTypes._boolean, returnType)) {
      return GETTER_BOOL_PREFIX;
    }
    return GETTER_PREFIX;
  }

  /**
   * Gets the property name (method name without the getter prefix) for the given getter {@link IMethodGenerator}.
   *
   * @param m
   *          The {@link IMethodGenerator}. Must not be {@code null}.
   * @param context
   *          Context information to compute the data types from which the setter name depends.
   * @return An {@link Optional} containing the property name if the given {@link IMethodGenerator} is a valid getter.
   *         Otherwise, an empty {@link Optional} is returned.
   */
  public static Optional<String> getterName(IMethodGenerator<?, ?> m, IJavaBuilderContext context) {
    return propertyNameOf(m, false, context);
  }

  /**
   * Gets the property name (method name without getter prefix) for the given getter {@link IMethod}.
   *
   * @param m
   *          The {@link IMethod} to check. Must not be {@code null}.
   * @return An {@link Optional} containing the property name if the given {@link IMethod} is a valid getter. Otherwise,
   *         an empty {@link Optional} is returned.
   */
  public static Optional<String> getterName(IMethod m) {
    return propertyNameOf(m, false);
  }

  protected static Optional<String> getterName(CharSequence methodName, int numParams, String returnType) {
    if (numParams != 0) {
      return Optional.empty();
    }
    if (JavaTypes._void.equals(returnType)) {
      return Optional.empty();
    }

    var matcher = GETTER_METHOD_NAME.matcher(methodName);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    var kind = matcher.group(1);
    var name = matcher.group(2);

    if (GETTER_PREFIX.equals(kind)) {
      return Optional.of(name);
    }
    var isBool = JavaTypes._boolean.equals(returnType) || JavaTypes.Boolean.equals(returnType);
    if (isBool && GETTER_BOOL_PREFIX.equals(kind)) {
      return Optional.of(name);
    }

    return Optional.empty();
  }

  /**
   * Gets the property name (method name without the setter prefix) for the given setter {@link IMethodGenerator}.
   *
   * @param m
   *          The {@link IMethodGenerator}. Must not be {@code null}.
   * @param context
   *          Context information to compute the data types from which the setter name depends.
   * @return An {@link Optional} containing the property name if the given {@link IMethodGenerator} is a valid setter.
   *         Otherwise, an empty {@link Optional} is returned.
   */
  public static Optional<String> setterName(IMethodGenerator<?, ?> m, IJavaBuilderContext context) {
    return propertyNameOf(m, true, context);
  }

  /**
   * Gets the property name (method name without the setter prefix) for the given setter {@link IMethod}.
   *
   * @param m
   *          The {@link IMethod} to check. Must not be {@code null}.
   * @return An {@link Optional} containing the property name if the given {@link IMethod} is a valid setter. Otherwise,
   *         an empty {@link Optional} is returned.
   */
  public static Optional<String> setterName(IMethod m) {
    return propertyNameOf(m, true);
  }

  protected static Optional<String> setterName(CharSequence methodName, int numParams, String returnType) {
    if (numParams != 1) {
      return Optional.empty();
    }
    if (!JavaTypes._void.equals(returnType)) {
      return Optional.empty();
    }

    var matcher = SETTER_METHOD_NAME.matcher(methodName);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    return Optional.of(matcher.group(2));
  }

  protected static Optional<String> propertyNameOf(IMethodGenerator<?, ?> m, boolean setter, IJavaBuilderContext context) {
    var returnType = m.returnType(context);
    if (returnType.isEmpty()) {
      return Optional.empty(); // constructors
    }
    CharSequence methodName = m.elementName(context).orElseThrow(() -> newFail("Method name is missing."));
    //noinspection NumericCastThatLosesPrecision
    var numParams = (int) m.parameters().count();
    if (setter) {
      return setterName(methodName, numParams, returnType.orElseThrow());
    }
    return getterName(methodName, numParams, returnType.orElseThrow());
  }

  protected static Optional<String> propertyNameOf(IMethod m, boolean setter) {
    var returnType = m.returnType();
    if (returnType.isEmpty()) {
      return Optional.empty();
    }

    var numParams = m.parameters().count();
    var returnDataType = returnType.orElseThrow().name();
    if (setter) {
      return setterName(m.elementName(), numParams, returnDataType);
    }
    return getterName(m.elementName(), numParams, returnDataType);
  }

  /**
   * Gets the data type of the bean method.
   *
   * @param getterOrSetter
   *          The getter or setter {@link IMethod}.
   * @return An {@link Optional} holding the bean data type of the method or an empty {@link Optional} if the data type
   *         could not be determined.
   */
  public static Optional<IType> dataTypeOf(IMethod getterOrSetter) {
    if (getterOrSetter == null) {
      return Optional.empty();
    }
    if (getterOrSetter.parameters().existsAny()) {
      return getterOrSetter.parameters()
          .first()
          .map(IMethodParameter::dataType);
    }
    return getterOrSetter.returnType().filter(t -> !t.isVoid());
  }

  /**
   * @return The declaring type that is hosting this property bean.
   */
  public IType declaringType() {
    return m_declaringType;
  }

  /**
   * @return The bean's name.
   */
  public String name() {
    return m_beanName;
  }

  /**
   * @return The data type of the bean.
   */
  public IType type() {
    return readMethod()
        .flatMap(IMethod::returnType)
        .orElseGet(() -> writeMethod()
            .map(writeMethod -> writeMethod.parameters().first().orElseThrow().dataType())
            .orElseThrow());
  }

  /**
   * @return The property's getter method. The resulting {@link Optional} is empty if this bean is write-only (no read
   *         method available).
   */
  public Optional<IMethod> readMethod() {
    return m_readMethod.opt();
  }

  /**
   * @return The property's setter method. The resulting {@link Optional} is empty if this bean is read-only (no write
   *         method available).
   */
  public Optional<IMethod> writeMethod() {
    return m_writeMethod.opt();
  }

  protected void setReadMethodIfAbsent(IMethod readMethod) {
    m_readMethod.setIfAbsent(readMethod);
  }

  protected void setWriteMethodIfAbsent(IMethod writeMethod) {
    m_writeMethod.setIfAbsent(writeMethod);
  }

  @Override
  public int hashCode() {
    var prime = 31;
    var result = 1;
    result = prime * result + name().hashCode();
    result = prime * result + declaringType().hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    var other = (PropertyBean) obj;
    return declaringType().equals(other.declaringType()) && name().equals(other.name());
  }

  @Override
  public String toString() {
    return declaringType().name() + '#' + name();
  }
}
