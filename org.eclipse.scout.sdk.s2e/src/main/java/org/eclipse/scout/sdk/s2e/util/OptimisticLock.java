/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
    var count = m_lockCount.incrementAndGet();
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
