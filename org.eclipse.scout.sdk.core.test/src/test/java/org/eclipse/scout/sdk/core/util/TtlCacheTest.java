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
package org.eclipse.scout.sdk.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class TtlCacheTest {

  @Test
  public void testPutAndGet() {
    var cache = new TtlCache<String, Long>(1, TimeUnit.DAYS);
    var key1 = "a";
    var key2 = "b";
    var value1 = 1L;
    var value2 = 2L;

    cache.put(key1, value1);
    cache.put(key2, value2);
    assertEquals(value1, cache.get(key1));
    assertNull(cache.get("c"));

    // test put replaces
    var replacementValue = 3L;
    cache.put(key1, replacementValue);
    assertEquals(replacementValue, cache.get(key1));
  }

  @Test
  public void testComputeIfAbsent() {
    var cache = new TtlCache<String, Long>(1, TimeUnit.DAYS);
    var key = "a";
    var value = 1L;

    assertNull(cache.get(key));
    cache.computeIfAbsent(key, k -> value);
    assertEquals(value, cache.get(key));

    // test mappingFunction is only executed if no mapping exists
    var counter = new AtomicInteger();
    cache.computeIfAbsent(key, k -> {
      counter.incrementAndGet();
      return 2L;
    });
    assertEquals(value, cache.get(key));
    assertEquals(0, counter.get());
  }

  @Test
  public void testClear() {
    var cache = new TtlCache<String, Long>(-1, TimeUnit.DAYS);
    var key1 = "a";
    var key2 = "b";

    cache.put(key1, 1L);
    cache.put(key2, 2L);
    cache.clear();
    assertNull(cache.get(key1));
    assertNull(cache.get(key2));
  }

  @Test
  public void testItemsElapse() throws InterruptedException {
    var cache = new TtlCache<String, Long>(10, TimeUnit.MILLISECONDS);
    var key = "a";
    cache.put(key, 1L);
    Thread.sleep(20);
    assertNull(cache.get(key));
  }

  @Test
  public void testAsyncCleanup() throws InterruptedException {
    var scheduledExecutorService = Executors.newScheduledThreadPool(5);
    try {
      var cacheCleanupExecuted = new CountDownLatch(1);
      var cache = new TtlCache<String, Long>(10, TimeUnit.MILLISECONDS, scheduledExecutorService) {
        @Override
        protected Void afterScheduledCacheCleanup(Map<String, TtlCacheEntry<Long>> c) {
          cacheCleanupExecuted.countDown();
          return super.afterScheduledCacheCleanup(c);
        }
      };
      var key = "a";
      cache.put(key, 1L);
      cacheCleanupExecuted.await(1, TimeUnit.MINUTES);
      assertNull(cache.get(key));
    }
    finally {
      scheduledExecutorService.shutdown();
      scheduledExecutorService.awaitTermination(1, TimeUnit.MINUTES);
    }
  }

  @Test
  public void testObsoleteAsyncCleanupFuturesAreCancelled() throws InterruptedException {
    var scheduledExecutorService = Executors.newScheduledThreadPool(5);
    var cleanupCounter = new AtomicInteger();
    try {
      var cache = new TtlCache<String, Long>(100, TimeUnit.MILLISECONDS, scheduledExecutorService) {
        @Override
        protected Void afterScheduledCacheCleanup(Map<String, TtlCacheEntry<Long>> c) {
          cleanupCounter.incrementAndGet();
          return super.afterScheduledCacheCleanup(c);
        }
      };

      var key = "a";
      cache.put(key, 1L);
      cache.put(key, 2L);
    }
    finally {
      scheduledExecutorService.shutdown();
      scheduledExecutorService.awaitTermination(1, TimeUnit.MINUTES);
    }

    assertEquals(1, cleanupCounter.get());
  }
}
