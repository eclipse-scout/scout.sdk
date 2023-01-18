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

/**
 * <h3>{@link AbstractBaseClass}</h3>
 *
 * @since 5.2.0
 */
public abstract class AbstractBaseClass implements AutoCloseable {

  @MarkerAnnotation
  protected void blub() {
  }

  protected void blub(String overload) {

  }

  void methodWithParams(@TestAnnotation String firstParam, Long second, Integer third) {
  }

  class InnerOne {

    class Leaf2 {
    }

    class InnerTwo {
      class Leaf3 {
      }

      class Leaf4 {
      }
    }

  }

  class Leaf {
  }
}
