/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.fixture.apidef;

import org.eclipse.scout.sdk.core.apidef.MaxApiLevel;

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
