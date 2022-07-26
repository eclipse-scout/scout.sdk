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

import org.eclipse.scout.sdk.core.util.Ensure;

public class JsModelPropertyGenerator<TYPE extends JsModelPropertyGenerator<TYPE>> extends AbstractJsSourceGenerator<TYPE> implements IJsModelPropertyGenerator<TYPE> {

  private String m_identifier;
  private IJsValueGenerator<?, ?> m_jsValueGenerator;

  public static JsModelPropertyGenerator<?> create() {
    return new JsModelPropertyGenerator<>();
  }

  @Override
  public void generate(IJsSourceBuilder<?> builder) {
    super.generate(builder);

    builder
        .append(identifier().get())
        .colon().space();

    jsValueGenerator().get().generate(builder);
  }

  @Override
  protected void setupImpl() {
    super.setupImpl();
    Ensure.notNull(identifier().orElse(null), "Identifier missing");
    Ensure.notNull(jsValueGenerator().orElse(null), "JsValueGenerator missing");
  }

  @Override
  public Optional<String> identifier() {
    return Optional.ofNullable(m_identifier);
  }

  @Override
  public TYPE withIdentifier(String identifier) {
    m_identifier = identifier;
    return thisInstance();
  }

  protected Optional<IJsValueGenerator<?, ?>> jsValueGenerator() {
    return Optional.ofNullable(m_jsValueGenerator);
  }

  @Override
  public TYPE withJsValueGenerator(IJsValueGenerator<?, ?> jsValueGenerator) {
    m_jsValueGenerator = jsValueGenerator;
    return thisInstance();
  }
}
