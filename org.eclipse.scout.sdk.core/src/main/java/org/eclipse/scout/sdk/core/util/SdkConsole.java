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
   * write an empty line to the console.
   */
  static synchronized void println() {
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
  static synchronized void println(String msg, Throwable... exceptions) {
    spi.println(msg, exceptions);
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

    void println(String s, Throwable... exceptions);
  }

  private static final SdkConsole.SdkConsoleSpi DEFAULT_SPI = new SdkConsole.SdkConsoleSpi() {
    @Override
    public void clear() {
      System.out.println(StringUtils.leftPad("", 50, '_'));
    }

    @Override
    public void println(String s, Throwable... exceptions) {
      if (s != null) {
        System.out.println(s);
      }

      if (exceptions == null || exceptions.length < 1) {
        return;
      }

      for (Throwable t : exceptions) {
        if (t != null) {
          t.printStackTrace();
        }
      }
    }
  };

  /**
   * The currently used sdk console strategy.
   */
  public static volatile SdkConsole.SdkConsoleSpi spi = DEFAULT_SPI;

}
