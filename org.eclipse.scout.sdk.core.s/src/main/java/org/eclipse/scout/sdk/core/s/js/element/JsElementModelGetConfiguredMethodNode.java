/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.js.element;

import static java.beans.Introspector.decapitalize;
import static org.eclipse.scout.sdk.core.util.Strings.removePrefix;
import static org.eclipse.scout.sdk.core.util.Strings.removeSuffix;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;

public class JsElementModelGetConfiguredMethodNode extends JsElementModelMethodNode implements IJsElementModelGetConfiguredMethodNode {

  private static final String DOUBLE_QUOTE = "\"";
  private static final String SEMICOLON = ";";
  private static final String PARENTHESIS_OPEN = "(";
  private static final String PARENTHESIS_CLOSE = ")";

  private static final String GET_CONFIGURED_PREFIX = "getConfigured";
  private static final String RETURN_PREFIX = "return ";
  private static final String TEXTS_PREFIX = "TEXTS.get";

  public enum PropertyType {
    STRING,
    TEXT_KEY,
    BOOLEAN,
    BIG_INTEGER
//    val NUMERIC = JsPropertyDataType("numeric")
//    val UNKNOWN = JsPropertyDataType("unknown")
  }

  private final PropertyType m_propertyType;
  private final String m_propertyIdentifier;
  private final Object m_value;

  protected JsElementModelGetConfiguredMethodNode(IMethod method) {
    super(method);
    Ensure.isTrue(name().startsWith(GET_CONFIGURED_PREFIX));
    Ensure.notNull(returnType().orElse(null));
    m_propertyType = parseType();
    m_propertyIdentifier = parsePropertyIdentifier();
    m_value = parseValue();
  }

  public static JsElementModelGetConfiguredMethodNode create(IMethod method) {
    return new JsElementModelGetConfiguredMethodNode(method);
  }

  private PropertyType parseType() {
    var type = requireReturnType();
    if (type.isInstanceOf(String.class.getName())) {
      if (sourceOfBody().startsWith(RETURN_PREFIX + TEXTS_PREFIX)) {
        return PropertyType.TEXT_KEY;
      }
      return PropertyType.STRING;
    }
    if (Stream.of(JavaTypes.Boolean, JavaTypes._boolean).anyMatch(type::isInstanceOf)) {
      return PropertyType.BOOLEAN;
    }
    if (Stream.of(JavaTypes.Integer, JavaTypes._int, JavaTypes.Long, JavaTypes._long).anyMatch(type::isInstanceOf)) {
      // TODO fsh is this sufficient?
      return PropertyType.BIG_INTEGER;
    }

    // TODO fsh throw exception or log warning?
    return null;
  }

  private String parsePropertyIdentifier() {
    return decapitalize(removePrefix(name(), GET_CONFIGURED_PREFIX));
  }

  private Object parseValue() {
    var valueRaw = removePrefix(sourceOfBody(), RETURN_PREFIX);
    valueRaw = removeSuffix(valueRaw, SEMICOLON);

    switch (propertyType()) {
      case TEXT_KEY:
        valueRaw = removePrefix(valueRaw, TEXTS_PREFIX);
        valueRaw = removePrefix(valueRaw, PARENTHESIS_OPEN);
        valueRaw = removeSuffix(valueRaw, PARENTHESIS_CLOSE);
      case STRING:
        valueRaw = removePrefix(valueRaw, DOUBLE_QUOTE);
        valueRaw = removeSuffix(valueRaw, DOUBLE_QUOTE);
        return valueRaw;
      case BOOLEAN:
        return Boolean.valueOf(valueRaw);
      case BIG_INTEGER:
        // TODO fsh is this sufficient?
        valueRaw = removeSuffix(valueRaw, "L");
        return BigInteger.valueOf(Long.parseLong(valueRaw));
      default:
        // TODO fsh throw exception or log warning?
        return null;
    }
  }

  @Override
  public PropertyType propertyType() {
    return m_propertyType;
  }

  @Override
  public String propertyIdentifier() {
    return m_propertyIdentifier;
  }

  @Override
  public String stringValue() {
    Ensure.isTrue(propertyType() == PropertyType.STRING);
    return (String) m_value;
  }

  @Override
  public String textKey() {
    Ensure.isTrue(propertyType() == PropertyType.TEXT_KEY);
    return (String) m_value;
  }

  @Override
  public Boolean booleanValue() {
    Ensure.isTrue(propertyType() == PropertyType.BOOLEAN);
    return (Boolean) m_value;
  }

  @Override
  public BigInteger bigIntValue() {
    Ensure.isTrue(propertyType() == PropertyType.BIG_INTEGER);
    return (BigInteger) m_value;
  }
}
