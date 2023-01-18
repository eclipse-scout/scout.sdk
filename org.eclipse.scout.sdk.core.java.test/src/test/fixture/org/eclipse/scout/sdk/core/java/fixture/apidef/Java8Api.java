/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.fixture.apidef;

import org.eclipse.scout.sdk.core.java.apidef.MaxApiLevel;

@MaxApiLevel(10)
public interface Java8Api extends IJavaApi {

  String VALUE = "8";

  @Override
  default String method() {
    return VALUE;
  }

  @Override
  default TestClass TestClass() {
    return new TestClass();
  }

  class TestClass implements IJavaApi.TestClass {

    public static final String TEST_CLASS_FQN = "test.Name";

    @Override
    public String fqn() {
      return TEST_CLASS_FQN;
    }
  }
}
