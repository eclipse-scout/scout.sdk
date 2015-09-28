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

import static java.math.RoundingMode.UP;

import java.math.RoundingMode;
import java.util.Map;

import org.eclipse.scout.sdk.core.fixture.TestAnnotation.TestEnum;

/**
 * <h3>{@link ClassWithMembers}</h3> used in SourceModelRoundtripTest
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
@SuppressWarnings("unused")
public class ClassWithMembers<T> {
  String a1;
  public String a2;
  protected String a3;
  private String a4;
  private final String a5 = "5";

  private static String b1;
  private static final String b2 = "2";
  private static final String b3 = UP.name();

  private String c1 = "abc";
  private String c2 = RoundingMode.DOWN.name();
  private String c3 = a5;
  private String c4 = b2;

  private int d1 = 1;
  private int d2 = 2;

  private Map<T, String> e1;

  static {
    System.out.println("static section");
  }

  @TestAnnotation(en = TestEnum.A)
  public String m1(@TestAnnotation(en = TestEnum.B) String type) {
    System.out.println("m1");
    return null;
  }

  @TestAnnotation(en = TestEnum.A)
  public T m2(@TestAnnotation(en = TestAnnotation.TestEnum.B) Class<T> type) {
    System.out.println("m2");
    return null;
  }

  @TestAnnotation(en = org.eclipse.scout.sdk.core.fixture.TestAnnotation.TestEnum.A)
  public <U extends T, V extends U> U m3(@TestAnnotation(en = TestEnum.B) Class<U> type, V v, T t) {
    System.out.println("m3");
    return null;
  }

  public class InnerMemberClass {
  }

  public static class InnerStaticClass {
  }

}
