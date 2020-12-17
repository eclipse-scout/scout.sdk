/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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

/**
 * Implements an unbound buffer that buffers elements until there are no new elements being submitted for a certain time
 * ({@link #silentTime()}). When the silent time elapses without a new element being submitted, all buffered elements
 * are asynchronously executed by a processor. Elements that have been handed over to the processor are removed from the
 * buffer.
 * 
 * @param <T>
 *          The type of buffered elements
 */
public class DelayedBuffer<T> {

  private final long m_silentTime;
  private final TimeUnit m_silentTimeUnit;
  private final ScheduledExecutorService m_executorService;
  private final boolean m_allowParallelProcessing;
  private final Consumer<List<T>> m_processor;
  private final List<T> m_buffer;
  private final Object m_workLock;
  private ScheduledFuture<?> m_future;

  /**
   * Creates a new {@link DelayedBuffer}.
   * 
   * @param silentTime
   *          If there are no events for this time, the buffered events are asynchronously executed by the given
   *          processor.
   * @param silentTimeUnit
   *          The {@link TimeUnit} of the silentTime. Must not be {@code null}.
   * @param executorService
   *          The {@link ScheduledExecutorService} to use to asynchronously execute the given processor. Must not be
   *          {@code null}.
   * @param allowParallelProcessing
   *          Specifies if only one processor may run at the same time or if they may run in parallel.
   * @param processor
   *          The processor to handle the buffered events once the silentTime elapsed without new elements being
   *          submitted. Must not be {@code null}.
   */
  public DelayedBuffer(long silentTime, TimeUnit silentTimeUnit, ScheduledExecutorService executorService, boolean allowParallelProcessing, Consumer<List<T>> processor) {
    m_silentTime = silentTime;
    m_silentTimeUnit = Ensure.notNull(silentTimeUnit);
    m_executorService = Ensure.notNull(executorService);
    m_allowParallelProcessing = allowParallelProcessing;
    m_processor = Ensure.notNull(processor);
    m_workLock = new Object();
    m_buffer = new ArrayList<>();
  }

  /**
   * Submits a new element to this buffer.
   * 
   * @param element
   *          The new element. May be {@code null}. In that case the {@link #processor()} must be able to handle
   *          {@code null} elements.
   */
  public void submit(T element) {
    synchronized (m_buffer) {
      cancelExistingFuture();
      registerElement(element);
      scheduleNewProcessing();
    }
  }

  /**
   * Waits until the ones that are in the buffer at the moment of the invocation of this method are all processed.<br>
   * It does not wait for elements to be processed that are submitted after the call to this method.
   * 
   * @param timeout
   *          The wait timeout.
   * @param unit
   *          The {@link TimeUnit} of the wait timeout.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting
   * @throws ExecutionException
   *           if the processor threw an exception
   * @throws TimeoutException
   *           if the wait timed out
   */
  public void awaitAllProcessed(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    var current = m_future;
    if (current == null) {
      return;
    }
    current.get(timeout, unit);
  }

  /**
   * Asynchronously executed in a worker of {@link #executorService()}.
   */
  void processElements() {
    var myWork = getMyWork();
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
    m_buffer.add(element);
  }

  void cancelExistingFuture() {
    var current = m_future;
    if (current == null) {
      return;
    }

    // cancel existing scheduled (and possibly not yet started) future
    // if it already started: let it finish its work (the delay elapsed)
    current.cancel(false); // no interrupt. if already started: let complete its work
  }

  void scheduleNewProcessing() {
    m_future = executorService().schedule(this::processElements, silentTime(), silentTimeUnit());
  }

  /**
   * @return {@code true} if the {@link #processor()} may be executed in parallel.
   */
  public boolean isAllowParallelProcessing() {
    return m_allowParallelProcessing;
  }

  /**
   * @return The {@link Consumer} that processes the buffered events once the {@link #silentTime()} elapsed. Never
   *         returns {@code null}.
   */
  public Consumer<List<T>> processor() {
    return m_processor;
  }

  /**
   * @return The time that needs to elapse without a new element being submitted so that the {@link #processor()} is
   *         executed for the buffered elements.
   */
  public long silentTime() {
    return m_silentTime;
  }

  /**
   * @return The {@link TimeUnit} of {@link #silentTime()}. Never returns {@code null}.
   */
  public TimeUnit silentTimeUnit() {
    return m_silentTimeUnit;
  }

  /**
   * @return The {@link ScheduledExecutorService} that is used to asynchronously execute the {@link #processor()}. Never
   *         returns {@code null}.
   */
  public ScheduledExecutorService executorService() {
    return m_executorService;
  }
}
