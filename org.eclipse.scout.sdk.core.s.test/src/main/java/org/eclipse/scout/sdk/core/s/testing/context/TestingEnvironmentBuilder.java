/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.testing.context;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link TestingEnvironmentBuilder}</h3>
 *
 * @since 7.1.0
 */
public class TestingEnvironmentBuilder {

  private boolean m_flushResourcesToDisk;
  private boolean m_assertNoCompileErrors = true;
  private Consumer<Consumer<IJavaEnvironment>> m_primaryEnv;
  private Consumer<Consumer<IJavaEnvironment>> m_dtoEnv;

  public boolean isFlushResourcesToDisk() {
    return m_flushResourcesToDisk;
  }

  public TestingEnvironmentBuilder withFlushResourcesToDisk(boolean flushResourcesToDisk) {
    m_flushResourcesToDisk = flushResourcesToDisk;
    return this;
  }

  public boolean isAssertNoCompileErrors() {
    return m_assertNoCompileErrors;
  }

  public TestingEnvironmentBuilder withAssertNoCompileErrors(boolean assertNoCompileErrors) {
    m_assertNoCompileErrors = assertNoCompileErrors;
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
    var ret = new AtomicReference<T>();
    getPrimaryEnvironment().orElse(nullEnvironment())
        .accept(first -> getDtoEnvironment().orElse(nullEnvironment())
            .accept(second -> ret.set(runInTestingEnvironment(task, first, isFlushResourcesToDisk(), isAssertNoCompileErrors(), second))));
    return ret.get();
  }

  private static <T> T runInTestingEnvironment(Function<TestingEnvironment, T> task, IJavaEnvironment first, boolean isFlushResourcesToDisk, boolean assertNoCompileErrors, IJavaEnvironment second) {
    try (var env = new TestingEnvironment(first, isFlushResourcesToDisk, assertNoCompileErrors, second)) {
      return task.apply(env);
    }
  }

  private static Consumer<Consumer<IJavaEnvironment>> nullEnvironment() {
    return a -> a.accept(null);
  }
}
