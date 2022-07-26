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
package org.eclipse.scout.sdk.core.s.js.element.gen;

import java.util.Optional;

import org.eclipse.scout.sdk.core.util.Strings;

public abstract class AbstractJsSourceBuilder<TYPE extends AbstractJsSourceBuilder<TYPE>> implements IJsSourceBuilder<TYPE> {

  @SuppressWarnings("HardcodedLineSeparator")
  private static final String NL = "\n";

  private static final String SPACE = " ";
  private static final String NULL = "null";

  private static final String COLON = ":";
  private static final String SEMICOLON = ";";
  private static final String COMMA = ",";
  private static final String ARROW = "=>";

  private static final String PARENTHESIS_OPEN = "(";
  private static final String PARENTHESIS_CLOSE = ")";
  private static final String OBJECT_START = "{";
  private static final String OBJECT_END = "}";
  private static final String ARRAY_START = "[";
  private static final String ARRAY_END = "]";

  private static final String EXPORT_DEFAULT = "export default";

  private final StringBuilder m_builder = new StringBuilder();

  private String m_lineSeparator;

  protected StringBuilder getBuilder() {
    return m_builder;
  }

  @SuppressWarnings("unchecked")
  protected TYPE thisInstance() {
    return (TYPE) this;
  }

  @Override
  public String source() {
    return getBuilder().toString();
  }

  protected String lineSeparator() {
    return Optional.ofNullable(m_lineSeparator).orElse(NL);
  }

  @Override
  public TYPE withLineSeparator(String lineSeparator) {
    m_lineSeparator = lineSeparator;
    return thisInstance();
  }

  @Override
  public TYPE append(String s) {
    getBuilder().append(s);
    return thisInstance();
  }

  @Override
  public TYPE append(CharSequence cs) {
    getBuilder().append(cs);
    return thisInstance();
  }

  @Override
  public TYPE append(boolean b) {
    getBuilder().append(b);
    return thisInstance();
  }

  @Override
  public TYPE append(Number n) {
    getBuilder().append(n);
    return thisInstance();
  }

  @Override
  public TYPE nl() {
    return append(lineSeparator());
  }

  @Override
  public TYPE space() {
    return append(SPACE);
  }

  @Override
  public TYPE stringLiteral(CharSequence literalValue) {
    if (literalValue == null) {
      return nullLiteral();
    }
    return append(Strings.toStringLiteral(literalValue, "'", true));
  }

  @Override
  public TYPE nullLiteral() {
    return append(NULL);
  }

  @Override
  public TYPE colon() {
    return append(COLON);
  }

  @Override
  public TYPE semicolon() {
    return append(SEMICOLON);
  }

  @Override
  public TYPE comma() {
    return append(COMMA);
  }

  @Override
  public TYPE arrow() {
    return append(ARROW);
  }

  @Override
  public TYPE parenthesisOpen() {
    return append(PARENTHESIS_OPEN);
  }

  @Override
  public TYPE parenthesisClose() {
    return append(PARENTHESIS_CLOSE);
  }

  @Override
  public TYPE objectStart() {
    return append(OBJECT_START);
  }

  @Override
  public TYPE objectEnd() {
    return append(OBJECT_END);
  }

  @Override
  public TYPE arrayStart() {
    return append(ARRAY_START);
  }

  @Override
  public TYPE arrayEnd() {
    return append(ARRAY_END);
  }

  @Override
  public TYPE exportDefault() {
    return append(EXPORT_DEFAULT);
  }
}
