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
package org.eclipse.scout.sdk.core.log;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * <h3>{@link SdkConsole}</h3> Handles access to the console used by the scout sdk.<br>
 * Usually it is preferred to use the {@link SdkLog} class instead.
 *
 * @since 5.2.0
 */
public final class SdkConsole {

  /**
   * The currently used sdk console strategy.
   */
  private static volatile ISdkConsoleSpi spi = new P_DefaultConsoleSpi();

  private SdkConsole() {
  }

  /**
   * @return the currently used sdk console strategy.
   */
  public static synchronized ISdkConsoleSpi getConsoleSpi() {
    return spi;
  }

  /**
   * Sets a new {@link ISdkConsoleSpi}.
   *
   * @param newSpi
   *          The new console strategy. If it is {@code null} the default strategy is used.
   */
  public static synchronized void setConsoleSpi(ISdkConsoleSpi newSpi) {
    if (newSpi == null) {
      newSpi = new P_DefaultConsoleSpi();
    }
    spi = newSpi;
  }

  /**
   * Clears the console contents.
   */
  public static synchronized void clear() {
    spi.clear();
  }

  static synchronized void println(LogMessage message) {
    spi.println(message);
  }

  static synchronized boolean isEnabled(Level level) {
    return level != null && spi.isEnabled(level);
  }

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  private static final class P_DefaultConsoleSpi implements ISdkConsoleSpi {

    private static final PrintStream OUT = System.out; // do not inline these constants!
    private static final PrintStream ERR = System.err;

    @Override
    public void clear() {
      var line = new char[50];
      Arrays.fill(line, '_');
      OUT.println(line);
    }

    @Override
    public void println(LogMessage msg) {
      PrintStream out;
      if (Level.SEVERE.equals(msg.severity())) {
        out = ERR;
      }
      else {
        out = OUT;
      }

      out.println(msg.all());
    }
  }
}
