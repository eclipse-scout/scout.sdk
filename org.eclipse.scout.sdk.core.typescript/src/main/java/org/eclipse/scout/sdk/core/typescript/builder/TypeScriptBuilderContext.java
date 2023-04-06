/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.builder;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.typescript.builder.imports.ES6ImportValidator;
import org.eclipse.scout.sdk.core.typescript.builder.imports.IES6ImportValidator;
import org.eclipse.scout.sdk.core.util.PropertySupport;

public class TypeScriptBuilderContext implements ITypeScriptBuilderContext {

  private final IBuilderContext m_inner;
  private final ES6ImportValidator m_validator;

  public TypeScriptBuilderContext(IBuilderContext context) {
    m_inner = Optional.ofNullable(context).orElseGet(BuilderContext::new);
    m_validator = new ES6ImportValidator();
  }

  public IBuilderContext builderContext() {
    return m_inner;
  }

  @Override
  public String lineDelimiter() {
    return builderContext().lineDelimiter();
  }

  @Override
  public PropertySupport properties() {
    return builderContext().properties();
  }

  @Override
  public IES6ImportValidator importValidator() {
    return m_validator;
  }
}
