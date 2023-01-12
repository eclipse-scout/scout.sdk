/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.environment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.junit.jupiter.api.Test;

public class EnvironmentTest {

  @Test
  public void testWaitForUncompletedFutures() {
    IFuture<Void> future1;
    IFuture<Void> future2;

    var threadPool = Executors.newFixedThreadPool(10);

    try (var env = new TestingEnvironment(null, false, false, null) {
      @Override
      protected IFuture<Void> doWriteResource(CharSequence content, Path filePath, IProgress progress, boolean sync) {
        var future = new TimedFuture(1000);
        threadPool.submit(future);
        return future;
      }
    }) {
      future1 = env.writeResourceAsync("Something", Paths.get("test1.txt"), null);
      future2 = env.writeResourceAsync("Else", Paths.get("test2.txt"), null);
    }

    assertTrue(future1.isDone());
    assertTrue(future2.isDone());
  }

  private static class TimedFuture extends SdkFuture<Void> implements Runnable {
    private final long m_duration;

    TimedFuture(long duration) {
      m_duration = duration;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(m_duration);
      }
      catch (InterruptedException e) {
        SdkLog.warning("Thread sleep was interrupted.", e);
      }
      doCompletion(false, null, () -> null);
    }
  }
}
