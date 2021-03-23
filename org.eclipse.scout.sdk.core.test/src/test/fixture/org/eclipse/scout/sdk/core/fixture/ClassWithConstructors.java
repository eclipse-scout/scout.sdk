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
package org.eclipse.scout.sdk.core.fixture;

import java.io.IOException;

public class ClassWithConstructors {
  /**
   * JavaDoc
   */
  public ClassWithConstructors() throws IOException {
    this("whatever");
  }

  /**
   * @param other
   */
  public ClassWithConstructors(String other) throws IOException {
    throw new IOException();
  }
}
