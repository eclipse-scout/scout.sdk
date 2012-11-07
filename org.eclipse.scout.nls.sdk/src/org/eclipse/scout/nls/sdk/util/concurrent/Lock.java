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
package org.eclipse.scout.nls.sdk.util.concurrent;

/**
 * <h4>Lock</h4> Is used to TODO
 */
public class Lock {
  private boolean m_locked = false;

  /**
   * to acquire a lock.
   * 
   * @return whether the lock can be acquired or not.
   */
  public synchronized boolean acquire() {
    if (m_locked) {
      return false;
    }
    else {
      m_locked = true;
      return true;
    }
  }

  /**
   * to release a lock
   */
  public void release() {
    m_locked = false;
  }

  /**
   * check the lock state.
   * 
   * @return *
   */
  public boolean isLocked() {
    return m_locked;
  }
}
