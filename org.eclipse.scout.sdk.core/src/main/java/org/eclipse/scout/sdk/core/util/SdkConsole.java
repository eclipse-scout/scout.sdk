/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;

/**
 * <h3>{@link SdkConsole}</h3> Handles access to the console used by the scout sdk.<br>
 * Usually it is preferred to use the {@link SdkLog} class instead.
 *
 * @author Ivan Motsch
 * @since 5.2.0
 */
public final class SdkConsole {

  /**
   * Clears the console contents.
   */
  public static void clear() {
    spi.clear();
  }

  /**
   * write an empty line to the console.
   */
  public static void println() {
    spi.println("");
  }

  /**
   * Write the given message with optional {@link Throwable}s to the console.
   *
   * @param msg
   *          The message to write. May be <code>null</code>.
   * @param exceptions
   *          Optional {@link Throwable}s to write to the console.
   */
  public static void println(String msg, Throwable... exceptions) {
    if (msg != null) {
      spi.println(msg);
    }

    if (exceptions == null) {
      return;
    }

    for (Throwable t : exceptions) {
      if (t != null) {
        spi.println(getStackTrace(t));
      }
    }
  }

  public static String getStackTrace(Throwable t) {
    try (StringWriter w = new StringWriter(); PrintWriter p = new PrintWriter(w)) {
      t.printStackTrace(p);
      return w.toString();
    }
    catch (IOException e) {
      return '[' + e.toString() + ']' + t.toString();
    }
  }

  // service provider interface (spi)

  private SdkConsole() {
  }

  /**
   * <h3>{@link SdkConsoleSpi}</h3> Console provider strategy.
   *
   * @author Ivan Motsch
   * @since 5.2.0
   */
  public interface SdkConsoleSpi {
    void clear();

    void println(String s);
  }

  private static final SdkConsole.SdkConsoleSpi DEFAULT_SPI = new SdkConsole.SdkConsoleSpi() {
    @Override
    public void clear() {
      System.out.println(StringUtils.leftPad("", 50, '_'));
    }

    @Override
    public void println(String s) {
      System.out.println(s);
    }
  };

  /**
   * The currently used sdk console strategy.
   */
  public static volatile SdkConsole.SdkConsoleSpi spi = DEFAULT_SPI;

}
