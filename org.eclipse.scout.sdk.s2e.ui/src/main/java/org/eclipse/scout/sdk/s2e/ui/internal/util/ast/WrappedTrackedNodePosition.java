/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.util.ast;

import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

/**
 * <h3>{@link WrappedTrackedNodePosition}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class WrappedTrackedNodePosition implements ITrackedNodePosition {

  private final int m_startDif;
  private final int m_lengthDif;
  private final ITrackedNodePosition m_inner;

  public WrappedTrackedNodePosition(ITrackedNodePosition inner, int startDif, int lengthDif) {
    m_inner = Validate.notNull(inner);
    m_startDif = startDif;
    m_lengthDif = lengthDif;
  }

  @Override
  public int getStartPosition() {
    return m_inner.getStartPosition() + m_startDif;
  }

  @Override
  public int getLength() {
    return m_inner.getLength() + m_lengthDif;
  }

}
