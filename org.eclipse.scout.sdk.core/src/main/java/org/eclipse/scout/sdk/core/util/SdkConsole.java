/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;

public final class SdkConsole {

  public static void clear() {
    spi.clear();
  }

  /**
   * write a line to the console
   */
  public static void println() {
    spi.println("");
  }

  /**
   * write a line to the console
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
      return "[" + e + "]" + t.toString();
    }
  }

  // service provider interface (spi)

  private SdkConsole() {
  }

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

  //default writes to sysout
  public static volatile SdkConsole.SdkConsoleSpi spi = DEFAULT_SPI;

}
