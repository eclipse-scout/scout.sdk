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

import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

/**
 * <h3>{@link ZeroLenWrappedTrackedNodePosition}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ZeroLenWrappedTrackedNodePosition extends WrappedTrackedNodePosition {

  public ZeroLenWrappedTrackedNodePosition(ITrackedNodePosition inner, int startDif) {
    super(inner, startDif, 0);
  }

  @Override
  public int getLength() {
    return 0;
  }
}
