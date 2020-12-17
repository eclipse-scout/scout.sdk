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

import static java.util.Collections.unmodifiableList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.scout.sdk.core.util.SdkException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DelayedBufferTest {

  private static ScheduledExecutorService m_executor;
  private static final long DELAY = 100;

  @BeforeAll
  public static void setup() {
    m_executor = Executors.newScheduledThreadPool(50);
  }

  @Test
  public void testElementsAreBuffered() throws InterruptedException, ExecutionException, TimeoutException {
    var protocolConsumer = new ProtocolConsumer();
    var worker = new DelayedBuffer<>(DELAY, TimeUnit.MILLISECONDS, m_executor, true, protocolConsumer);
    var numElementsToSubmit = 100;
    for (var i = 0; i < numElementsToSubmit; i++) {
      worker.submit(Integer.toString(i));
    }
    worker.awaitAllProcessed(DELAY * 5, TimeUnit.MILLISECONDS);
    assertEquals(1, protocolConsumer.numInvocations());
    assertEquals(numElementsToSubmit, protocolConsumer.elements().size());
  }

  @Test
  public void testMultipleSubmitsAfterDelay() throws InterruptedException, ExecutionException, TimeoutException {
    var protocolConsumer = new ProtocolConsumer();
    var worker = new DelayedBuffer<>(DELAY, TimeUnit.MILLISECONDS, m_executor, true, protocolConsumer);
    worker.submit("1");
    worker.awaitAllProcessed(DELAY * 2, TimeUnit.MILLISECONDS);
    worker.submit("2");
    worker.awaitAllProcessed(DELAY * 2, TimeUnit.MILLISECONDS);
    assertEquals(2, protocolConsumer.numInvocations());
    assertEquals(2, protocolConsumer.elements().size());
  }

  @Test
  public void testSubmittedWhileWorkerIsBeingExecuted() throws InterruptedException, TimeoutException, ExecutionException {
    var protocolConsumer = new ProtocolConsumer();
    var workerStarted = new CountDownLatch(1);
    var secondElementSubmitted = new CountDownLatch(1);
    var worker = new DelayedBuffer<>(DELAY, TimeUnit.MILLISECONDS, m_executor,  false, protocolConsumer) {
      @Override
      List<String> getMyWork() {
        var myWork = super.getMyWork();
        workerStarted.countDown();
        try {
          secondElementSubmitted.await(DELAY * 2, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
          throw new SdkException(e);
        }
        return myWork;
      }
    };
    worker.submit("1");
    workerStarted.await(DELAY * 2, TimeUnit.MILLISECONDS);
    worker.submit("2");
    secondElementSubmitted.countDown();
    worker.awaitAllProcessed(DELAY * 2, TimeUnit.MILLISECONDS);
    assertEquals(2, protocolConsumer.numInvocations());
    assertEquals(2, protocolConsumer.elements().size());
  }

  @Test
  public void testSubmittedBeforeWorkerHasTakenItsItems() throws InterruptedException, TimeoutException, ExecutionException {
    var protocolConsumer = new ProtocolConsumer();
    var workerStarted = new CountDownLatch(1);
    var secondElementSubmitted = new CountDownLatch(1);
    var worker = new DelayedBuffer<>(DELAY, TimeUnit.MILLISECONDS, m_executor,  false, protocolConsumer) {
      @Override
      List<String> getMyWork() {
        workerStarted.countDown();
        try {
          secondElementSubmitted.await(DELAY * 2, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
          throw new SdkException(e);
        }
        return super.getMyWork();
      }
    };
    worker.submit("1");
    workerStarted.await(DELAY * 2, TimeUnit.MILLISECONDS);
    worker.submit("2");
    secondElementSubmitted.countDown();
    worker.awaitAllProcessed(DELAY * 2, TimeUnit.MILLISECONDS);
    assertEquals(1, protocolConsumer.numInvocations());
    assertEquals(2, protocolConsumer.elements().size());
  }

  private static class ProtocolConsumer implements Consumer<List<String>> {

    private final List<String> m_elements = new ArrayList<>();
    private final AtomicInteger m_numInvocations = new AtomicInteger();

    @Override
    public void accept(List<String> elements) {
      m_numInvocations.incrementAndGet();
      m_elements.addAll(elements);
    }

    private int numInvocations() {
      return m_numInvocations.get();
    }

    private List<String> elements() {
      return unmodifiableList(m_elements);
    }
  }

  @AfterAll
  public static void cleanup() throws InterruptedException {
    try {
      m_executor.awaitTermination(4, TimeUnit.SECONDS);
    }
    finally {
      m_executor.shutdown();
    }
    m_executor = null;
  }
}
