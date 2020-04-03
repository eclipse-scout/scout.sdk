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
package org.eclipse.scout.sdk.core.log;

import static java.lang.System.lineSeparator;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.logging.Level;

import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link SdkLogTest}</h3>
 *
 * @since 5.2.0
 */
public class SdkLogTest {

  private static final String EXPECTED_TIME = "1970-01-01 02:00:00.000 ";

  @BeforeAll
  public static void setup() {
    SdkLog.clock = Clock.fixed(ofEpochSecond(3600), ZoneId.of("Europe/Berlin"));
  }

  @Test
  public void testHandlePlaceholders() {
    assertPlaceholders("a {} b", "a {} b");
    assertPlaceholders("anull12", "a{}{}{}", null, "1", 2);
    assertPlaceholders("1a", "{}a", 1);
    assertPlaceholders("a null b", "a {} b", new Object[]{null});
    assertPlaceholders("a 1 b", "a {} b", 1);
    assertPlaceholders("a 1 b", "a {} b", 1, "notused");
    assertPlaceholders("a testle b", "a {} b", "testle");
    assertPlaceholders("a [] b", "a {} b", new Object[]{new Object[]{}});
    assertPlaceholders("a [1, 2, 3] b", "a {} b", new Object[]{new Object[]{1, 2, 3}});
    assertPlaceholders("a [1, 2, 3] b", "a {} b", new Object[]{new Integer[]{1, 2, 3}});
    assertPlaceholders("a testle b {} c", "a {} b {} c", "testle");
    assertPlaceholders("a [true, false, false] b", "a {} b", new Object[]{new boolean[]{true, false, false}});
    assertPlaceholders("a [40, 41, 2] b", "a {} b", new Object[]{new byte[]{40, 41, 2}});
    assertPlaceholders("a [a, l, d] b", "a {} b", new Object[]{new char[]{'a', 'l', 'd'}});
    assertPlaceholders("a [40, 41, 2] b", "a {} b", new Object[]{new short[]{40, 41, 2}});
    assertPlaceholders("a [40, 41, 2] b", "a {} b", new Object[]{new int[]{40, 41, 2}});
    assertPlaceholders("a [40, 41, 2000000000000000000] b", "a {} b", new Object[]{new long[]{40L, 41L, 2000000000000000000L}});
    assertPlaceholders("a [1.1, 1.002, 300.0] b", "a {} b", new Object[]{new float[]{1.1f, 1.002f, 300.0f}});
    assertPlaceholders("a [11.3, 12.004, 100.0] b", "a {} b", new Object[]{new double[]{11.3, 12.004, 100}});
    assertPlaceholders("a [[[11, 12]], [[13], [14, 15, 16]]] b", "a {} b", new Object[]{new int[][][]{{{11, 12}}, {{13}, {14, 15, 16}}}});
    assertPlaceholders("a false b", "a {} b", false, new Exception(), new Exception());

    Object[] longArgs = new Object[101];
    for (int i = 0; i < longArgs.length; i++) {
      longArgs[i] = i + 1;
    }
    StringBuilder expected = new StringBuilder("1");
    for (int i = 2; i <= 100; i++) {
      expected.append(", ");
      expected.append(i);
    }

    assertPlaceholders("a [" + expected + ",...] b", "a {} b", new Object[]{longArgs});
  }

  private static void assertPlaceholders(String expected, String input, Object... args) {
    FormattingTuple tuple = MessageFormatter.arrayFormat(input, args);
    String replaced = tuple.message();
    assertEquals(expected, replaced);

    int expectedNumThrowables = 0;
    if (args != null) {
      int numReplaced = Math.min(Strings.countMatches(input, MessageFormatter.ARG_REPLACE_PATTERN), args.length);
      for (int i = numReplaced; i < args.length; i++) {
        if (args[i] instanceof Throwable) {
          expectedNumThrowables++;
        }
      }
    }
    assertEquals(expectedNumThrowables, tuple.throwables().size());
  }

  @Test
  public void testFormattingTuple() {
    Exception e = new Exception("err");
    FormattingTuple a = new FormattingTuple("msg", singletonList(e));
    FormattingTuple b = new FormattingTuple("msg", singletonList(e));
    FormattingTuple c = new FormattingTuple("msg", emptyList());

    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(a.toString(), b.toString());
    assertEquals(a, b);
    assertNotEquals(a.hashCode(), c.hashCode());
    assertNotEquals(a.toString(), c.toString());
    assertNotEquals(a, c);
  }

  @Test
  public void testExtractThrowables() {
    assertEquals(0, MessageFormatter.extractThrowables(0).size());
    assertEquals(0, MessageFormatter.extractThrowables(0, (Object[]) null).size());
    assertEquals(0, MessageFormatter.extractThrowables(0, "a", "b", null).size());
    assertEquals(0, MessageFormatter.extractThrowables(4, "a", "b", null).size());
    assertEquals(1, MessageFormatter.extractThrowables(0, "a", "b", new Exception()).size());
    assertEquals(0, MessageFormatter.extractThrowables(3, "a", "b", new Exception()).size());
    assertEquals(1, MessageFormatter.extractThrowables(2, "a", "b", new Exception()).size());
    assertEquals(2, MessageFormatter.extractThrowables(2, "a", "b", new Object[]{new Exception(), new Exception()}).size());
    assertEquals(3, MessageFormatter.extractThrowables(2, "a", "b", new Object[]{new Exception(), new Object[]{new Exception(), new Exception()}}).size());
    assertEquals(3, MessageFormatter.extractThrowables(2, "a", "b", new Object[]{singletonList(new Exception()), Arrays.asList(new Exception(), new Exception())}).size());
  }

  @Test
  public void testParseLevel() {
    assertEquals(SdkLog.DEFAULT_LOG_LEVEL, SdkLog.parseLevel(null));
    assertEquals(SdkLog.DEFAULT_LOG_LEVEL, SdkLog.parseLevel(""));
    assertEquals(SdkLog.DEFAULT_LOG_LEVEL, SdkLog.parseLevel(" "));
    assertEquals(SdkLog.DEFAULT_LOG_LEVEL, SdkLog.parseLevel("\t"));
    assertEquals(Level.SEVERE, SdkLog.parseLevel(Level.SEVERE.getName()));
    assertEquals(SdkLog.DEFAULT_LOG_LEVEL, SdkLog.parseLevel("aa"));
  }

  @Test
  public void testLogOfObjectWithToStringThrowingException() {
    runWithPrivateLogger(Level.WARNING, logContent -> {
      SdkLog.error("Msg: {}", new ClassWithToStringThrowingNpeFixture());
      assertEqualsWithTime("[SEVERE]  Msg: [FAILED toString() of class " + SdkLogTest.class.getName() + JavaTypes.C_DOLLAR + ClassWithToStringThrowingNpeFixture.class.getSimpleName() + ']', logContent.toString());
      SdkConsole.clear();
    });
  }

  @Test
  public void testLog() {
    runWithPrivateLogger(Level.WARNING, logContent -> {
      SdkLog.warning("hello");
      assertEqualsWithTime("[WARNING] hello", logContent.toString());
      assertTrue(SdkLog.isWarningEnabled());
      SdkConsole.clear();

      Exception exception = new Exception();
      SdkLog.warning("hello {} there", "test", exception);
      assertEqualsWithTime("[WARNING] hello test there" + lineSeparator() + Strings.fromThrowable(exception), logContent.toString());
      SdkConsole.clear();

      SdkLog.error(exception);
      assertEqualsWithTime("[SEVERE]  " + lineSeparator() + Strings.fromThrowable(exception), logContent.toString());
      assertTrue(SdkLog.isErrorEnabled());
      SdkConsole.clear();

      SdkLog.warning(null, (Object[]) null);
      assertEqualsWithTime("[WARNING] ", logContent.toString());
      SdkConsole.clear();

      SdkLog.info("hello");
      assertEquals("", logContent.toString());
      assertFalse(SdkLog.isInfoEnabled());
      SdkConsole.clear();

      SdkLog.log(Level.OFF, "hello");
      assertEquals("", logContent.toString());
      assertFalse(SdkLog.isDebugEnabled());
      SdkConsole.clear();

      assertFalse(SdkLog.isLevelEnabled(null));
      SdkLog.log(null, "hello");
      assertEqualsWithTime("[WARNING] hello", logContent.toString());
      SdkConsole.clear();
    });
  }

  private static void assertEqualsWithTime(String expected, String actual) {
    assertEquals(EXPECTED_TIME + expected, actual);
  }

  private static final class ClassWithToStringThrowingNpeFixture {
    @Override
    public String toString() {
      throw new NullPointerException("NPE of test " + SdkLogTest.class);
    }
  }

  @FunctionalInterface
  private interface ILogTestRunner {
    void run(StringBuilder logContent);
  }

  private static void runWithPrivateLogger(Level initialLevel, ILogTestRunner runnable) {
    // lock on console to ensure no other thread writes to the console while we are testing (in case tests are running in parallel)
    //noinspection SynchronizeOnThis
    synchronized (SdkConsole.class) {
      ISdkConsoleSpi backup = SdkConsole.getConsoleSpi();
      Level oldLevel = SdkLog.getLogLevel();
      try {
        StringBuilder logContent = new StringBuilder();
        SdkLog.setLogLevel(initialLevel);
        SdkConsole.setConsoleSpi(new ISdkConsoleSpi() {

          @Override
          public void println(LogMessage msg) {
            logContent.append(msg.all());
          }

          @Override
          public void clear() {
            logContent.delete(0, logContent.length());
          }
        });

        runnable.run(logContent);

      }
      finally {
        SdkLog.setLogLevel(oldLevel);
        SdkConsole.setConsoleSpi(backup);
      }
    }
  }

  @AfterAll
  public static void cleanup() {
    SdkLog.clock = Clock.systemDefaultZone();
  }
}
