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

public class JsBooleanValueGenerator<TYPE extends JsBooleanValueGenerator<TYPE>> extends AbstractJsValueGenerator<Boolean, TYPE> {

  public static JsBooleanValueGenerator<?> create() {
    return new JsBooleanValueGenerator<>();
  }

  @Override
  protected void generateValue(Boolean b, IJsSourceBuilder<?> builder) {
    builder.append(b);
  }
}
