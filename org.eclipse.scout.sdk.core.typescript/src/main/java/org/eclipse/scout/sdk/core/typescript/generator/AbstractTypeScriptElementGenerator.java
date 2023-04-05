/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator;

import static org.eclipse.scout.sdk.core.util.Strings.notBlank;

import java.util.Optional;

import org.eclipse.scout.sdk.core.builder.ISourceBuilder;
import org.eclipse.scout.sdk.core.typescript.builder.ITypeScriptSourceBuilder;
import org.eclipse.scout.sdk.core.typescript.builder.TypeScriptSourceBuilder;

/**
 * <h3>{@link AbstractTypeScriptElementGenerator}</h3>
 *
 * @since 13.0
 */
public abstract class AbstractTypeScriptElementGenerator<TYPE extends ITypeScriptElementGenerator<TYPE>> implements ITypeScriptElementGenerator<TYPE> {

  private String m_elementName;
  private boolean m_setupDone;

  protected AbstractTypeScriptElementGenerator() {
  }

  @SuppressWarnings("unchecked")
  protected TYPE thisInstance() {
    return (TYPE) this;
  }

  @Override
  public Optional<String> elementName() {
    return notBlank(m_elementName);
  }

  @Override
  public TYPE withElementName(String newName) {
    m_elementName = newName;
    return thisInstance();
  }

  /**
   * Callback for child classes to set up the template. This method is only called once for each
   * {@link AbstractTypeScriptElementGenerator}.
   */
  protected void setup() {
    // hook for subclasses
  }

  @SuppressWarnings("unused")
  protected void build(ITypeScriptSourceBuilder<?> builder) {
    // hook for subclasses
  }

  public static ITypeScriptSourceBuilder<?> ensureTypeScriptSourceBuilder(ISourceBuilder<?> inner) {
    if (inner instanceof ITypeScriptSourceBuilder<?>) {
      return (ITypeScriptSourceBuilder<?>) inner;
    }
    return TypeScriptSourceBuilder.create(inner);
  }

  @Override
  public final void generate(ISourceBuilder<?> builder) {
    if (!isSetupDone()) {
      setup();
      setSetupDone();
    }
    build(ensureTypeScriptSourceBuilder(builder));
  }

  private boolean isSetupDone() {
    return m_setupDone;
  }

  private void setSetupDone() {
    m_setupDone = true;
  }
}
