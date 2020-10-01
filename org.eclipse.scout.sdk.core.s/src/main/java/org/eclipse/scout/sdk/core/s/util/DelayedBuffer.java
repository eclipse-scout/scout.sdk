/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.util.Ensure;

public class DelayedBuffer<T> {

  private final long m_delay;
  private final TimeUnit m_unit;
  private final ScheduledExecutorService m_executor;
  private final boolean m_allowParallelProcessing;
  private final Consumer<List<T>> m_processor;
  private final List<T> m_buffer;
  private final Object m_workLock;
  private ScheduledFuture<?> m_future;

  public DelayedBuffer(long delay, TimeUnit unit, ScheduledExecutorService executor, boolean allowParallelProcessing, Consumer<List<T>> processor) {
    m_delay = delay;
    m_unit = Ensure.notNull(unit);
    m_executor = Ensure.notNull(executor);
    m_allowParallelProcessing = allowParallelProcessing;
    m_processor = Ensure.notNull(processor);
    m_workLock = new Object();
    m_buffer = new ArrayList<>();
  }

  public void submit(T element) {
    synchronized (m_buffer) {
      registerElement(element);
      cancelExistingFuture();
      scheduleNewProcessing();
    }
  }

  public void awaitAllProcessed(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    ScheduledFuture<?> current = m_future;
    if (current == null) {
      return;
    }
    current.get(timeout, unit);
  }

  void processElements() {
    List<T> myWork = getMyWork();
    if (myWork.isEmpty()) {
      return;
    }

    if (isAllowParallelProcessing()) {
      processor().accept(myWork);
    }
    else {
      synchronized (m_workLock) {
        processor().accept(myWork);
      }
    }
  }

  List<T> getMyWork() {
    List<T> myWork;
    synchronized (m_buffer) {
      // hand elements over to this worker
      myWork = new ArrayList<>(m_buffer);
      m_buffer.clear();
    }
    return myWork;
  }

  void registerElement(T element) {
    m_buffer.add(Ensure.notNull(element));
  }

  void cancelExistingFuture() {
    ScheduledFuture<?> current = m_future;
    if (current == null) {
      return;
    }

    // cancel existing scheduled (and possibly not yet started) future
    // if it already started: let it finish its work (the delay elapsed)
    current.cancel(false); // no interrupt. if already started: let complete its work
  }

  void scheduleNewProcessing() {
    m_future = executor().schedule(this::processElements, delay(), delayUnit());
  }

  public boolean isAllowParallelProcessing() {
    return m_allowParallelProcessing;
  }

  public Consumer<List<T>> processor() {
    return m_processor;
  }

  public long delay() {
    return m_delay;
  }

  public TimeUnit delayUnit() {
    return m_unit;
  }

  public ScheduledExecutorService executor() {
    return m_executor;
  }
}
