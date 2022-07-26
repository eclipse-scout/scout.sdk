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

public abstract class AbstractJsValueGenerator<VALUE, TYPE extends AbstractJsValueGenerator<VALUE, TYPE>> extends AbstractJsSourceGenerator<TYPE> implements IJsValueGenerator<VALUE, TYPE> {

  private VALUE m_value;

  @Override
  public void generate(IJsSourceBuilder<?> builder) {
    super.generate(builder);

    value().ifPresentOrElse(value -> generateValue(value, builder), builder::nullLiteral);
  }

  protected abstract void generateValue(VALUE value, IJsSourceBuilder<?> builder);

  protected Optional<VALUE> value() {
    return Optional.ofNullable(m_value);
  }

  @Override
  public TYPE withValue(VALUE value) {
    m_value = value;
    return thisInstance();
  }
}
