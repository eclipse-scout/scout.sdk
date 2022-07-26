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

public class JsTextKeyValueGenerator<TYPE extends JsTextKeyValueGenerator<TYPE>> extends AbstractJsValueGenerator<String, TYPE> {

  private static final String TEXT_KEY_PREFIX = "${textKey:";
  private static final String TEXT_KEY_SUFFIX = "}";

  public static JsTextKeyValueGenerator<?> create() {
    return new JsTextKeyValueGenerator<>();
  }

  @Override
  protected void generateValue(String s, IJsSourceBuilder<?> builder) {
    builder.stringLiteral(TEXT_KEY_PREFIX + s + TEXT_KEY_SUFFIX);
  }
}
