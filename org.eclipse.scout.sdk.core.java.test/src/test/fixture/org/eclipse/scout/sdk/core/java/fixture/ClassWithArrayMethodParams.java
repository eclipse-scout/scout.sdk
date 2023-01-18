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

import java.util.List;
import java.util.Map;

public final class ClassWithArrayMethodParams {

  private ClassWithArrayMethodParams() {
  }

  public static void method1(String[] arr) {
  }

  public static void method2(String[][][] arr) {
  }

  public static void method3(String... arr) {
  }

  public static void method4(List<String[]> list) {
  }

  public static void method5(boolean[] arr) {
  }

  public static <X> X method6(X[] arr) {
    return null;
  }

  public static void method7(List<Map<String, String[]>> list) {
  }
}
