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
package org.eclipse.scout.sdk.core.fixture;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 */
@TestAnnotation(values = {Serializable.class, Runnable.class})
public class ChildClass<X extends AbstractList<String> & Runnable & Serializable> extends BaseClass<X, Long> implements InterfaceLevel0 {

  public static final String myString = "myStringValue";

  @TestAnnotation
  protected final int[][] m_test = null;

  public ChildClass() {
  }

  /**
   * @param firstParam
   * @param secondParam
   * @return
   * @throws IOException
   */
  @TestAnnotation(en = TestAnnotation.TestEnum.A, values = Long.class)
  protected synchronized boolean[] methodInChildClass(final String firstParam, final List<Runnable> secondParam) throws IOException {
    return null;
  }

  @SuppressWarnings("unused")
  private Set<HashMap<Long, List<Object>[]>>[][][] firstCase() {
    return null;
  }
}
