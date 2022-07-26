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

import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.s.js.element.JsElementModel;

public class PrimaryJsModelGenerator<TYPE extends PrimaryJsModelGenerator<TYPE>> extends AbstractJsSourceGenerator<TYPE> implements IJsModelGenerator<TYPE> {

  private final JsModelGenerator<?> m_primaryJsModel;

  protected PrimaryJsModelGenerator() {
    m_primaryJsModel = JsModelGenerator.create();
  }

  public static PrimaryJsModelGenerator<?> create() {
    return new PrimaryJsModelGenerator<>();
  }

  @Override
  public void generate(IJsSourceBuilder<?> builder) {
    super.generate(builder);

    builder.exportDefault().space().parenthesisOpen().parenthesisClose().space().arrow().space().parenthesisOpen();
    primaryJsModel().generate(builder);
    builder.parenthesisClose().semicolon().nl();
  }

  @Override
  protected void setupImpl() {
    super.setupImpl();
    primaryJsModel().setup();
    primaryJsModel().withoutJsModelPropertyGenerator(generator -> generator.identifier()
        .map(IJsModelPropertyGenerator.OBJECT_TYPE_PROPERTY::equals)
        .orElse(false));
  }

  public JsModelGenerator<?> primaryJsModel() {
    return m_primaryJsModel;
  }

  @Override
  public TYPE withJsModelPropertyGenerator(IJsModelPropertyGenerator<?> jsModelPropertyGenerator) {
    primaryJsModel().withJsModelPropertyGenerator(jsModelPropertyGenerator);
    return thisInstance();
  }

  @Override
  public TYPE withoutJsModelPropertyGenerator(Predicate<IJsModelPropertyGenerator<?>> removalFilter) {
    primaryJsModel().withoutJsModelPropertyGenerator(removalFilter);
    return thisInstance();
  }

  @Override
  public TYPE withJsElementModel(JsElementModel jsElementModel) {
    primaryJsModel().withJsElementModel(jsElementModel);
    return thisInstance();
  }
}
