/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.environment;

/**
 * <h3>{@link NullProgress}</h3>
 *
 * @since 7.0.0
 */
public class NullProgress implements IProgress {

  @Override
  public IProgress init(int totalWork, CharSequence name, Object... args) {
    return this;
  }

  @Override
  public IProgress newChild(int work) {
    return this;
  }

  @Override
  public IProgress setWorkRemaining(int i) {
    return this;
  }

  @Override
  public IProgress worked(int i) {
    return this;
  }
}
