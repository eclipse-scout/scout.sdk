/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util.jdt;

import java.util.EventListener;

import org.eclipse.scout.sdk.util.typecache.IJavaResourceChangedEmitter;

/**
 * <h3>{@link IJavaResourceChangedListener}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 24.11.2010
 * @see IJavaResourceChangedEmitter
 */
public interface IJavaResourceChangedListener extends EventListener {

  /**
   * Callback that notifies this listener about changes.
   *
   * @param event
   *          The event describing the change.
   */
  void handleEvent(JdtEvent event);
}
