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

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

@TestAnnotation(values = {Serializable.class, Runnable.class})
public class BaseClass<T, Z> implements InterfaceLevel1<Z> {

  @TestAnnotation
  public static final java.lang.Long myLong = 15L;

  public static final Runnable ANONYMOUS_CLASS = new Runnable() {
    @Override
    public void run() {
    }
  };

  /**
   * @param runnableParam
   * @return
   * @throws IOError
   * @throws FileNotFoundException
   */
  @TestAnnotation(values = {Serializable.class, Runnable.class})
  @MarkerAnnotation
  protected Long[][] methodInBaseClass(final Double[] runnableParam) throws IOError, FileNotFoundException {
    return null;
  }

  public final synchronized void method2InBaseClass() {

  }

  static class InnerClass1 extends ArrayList<long[]> {
    private static final long serialVersionUID = 1L;
  }

  protected class InnerClass2 extends ArrayList<BigDecimal[]> {
    private static final long serialVersionUID = 1L;

    protected InnerClass2() {
    }
  }

}
