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
package org.eclipse.scout.sdk.core.s.testing.context;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link TestingEnvironmentBuilder}</h3>
 *
 * @since 7.1.0
 */
public class TestingEnvironmentBuilder {

  private boolean m_flushResourcesToDisk;
  private Consumer<Consumer<IJavaEnvironment>> m_primaryEnv;
  private Consumer<Consumer<IJavaEnvironment>> m_dtoEnv;

  public boolean isFlushResourcesToDisk() {
    return m_flushResourcesToDisk;
  }

  public TestingEnvironmentBuilder withFlushResourcesToDisk(boolean flushResourcesToDisk) {
    m_flushResourcesToDisk = flushResourcesToDisk;
    return this;
  }

  public Optional<Consumer<Consumer<IJavaEnvironment>>> getPrimaryEnvironment() {
    return Optional.ofNullable(m_primaryEnv);
  }

  public TestingEnvironmentBuilder withPrimaryEnvironment(Consumer<Consumer<IJavaEnvironment>> primaryEnv) {
    m_primaryEnv = primaryEnv;
    return this;
  }

  public Optional<Consumer<Consumer<IJavaEnvironment>>> getDtoEnvironment() {
    return Optional.ofNullable(m_dtoEnv);
  }

  public TestingEnvironmentBuilder withDtoEnvironment(Consumer<Consumer<IJavaEnvironment>> dtoEnv) {
    m_dtoEnv = dtoEnv;
    return this;
  }

  public void run(Consumer<TestingEnvironment> task) {
    call(env -> {
      task.accept(env);
      return null;
    });
  }

  public <T> T call(Function<TestingEnvironment, T> task) {
    Ensure.notNull(task);
    AtomicReference<T> ret = new AtomicReference<>();
    getPrimaryEnvironment().orElse(nullEnvironment())
        .accept(first -> getDtoEnvironment().orElse(nullEnvironment())
            .accept(second -> ret.set(runInTestingEnvironment(task, first, isFlushResourcesToDisk(), second))));
    return ret.get();
  }

  private static <T> T runInTestingEnvironment(Function<TestingEnvironment, T> task, IJavaEnvironment first, boolean isFlushResourcesToDisk, IJavaEnvironment second) {
    try (TestingEnvironment env = new TestingEnvironment(first, isFlushResourcesToDisk, second)) {
      return task.apply(env);
    }
  }

  private static Consumer<Consumer<IJavaEnvironment>> nullEnvironment() {
    return a -> a.accept(null);
  }
}
