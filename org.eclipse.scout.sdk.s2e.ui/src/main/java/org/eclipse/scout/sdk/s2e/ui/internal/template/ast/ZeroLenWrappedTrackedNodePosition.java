/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;

/**
 * <h3>{@link ZeroLenWrappedTrackedNodePosition}</h3>
 *
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
