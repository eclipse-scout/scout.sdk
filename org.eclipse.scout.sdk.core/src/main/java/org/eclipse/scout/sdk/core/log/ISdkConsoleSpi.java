package org.eclipse.scout.sdk.core.log;

import java.util.logging.Level;

/**
 * <h3>{@link ISdkConsoleSpi}</h3> Console provider strategy.
 *
 * @since 5.2.0
 */
public interface ISdkConsoleSpi {
    void clear();

    void println(Level level, String s, Throwable... exceptions);
}
