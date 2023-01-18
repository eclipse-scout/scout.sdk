/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.fixture;

import java.util.Set;

/**
 * <h3>{@link AbstractClass}</h3>
 *
 * @since 9.0.0
 */
public abstract class AbstractClass implements InterfaceWithTypeParam<Set<String>>, InterfaceWithDefaultMethod {

  protected AbstractClass() {
  }

  protected AbstractClass(Float f) {
  }

  protected abstract void voidMethod();

  public abstract String methodWithoutArgs();

  protected abstract int methodWithArgs(int a, String b, Set<String> c) throws RuntimeException;

  protected abstract int methodWithOverload(int a, String b) throws RuntimeException;

  protected abstract int methodWithOverload(int a, String b, Long c) throws RuntimeException;
}
