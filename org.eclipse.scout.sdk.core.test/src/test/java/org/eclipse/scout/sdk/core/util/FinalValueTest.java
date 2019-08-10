/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * <h3>{@link FinalValueTest}</h3>
 *
 * @since 6.1.0
 */
public class FinalValueTest {
  private static final String TEST_VALUE = "test";

  @Test
  public void testUnset() {
    FinalValue<String> s = new FinalValue<>();
    assertFalse(s.isSet());
    assertNull(s.get());
  }

  @Test
  public void testSuccessfulSet() {
    FinalValue<String> s = new FinalValue<>();
    s.set(TEST_VALUE);
    assertTestValue(s);
  }

  @Test
  public void testDuplicateSet() {
    FinalValue<String> s = new FinalValue<>();
    s.set(TEST_VALUE);
    assertThrows(IllegalArgumentException.class, () -> s.set(TEST_VALUE));
  }

  @Test
  public void testToString() {
    FinalValue<String> a = new FinalValue<>();
    assertEquals("FinalValue[<not set>]", a.toString());

    FinalValue<String> b = new FinalValue<>();
    b.set(null);
    assertEquals("FinalValue[null]", b.toString());

    FinalValue<String> c = new FinalValue<>();
    c.set("");
    assertEquals("FinalValue[]", c.toString());

    FinalValue<String> d = new FinalValue<>();
    d.set("abc");
    assertEquals("FinalValue[abc]", d.toString());
  }

  @Test
  @SuppressWarnings("unlikely-arg-type")
  public void testEquals() {
    FinalValue<String> a = new FinalValue<>();
    FinalValue<String> b = new FinalValue<>();
    b.set(null);
    FinalValue<String> c = new FinalValue<>();
    c.set("abc");
    FinalValue<String> d = new FinalValue<>();
    d.set("abc");

    assertTrue(a.equals(a));
    assertTrue(a.equals(b));
    assertTrue(c.equals(d));

    assertFalse(a.equals(c));
    assertFalse(a.equals(null));
    assertFalse(a.equals("abc"));
  }

  @Test
  public void testHashCode() {
    FinalValue<String> a = new FinalValue<>();
    FinalValue<String> b = new FinalValue<>();
    b.set(null);
    FinalValue<String> c = new FinalValue<>();
    c.set("abc");
    FinalValue<String> d = new FinalValue<>();
    d.set("abc");

    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(c.hashCode(), d.hashCode());
    assertEquals(c.hashCode(), c.get().hashCode());

    assertNotEquals(a.hashCode(), d.hashCode());
  }

  @Test
  public void testOpt() {
    FinalValue<String> s = new FinalValue<>();
    assertFalse(s.opt().isPresent());
    assertFalse(s.isSet());
    s.set(null);
    assertFalse(s.opt().isPresent());
    assertTrue(s.isSet());

    s = new FinalValue<>();
    s.set("whatever");
    assertTrue(s.opt().isPresent());
    assertTrue(s.isSet());
  }

  @Test
  public void testLazySet() {
    FinalValue<String> s = new FinalValue<>();
    String value = s.setIfAbsentAndGet(TEST_VALUE);
    assertTestValue(s);
    assertEquals(TEST_VALUE, value);
  }

  @Test
  public void testLazySetWithException() {
    FinalValue<String> s = new FinalValue<>();

    assertThrows(RuntimeException.class,
        () -> s.computeIfAbsent(() -> {
          throw new RuntimeException("expected JUnit test exception");
        }));
  }

  @Test
  public void testLazySetWithCustomException() {
    FinalValue<String> s = new FinalValue<>();
    assertThrows(RuntimeException.class,
        () -> s.computeIfAbsent(() -> {
          throw new MyRuntimeException();
        }));
  }

  @Test
  public void testLazyDuplicateSet() {
    FinalValue<String> s = new FinalValue<>();
    s.setIfAbsentAndGet(TEST_VALUE);
    String value2 = s.setIfAbsentAndGet("other");
    assertTestValue(s);
    assertEquals(TEST_VALUE, value2);
  }

  @Test
  public void testNoDoubleInitializationTry() {
    FinalValue<String> s = new FinalValue<>();
    s.setIfAbsentAndGet(null);
    assertNull(s.setIfAbsentAndGet("should not matter"));
    assertTrue(s.isSet());
  }

  @Test
  public void testBlockingCalls() {
    FinalValue<String> s = new FinalValue<>();

    CountDownLatch setup = new CountDownLatch(1);
    CountDownLatch latch = new CountDownLatch(1);
    String schedThreadMsg = "scheduled thread";
    String testThreadMsg = "test thread";
    ExecutorService executor = Executors.newSingleThreadExecutor();

    assertTimeout(Duration.ofSeconds(2), () -> {
      executor.submit(() -> {
        try {
          // wait until test thread is invoking FinalValue producer's call method
          setup.await(5, TimeUnit.SECONDS);
          s.computeIfAbsent(() -> {
            // release test thread
            latch.countDown();
            return schedThreadMsg;
          });
        }
        catch (InterruptedException e) {
          // nop
        }
      });

      String value = s.computeIfAbsentAndGet(() -> {
        setup.countDown();
        try {
          latch.await(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
          throw new SdkException(e);
        }
        return testThreadMsg;
      });

      assertTrue(s.isSet());
      // exact assertion about value is not possible because execution order is not deterministic
      assertTrue(schedThreadMsg.equals(value) || testThreadMsg.equals(value));

    });
  }

  private static void assertTestValue(FinalValue<String> s) {
    assertTrue(s.isSet());
    assertEquals(TEST_VALUE, s.get());
  }

  static class MyRuntimeException extends RuntimeException {

    MyRuntimeException() {
      super("expected JUnit test exception");
    }

    private static final long serialVersionUID = 1L;
  }
}
