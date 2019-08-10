/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.fixture;

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
