/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.testing.mock;

import org.apache.commons.lang3.Validate;
import org.junit.runners.model.Statement;

/**
 * <h3>{@link PlatformMocksInitStatement}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class PlatformMocksInitStatement extends Statement {

  private final Statement m_next;
  private final Object m_target;
  private final PlatformMocks m_platformMocks;

  public PlatformMocksInitStatement(final Statement next, final Object target) {
    m_next = Validate.notNull(next, "next statement must not be null");
    m_target = Validate.notNull(target);
    m_platformMocks = new PlatformMocks();
  }

  @Override
  public void evaluate() throws Throwable {
    m_platformMocks.initFieldMocks(m_target);
    m_platformMocks.initDerivedResourceManagerMock();
    m_platformMocks.initAutomaticClassIdGeneration();
    m_next.evaluate();
  }
}
