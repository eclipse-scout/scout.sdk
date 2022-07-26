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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.util.Ensure;

public class JsArrayValueGenerator<VALUE, TYPE extends JsArrayValueGenerator<VALUE, TYPE>> extends AbstractJsValueGenerator<List<VALUE>, TYPE> implements IJsArrayValueGenerator<VALUE, TYPE> {

  private Supplier<IJsValueGenerator<VALUE, ?>> m_jsValueGeneratorSupplier;

  public static <T> JsArrayValueGenerator<T, ?> create(Class<T> clazz) {
    return new JsArrayValueGenerator<>();
  }

  @Override
  protected void generateValue(List<VALUE> values, IJsSourceBuilder<?> builder) {
    builder.arrayStart().nl();

    var appendCommaAndNl = new AtomicBoolean();
    values
        .forEach(value -> {
          if (appendCommaAndNl.getAndSet(true)) {
            builder.comma().nl();
          }
          createJsValueGenerator()
              .withValue(value)
              .generate(builder);
        });

    builder.nl().arrayEnd();
  }

  @Override
  protected void setupImpl() {
    super.setupImpl();
    Ensure.notNull(jsValueGeneratorSupplier().orElse(null), "IJsValueGenerator supplier missing");
  }

  protected IJsValueGenerator<VALUE, ?> createJsValueGenerator() {
    return jsValueGeneratorSupplier().orElseThrow().get();
  }

  protected Optional<Supplier<IJsValueGenerator<VALUE, ?>>> jsValueGeneratorSupplier() {
    return Optional.ofNullable(m_jsValueGeneratorSupplier);
  }

  @Override
  public TYPE withJsValueGeneratorSupplier(Supplier<IJsValueGenerator<VALUE, ?>> jsValueGeneratorSupplier) {
    m_jsValueGeneratorSupplier = jsValueGeneratorSupplier;
    return thisInstance();
  }
}
