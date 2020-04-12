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
package org.eclipse.scout.sdk.core.model.ecj;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;

/**
 * <h3>{@link JavaEnvironmentFactories}</h3>
 * <p>
 * Contains predefined {@link IJavaEnvironmentFactory Java environment factories}.
 * <p>
 * For a sample usage see {@link org.eclipse.scout.sdk.core.model.ecj}.
 *
 * @since 7.1.0
 * @see EmptyJavaEnvironmentFactory
 * @see RunningJavaEnvironmentFactory
 */
public final class JavaEnvironmentFactories {

  private JavaEnvironmentFactories() {
  }

  /**
   * <h3>{@link IJavaEnvironmentFactory}</h3>
   * <p>
   * Represents a factory that creates {@link IJavaEnvironment} using an {@link JavaEnvironmentWithEcjBuilder}. On the
   * factory {@link Function}s and {@link Consumer}s can be called. These get the created {@link IJavaEnvironment} as
   * input.
   *
   * @since 7.1.0
   */
  @FunctionalInterface
  public interface IJavaEnvironmentFactory extends Supplier<JavaEnvironmentWithEcjBuilder<?>> {
    /**
     * Calls the specified {@link Function} and passes a new {@link IJavaEnvironment} instance to the function. The Java
     * environment may be used during the call of this function only. The classpath of the {@link IJavaEnvironment}
     * corresponds to the settings of this factory.
     *
     * @param task
     *          The {@link Function} to call. Must not be {@code null}.
     * @return The return value of the specified {@link Function}.
     */
    default <T> T call(Function<IJavaEnvironment, T> task) {
      return get().call(task);
    }

    /**
     * Calls the specified {@link Consumer} and passes a new {@link IJavaEnvironment} instance to the consumer. The Java
     * environment may be used during the call of this consumer only. The classpath of the {@link IJavaEnvironment}
     * corresponds to the settings of this factory.
     *
     * @param task
     *          The {@link Consumer} to call. Must not be {@code null}.
     */
    default void accept(Consumer<IJavaEnvironment> task) {
      get().accept(task);
    }
  }

  /**
   * {@link IJavaEnvironment} factory with only the bootstrap classpath of the running JRE (no custom classes
   * available).
   *
   * @since 7.1.0
   */
  public static final class EmptyJavaEnvironmentFactory implements IJavaEnvironmentFactory {
    @Override
    public JavaEnvironmentWithEcjBuilder<?> get() {
      return new JavaEnvironmentWithEcjBuilder<>()
          .withoutScoutSdk()
          .withRunningClasspath(false);
    }
  }

  /**
   * {@link IJavaEnvironment} factory with the same classpath as the running JRE.
   *
   * @since 7.1.0
   */
  public static final class RunningJavaEnvironmentFactory implements IJavaEnvironmentFactory {
    @Override
    public JavaEnvironmentWithEcjBuilder<?> get() {
      return new JavaEnvironmentWithEcjBuilder<>()
          .withoutScoutSdk();
    }
  }
}
