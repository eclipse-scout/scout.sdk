/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.util;

import java.io.PrintStream;
import java.util.logging.Level;

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
  public static synchronized void clear() {
    spi.clear();
  }

  /**
   * Write the given message with optional {@link Throwable}s to the console.
   *
   * @param level
   *          The severity of the given message
   * @param msg
   *          The message to write. May be <code>null</code>.
   * @param exceptions
   *          Optional {@link Throwable}s to write to the console.
   */
  static synchronized void println(Level level, String msg, Throwable... exceptions) {
    spi.println(level, msg, exceptions);
  }

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

    void println(Level level, String s, Throwable... exceptions);
  }

  private static final SdkConsole.SdkConsoleSpi DEFAULT_SPI = new SdkConsole.SdkConsoleSpi() {
    @Override
    public void clear() {
      System.out.println(StringUtils.leftPad("", 50, '_'));
    }

    @Override
    public void println(Level level, String s, Throwable... exceptions) {
      PrintStream out = null;
      if (Level.SEVERE.equals(level)) {
        out = System.err;
      }
      else {
        out = System.out;
      }

      if (s != null) {
        out.println(s);
      }

      if (exceptions == null || exceptions.length < 1) {
        return;
      }

      for (Throwable t : exceptions) {
        if (t != null) {
          t.printStackTrace(out);
        }
      }
    }
  };

  /**
   * The currently used sdk console strategy.
   */
  public static volatile SdkConsole.SdkConsoleSpi spi = DEFAULT_SPI;

}
