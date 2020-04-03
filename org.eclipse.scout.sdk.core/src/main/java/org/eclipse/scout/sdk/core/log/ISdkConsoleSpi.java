package org.eclipse.scout.sdk.core.log;

import java.util.logging.Level;

/**
 * <h3>{@link ISdkConsoleSpi}</h3> Console provider strategy.
 * <p>
 * Used by {@link SdkLog} as target for messages.
 *
 * @since 5.2.0
 */
public interface ISdkConsoleSpi {
  /**
   * Clears all content from the console.
   */
  void clear();

  /**
   * Specifies if the given {@link Level} is relevant for the {@link ISdkConsoleSpi} and should therefore be processed
   * and forwarded.
   * 
   * @param level
   *          The level to check. Is never {@code null}.
   * @return {@code true} if the level is relevant and should be forwarded to {@link #println(LogMessage)}.
   *         {@code false} otherwise.
   */
  default boolean isRelevant(Level level) {
    return level.intValue() >= SdkLog.getLogLevel().intValue()
        && level.intValue() != Level.OFF.intValue();
  }

  /**
   * Prints the {@link LogMessage} specified.
   * 
   * @param message
   *          The message to log. Is never {@code null}.
   */
  void println(LogMessage message);
}
