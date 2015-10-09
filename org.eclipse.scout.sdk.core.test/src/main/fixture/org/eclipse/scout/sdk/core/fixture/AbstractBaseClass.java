/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.fixture;

import org.eclipse.scout.sdk.core.model.sugar.QueryTest;

/**
 * <h3>{@link AbstractBaseClass}</h3> Fixture used in {@link QueryTest}
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public abstract class AbstractBaseClass implements AutoCloseable {

  @MarkerAnnotation
  protected void blub() {
  }

  protected void blub(String overload) {

  }

  void methodWithParams(@TestAnnotation final String firstParam, Long second, Integer third) {
  }

  class InnerOne {
    class InnerTwo {
      class InnerThree {

      }
    }
  }
}
