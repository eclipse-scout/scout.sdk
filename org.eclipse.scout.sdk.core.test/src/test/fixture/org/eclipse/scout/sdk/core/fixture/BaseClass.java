/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.fixture;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 *
 */
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
