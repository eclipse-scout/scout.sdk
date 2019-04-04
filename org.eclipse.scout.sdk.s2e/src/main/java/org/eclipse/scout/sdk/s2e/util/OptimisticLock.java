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
package org.eclipse.scout.sdk.s2e.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.sdk.core.log.SdkLog;

/**
 * Optimistic locking with one accepted writer in critical section.<br>
 * <br>
 * Usage for writers with access check is as follows:<br>
 * <code>
 * try{ if(lock.acquire()){ ... } }<br>
 * finally{ lock.release(); }
 * </code><br>
 * <br>
 * Usage for writers with no access check is as follows:<br>
 * {@code try{ lock.acquire() ... } finally{ lock.release(); }} <br>
 * <br>
 * Usage for tester is as follows:<br>
 * {@code if(lock.isAcquired()){ ... } or if(lock.isReleased()){ ... } }
 */
public final class OptimisticLock {

  private final AtomicInteger m_lockCount = new AtomicInteger(0);

  /**
   * @return true if lock was acquired as first monitor
   */
  public synchronized boolean acquire() {
    int count = m_lockCount.incrementAndGet();
    if (count == 1) {
      // this is the first
      return true;
    }
    if (count > 10) {
      SdkLog.error("potential programming problem; lock was 10 times acquired and not released", new Exception("origin"));
    }
    return false;
  }

  public void release() {
    if (m_lockCount.decrementAndGet() < 0) {
      SdkLog.error("potential programming problem. lock is negative", new Exception("origin"));
    }
  }

  public boolean isAcquired() {
    return m_lockCount.get() > 0;
  }

  public boolean isReleased() {
    return !isAcquired();
  }
}
