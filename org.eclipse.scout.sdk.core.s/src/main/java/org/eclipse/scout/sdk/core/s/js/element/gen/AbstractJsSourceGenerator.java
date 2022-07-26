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

public abstract class AbstractJsSourceGenerator<TYPE extends AbstractJsSourceGenerator<TYPE>> implements IJsSourceGenerator<IJsSourceBuilder<?>> {

  private boolean m_setupDone;

  @SuppressWarnings("unchecked")
  protected TYPE thisInstance() {
    return (TYPE) this;
  }

  @Override
  public void generate(IJsSourceBuilder<?> builder) {
    setup();
  }

  protected final void setup() {
    if (!isSetupDone()) {
      setupImpl();
      setSetupDone();
    }
  }

  protected void setupImpl() {
    // nop
  }

  private boolean isSetupDone() {
    return m_setupDone;
  }

  private void setSetupDone() {
    m_setupDone = true;
  }
}
