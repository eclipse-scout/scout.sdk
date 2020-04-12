/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.builder.java.body.IMethodBodyBuilder;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;
import org.eclipse.scout.sdk.core.util.JavaTypes;

/**
 * <h3>{@link PropertyBean}</h3><br>
 * Description of a Java bean property.
 */
public class PropertyBean {

  public static final String GETTER_PREFIX = "get";
  public static final String SETTER_PREFIX = "set";
  public static final String GETTER_BOOL_PREFIX = "is";

  /**
   * Regular expression matching bean method names (is..., get..., set...)
   */
  @SuppressWarnings("HardcodedFileSeparator")
  public static final Pattern BEAN_METHOD_NAME = Pattern.compile('(' + GETTER_PREFIX + '|' + SETTER_PREFIX + '|' + GETTER_BOOL_PREFIX + ")([A-Z]\\w*)");
  @SuppressWarnings("HardcodedFileSeparator")
  public static final Pattern GETTER_METHOD_NAME = Pattern.compile('(' + GETTER_PREFIX + '|' + GETTER_BOOL_PREFIX + ")([A-Z]\\w*)");
  @SuppressWarnings("HardcodedFileSeparator")
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
   * @see <a href="http://www.oracle.com/technetwork/java/javase/documentation/spec-136004.html">JavaBeans Spec</a>
   */
  public static Stream<PropertyBean> of(IType type) {
    List<IMethod> methods = type.methods()
        .withFlags(Flags.AccPublic).stream()
        .collect(toList());

    Map<String, PropertyBean> beans = new HashMap<>();
    for (IMethod m : methods) {
      Optional<String> getterName = getterName(m);
      if (getterName.isPresent()) {
        beans.computeIfAbsent(getterName.get(),
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
  public static String getterPrefixFor(String returnType) {
    if (JavaTypes._boolean.equals(returnType)) {
      return GETTER_BOOL_PREFIX;
    }
    return GETTER_PREFIX;
  }

  /**
   * Gets the property name (method name without the getter prefix) for the given getter {@link IMethodGenerator}.
   *
   * @param m
   *          The {@link IMethodGenerator}. Must not be {@code null}.
   * @return An {@link Optional} containing the property name if the given {@link IMethodGenerator} is a valid getter.
   *         Otherwise an empty {@link Optional} is returned.
   */
  public static Optional<String> getterName(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> m) {
    return nameOf(m, false);
  }

  /**
   * Gets the property name (method name without getter prefix) for the given getter {@link IMethod}.
   *
   * @param m
   *          The {@link IMethod} to check. Must not be {@code null}.
   * @return An {@link Optional} containing the property name if the given {@link IMethod} is a valid getter. Otherwise
   *         an empty {@link Optional} is returned.
   */
  public static Optional<String> getterName(IMethod m) {
    return nameOf(m, false);
  }

  protected static Optional<String> getterName(CharSequence methodName, int numParams, String returnType) {
    if (numParams != 0) {
      return Optional.empty();
    }
    if (JavaTypes._void.equals(returnType)) {
      return Optional.empty();
    }

    Matcher matcher = GETTER_METHOD_NAME.matcher(methodName);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    String kind = matcher.group(1);
    String name = matcher.group(2);

    if (GETTER_PREFIX.equals(kind)) {
      return Optional.of(name);
    }
    boolean isBool = JavaTypes._boolean.equals(returnType) || JavaTypes.Boolean.equals(returnType);
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
   * @return An {@link Optional} containing the property name if the given {@link IMethodGenerator} is a valid setter.
   *         Otherwise an empty {@link Optional} is returned.
   */
  public static Optional<String> setterName(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> m) {
    return nameOf(m, true);
  }

  /**
   * Gets the property name (method name without the setter prefix) for the given setter {@link IMethod}.
   *
   * @param m
   *          The {@link IMethod} to check. Must not be {@code null}.
   * @return An {@link Optional} containing the property name if the given {@link IMethod} is a valid setter. Otherwise
   *         an empty {@link Optional} is returned.
   */
  public static Optional<String> setterName(IMethod m) {
    return nameOf(m, true);
  }

  protected static Optional<String> setterName(CharSequence methodName, int numParams, String returnType) {
    if (numParams != 1) {
      return Optional.empty();
    }
    if (!JavaTypes._void.equals(returnType)) {
      return Optional.empty();
    }

    Matcher matcher = SETTER_METHOD_NAME.matcher(methodName);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    return Optional.of(matcher.group(2));
  }

  protected static Optional<String> nameOf(IMethodGenerator<?, ? extends IMethodBodyBuilder<?>> m, boolean setter) {
    if (!m.returnType().isPresent()) {
      return Optional.empty();
    }
    CharSequence methodName = m.elementName().orElseThrow(() -> newFail("Method name is missing."));
    //noinspection NumericCastThatLosesPrecision
    int numParams = (int) m.parameters().count();
    if (setter) {
      return setterName(methodName, numParams, m.returnType().get());
    }
    return getterName(methodName, numParams, m.returnType().get());
  }

  protected static Optional<String> nameOf(IMethod m, boolean setter) {
    Optional<IType> returnType = m.returnType();
    if (!returnType.isPresent()) {
      return Optional.empty();
    }

    //noinspection NumericCastThatLosesPrecision
    int numParams = (int) m.parameters().stream().count();
    String returnDataType = m.requireReturnType().name();
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
            .map(writeMethod -> writeMethod.parameters().first().get().dataType())
            .get());
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
    int prime = 31;
    int result = 1;
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
    PropertyBean other = (PropertyBean) obj;
    return declaringType().equals(other.declaringType()) && name().equals(other.name());
  }

  @Override
  public String toString() {
    return declaringType().name() + '#' + name();
  }
}
