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

public class JsNumberValueGenerator<TYPE extends JsNumberValueGenerator<TYPE>> extends AbstractJsValueGenerator<Number, TYPE> {

  public static JsNumberValueGenerator<?> create() {
    return new JsNumberValueGenerator<>();
  }

  @Override
  protected void generateValue(Number number, IJsSourceBuilder<?> builder) {
    builder.append(number);
  }
}
