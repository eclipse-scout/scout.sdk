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
package org.eclipse.scout.sdk.core.signature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SignatureTest {

  /**
   * @see Signature
   */
  @Test
  public void testCreateArraySignature() {
    assertEquals(
        "Signature#createArraySignature not correct",
        "[[[[QString",
        Signature.createArraySignature("QString", 4));

    // tests with 1.5-specific elements
    assertEquals(
        "Signature#createArraySignature not correct",
        "[[[[Qlist<Qstring;>;",
        Signature.createArraySignature("Qlist<Qstring;>;", 4));

  }

  /**
   * @see Signature
   */
  @Test
  public void testCreateMethodSignature() {
    assertEquals(
        "Signature#createMethodSignature is not correct 1",
        "(QString;QObject;I)I",
        Signature.createMethodSignature(new String[]{"QString;", "QObject;", "I"}, "I"));
    assertEquals(
        "Signature#createMethodSignature is not correct 2",
        "()Ljava.lang.String;",
        Signature.createMethodSignature(new String[]{}, "Ljava.lang.String;"));
  }

  /**
   * @see Signature
   */
  @Test
  public void testCreateTypeSignature() {
    assertEquals("Signature#createTypeSignature is not correct1", "I",
        Signature.createTypeSignature("int".toCharArray(), false));
    assertEquals("Signature#createTypeSignature is not correct2", "Ljava.lang.String;",
        Signature.createTypeSignature("java.lang.String".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct3", "QString;",
        Signature.createTypeSignature("String".toCharArray(), false));
    assertEquals("Signature#createTypeSignature is not correct4", "Qjava.lang.String;",
        Signature.createTypeSignature("java.lang.String".toCharArray(), false));
    assertEquals("Signature#createTypeSignature is not correct5", "[I",
        Signature.createTypeSignature("int []".toCharArray(), false));
    assertEquals("Signature#createTypeSignature is not correct6", "[QString;",
        Signature.createTypeSignature("String []".toCharArray(), false));
    assertEquals("Signature#createTypeSignature is not correct7", "[Ljava.util.Vector;",
        Signature.createTypeSignature("java.util.Vector []".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct8", "[[Ljava.util.Vector;",
        Signature.createTypeSignature("java .\n util  .  Vector[  ][]".toCharArray(), true));
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=41019
    assertEquals("Signature#createTypeSignature is not correct9", "Linteration.test.MyData;",
        Signature.createTypeSignature("interation.test.MyData".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct10", "Llongtest.MyData;",
        Signature.createTypeSignature("longtest.MyData".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct11", "Lbooleantest.MyData;",
        Signature.createTypeSignature("booleantest.MyData".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct12", "Lbytetest.MyData;",
        Signature.createTypeSignature("bytetest.MyData".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct13", "Lchartest.MyData;",
        Signature.createTypeSignature("chartest.MyData".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct14", "Lshorttest.MyData;",
        Signature.createTypeSignature("shorttest.MyData".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct15", "Ldoubletest.MyData;",
        Signature.createTypeSignature("doubletest.MyData".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct16", "Lfloattest.MyData;",
        Signature.createTypeSignature("floattest.MyData".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct17", "Lvoidtest.MyData;",
        Signature.createTypeSignature("voidtest.MyData".toCharArray(), true));
    assertEquals("Signature#createTypeSignature is not correct18", "QList<QList<QString;>;>;",
        Signature.createTypeSignature("List<List<String>>".toCharArray(), false));
    assertEquals("Signature#createTypeSignature is not correct19", "QList<QList<I>;>;",
        Signature.createTypeSignature("List<List<int>>".toCharArray(), false));
    assertEquals("Signature#createTypeSignature is not correct20", "[QList<QList<[I>;>;",
        Signature.createTypeSignature("List<List<int[]>>[]".toCharArray(), false));
    assertEquals("Signature#createTypeSignature is not correct21", "Qjava.y.Map<[QObject;QString;>.MapEntry<[Qp.K<QT;>;[Qq.r.V2;>;",
        Signature.createTypeSignature("java.y.Map<Object[],String>.MapEntry<p.K<T>[],q.r.V2[]>".toCharArray(), false));
  }

  /**
   * Ensures that creating an invalid type signature throws an IllegalArgumentException.
   */
  @Test
  public void testCreateInvalidTypeSignature() {
    assertInvalidTypeSignature(null, false, null);
    assertInvalidTypeSignature("", false, "");
    assertInvalidTypeSignature("int.Y", false, "I");
    assertInvalidTypeSignature("Y [].X", false, "[QY;");
    assertInvalidTypeSignature("X[[]", true, "[[LX;");
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetArrayCount() {
    assertEquals("Signature#getArrayCount is not correct", 4,
        Signature.getArrayCount("[[[[QString;"));
    try {
      Signature.getArrayCount("");
      assertTrue("Signature#getArrayCount is not correct, exception", false);
    }
    catch (IllegalArgumentException iae) {
      // do nothing
    }

    // tests with 1.5-specific elements
    assertEquals(
        "Signature#getArrayCount not correct", 4,
        Signature.getArrayCount("[[[[Qlist<Qstring;>;"));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetElementType() {
    assertEquals("Signature#getElementType is not correct1", "QString;",
        Signature.getElementType("[[[[QString;"));
    assertEquals("Signature#getElementType is not correct2", "QString;",
        Signature.getElementType("QString;"));
    assertEquals("Signature#getElementType is not correct2", "I",
        Signature.getElementType("[[I"));
    try {
      Signature.getElementType("");
      assertTrue("Signature#getArrayCount is not correct, exception", false);
    }
    catch (IllegalArgumentException iae) {
      // do nothing
    }

    // tests with 1.5-specific elements
    assertEquals(
        "Signature#getElementType not correct", "Qlist<Qstring;>;",
        Signature.getElementType("[[[[Qlist<Qstring;>;"));
  }

  @Test
  public void testGetElementType2() {
    String typeSign = "Ljava.util.List;";
    assertTrue(Signature.getElementType(typeSign) == typeSign);
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetParameterCount01() {
    String methodSig = "(QString;QObject;I)I";
    assertEquals("Signature#getParameterCount is not correct", 3,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount02() {
    try {
      Signature.getParameterCount("");
      assertTrue("Signature#getParameterCount is not correct: exception", false);
    }
    catch (IllegalArgumentException iae) {
      // do nothing
    }
  }

// tests with 1.5-specific elements
  @Test
  public void testGetParameterCount03() {
    String methodSig = "<X:Qlist<Qstring;>;>(IQlist;Tww;)Qlist<Qxxx;>;^Qexception;^Qerror;";
    assertEquals("Signature#getParameterCount is not correct", 3,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount04() {
    String methodSig = "<X:Qlist<Qstring;>;>(IQlist<Qstring;>;Tww;)Qlist<Qxxx;>;^Qexception;^Qerror;";
    assertEquals("Signature#getParameterCount is not correct", 3,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount05() {
    String methodSig = "foo(+Ljava.lang.Comparable;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount06() {
    String methodSig = "foo(+Ljava.lang.Comparable;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount07() {
    String methodSig = "foo(*)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount08() {
    String methodSig = "foo(LA<+Ljava.lang.Comparable;>;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount09() {
    String methodSig = "foo(LA<-Ljava.lang.Comparable;>;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount10() {
    String methodSig = "foo(LA<*>;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount11() {
    String methodSig = "foo(LA<+Ljava.lang.Comparable;+Ljava.lang.Comparable;>;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount12() {
    String methodSig = "foo(+Ljava.lang.Comparable;+Ljava.lang.Comparable;)";
    assertEquals("Signature#getParameterCount is not correct", 2,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount13() {
    String methodSig = "foo(+Ljava.lang.Comparable;-Ljava.lang.Comparable;)";
    assertEquals("Signature#getParameterCount is not correct", 2,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount14() {
    String methodSig = "foo(Ljava.util.List<-[Ljava.lang.Number;>;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount15() {
    String methodSig = "foo(LA<++Ljava.lang.Comparable;>;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount16() {
    String methodSig = "foo(LA<--Ljava.lang.Comparable;>;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  @Test
  public void testGetParameterCount17() {
    String methodSig = "foo(LA<+-Ljava.lang.Comparable;>;)";
    assertEquals("Signature#getParameterCount is not correct", 1,
        Signature.getParameterCount(methodSig));
  }

  /**
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=267432
   */
  @Test
  public void testGetParameterCount19() {
    String methodSig = "<TYPE:Ljava/lang/Object;>(Ljava/lang/Object;Ljava/lang/Class<TTYPE;>;)TTYPE;";
    assertEquals("Signature#getParameterCount is not correct", 2,
        Signature.getParameterCount(methodSig));
  }

  /**
   * Ensures that a signature with a '? extends ?' wildcard can be decoded. (regression test for bug 92370 [1.5] IAE in
   * Signature.getParameterCount(..) for method proposal on capture type receiver)
   */
  @Test
  public void testGetParameterCount18() {
    String methodSig = "(ILjava.util.Collection<+*>;)Z";
    assertEquals("Signature#getParameterCount is not correct", 2,
        Signature.getParameterCount(methodSig));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetParameterTypes() {
    String methodSig = "(QString;QObject;I)I";
    String[] parameterTypes = Signature.getParameterTypes(methodSig);
    assertEquals("Signature#getParameterTypes is not correct1", 3, parameterTypes.length);
    assertEquals("Signature#getParameterTypes is not correct2", "QObject;", parameterTypes[1]);
    try {
      Signature.getParameterTypes("");
      assertTrue("Signature#getParameterTypes is not correct: exception", false);
    }
    catch (IllegalArgumentException iae) {
      // do nothing
    }

    // primitive types
    methodSig = "(BCDFIJSVZ)V";
    parameterTypes = Signature.getParameterTypes(methodSig);
    assertEquals("Signature#getParameterTypes 4", 9,
        parameterTypes.length);
    assertEquals("Signature#getParameterTypes 4", "B",
        parameterTypes[0]);
    assertEquals("Signature#getParameterTypes 4", "C",
        parameterTypes[1]);
    assertEquals("Signature#getParameterTypes 4", "D",
        parameterTypes[2]);
    assertEquals("Signature#getParameterTypes 4", "F",
        parameterTypes[3]);
    assertEquals("Signature#getParameterTypes 4", "I",
        parameterTypes[4]);
    assertEquals("Signature#getParameterTypes 4", "J",
        parameterTypes[5]);
    assertEquals("Signature#getParameterTypes 4", "S",
        parameterTypes[6]);
    assertEquals("Signature#getParameterTypes 4", "V",
        parameterTypes[7]);
    assertEquals("Signature#getParameterTypes 4", "Z",
        parameterTypes[8]);

    // array types
    methodSig = "([I[[J[[[B[Qstring;[Tv;[Lstring;)V";
    parameterTypes = Signature.getParameterTypes(methodSig);
    assertEquals("Signature#getParameterTypes 5", 6,
        parameterTypes.length);
    assertEquals("Signature#getParameterTypes 5", "[I",
        parameterTypes[0]);
    assertEquals("Signature#getParameterTypes 5", "[[J",
        parameterTypes[1]);
    assertEquals("Signature#getParameterTypes 5", "[[[B",
        parameterTypes[2]);
    assertEquals("Signature#getParameterTypes 5", "[Qstring;",
        parameterTypes[3]);
    assertEquals("Signature#getParameterTypes 5", "[Tv;",
        parameterTypes[4]);
    assertEquals("Signature#getParameterTypes 5", "[Lstring;",
        parameterTypes[5]);

    // resolved types
    methodSig = "(La;)V";
    parameterTypes = Signature.getParameterTypes(methodSig);
    assertEquals("Signature#getParameterTypes 6", 1, parameterTypes.length);
    assertEquals("Signature#getParameterTypes 6", "La;", parameterTypes[0]);

    methodSig = "(La<TE;>;)V";
    parameterTypes = Signature.getParameterTypes(methodSig);
    assertEquals("Signature#getParameterTypes 6", 1,
        parameterTypes.length);
    assertEquals("Signature#getParameterTypes 6", "La<TE;>;",
        parameterTypes[0]);

    methodSig = "(La.b.c<TE;>.d<TF;>;)V";
    parameterTypes = Signature.getParameterTypes(methodSig);
    assertEquals("Signature#getParameterTypes 6", 1,
        parameterTypes.length);
    assertEquals("Signature#getParameterTypes 6", "La.b.c<TE;>.d<TF;>;",
        parameterTypes[0]);

    // signature with type variable
    methodSig = "<TYPE:Ljava.lang.Object;>(Ljava.lang.Object;Ljava.lang.Class<TTYPE;>;)TTYPE;";
    parameterTypes = Signature.getParameterTypes(methodSig);
    assertEquals("Signature#getParameterTypes 7", 2,
        parameterTypes.length);
    assertEquals("Signature#getParameterTypes 7", "Ljava.lang.Object;",
        parameterTypes[0]);
    assertEquals("Signature#getParameterTypes 7", "Ljava.lang.Class<TTYPE;>;",
        parameterTypes[1]);
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetTypeParameters1() {
    String sig = "<X:TF;Y::Ljava.lang.Cloneable;>";
    assertStringsEqual(
        "Unexpected type parameters",
        "X:TF;\n" +
            "Y::Ljava.lang.Cloneable;\n",
        Signature.getTypeParameters(sig));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetTypeParameters2() {
    String sig = "<X:TF;Y::Ljava.lang.Cloneable;>()V";
    assertStringsEqual(
        "Unexpected type parameters",
        "X:TF;\n" +
            "Y::Ljava.lang.Cloneable;\n",
        Signature.getTypeParameters(sig));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetTypeParameters3() {
    String sig = "<E:>Ljava.util.AbstractCollection;";
    assertStringsEqual(
        "Unexpected type parameters",
        "E:\n",
        Signature.getTypeParameters(sig));
  }

  /**
   * @see Signature (regression test for bug 93662 Singature#getTypeParameters returns strange signature string)
   */
  @Test
  public void testGetTypeParameters4() {
    String sig = "<K:V:>Ljava.util.AbstractMap;";
    assertStringsEqual(
        "Unexpected type parameters",
        "K:\n" +
            "V:\n",
        Signature.getTypeParameters(sig));
  }

  /**
   * @see Signature (regression test for bug 93662 Singature#getTypeParameters returns strange signature string)
   */
  @Test
  public void testGetTypeParameters5() {
    String sig = "<L:T:>Ljava.util.AbstractMap;";
    assertStringsEqual(
        "Unexpected type parameters",
        "L:\n" +
            "T:\n",
        Signature.getTypeParameters(sig));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetTypeParameters6() {
    String sig = "<E::Lp/I;>Lp1/X;";
    assertStringsEqual(
        "Unexpected type parameters",
        "E::Lp/I;\n",
        Signature.getTypeParameters(sig));
  }

  /**
   * @see Signature (regression test for Bug 466512: Unexpected runtime error while computing a text hover)
   */
  @Test
  public void testGetTypeParameters7() {
    String sig = "<S:!-TT;>(Ljava.lang.Class<!-TT;>;)Lxy.HoverTest$TestClass<!-TT;>;";
    assertStringsEqual(
        "Unexpected type parameters",
        "S:!-TT;\n",
        Signature.getTypeParameters(sig));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetQualifier1() {
    assertEquals(
        "java.lang",
        Signature.getQualifier("java.lang.Object"));
  }

  @Test
  public void testGetQualifier2() {
    assertEquals(
        "",
        Signature.getQualifier(""));
  }

  @Test
  public void testGetQualifier3() {
    assertEquals(
        "java.util",
        Signature.getQualifier("java.util.List<java.lang.Object>"));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetReturnType() {
    String methodSig = "(QString;QObject;I)I";
    assertEquals("Signature#getReturnType is not correct1", "I",
        Signature.getReturnType(methodSig));
    try {
      Signature.getReturnType("");
      assertTrue("Signature#getReturnType is not correct: exception", false);
    }
    catch (IllegalArgumentException iae) {
      // do nothing
    }

    // tests with 1.5-specific elements
    methodSig = "<X:Qlist<Qstring;>;>(Qstring;Qobject;I)IQexception;Qerror;";
    assertEquals("Signature#getReturnType is not correct2", "I",
        Signature.getReturnType(methodSig));
    methodSig = "<X:Qlist<Qstring;>;>(Qlist<Qstring;>;)Qlist<Qxxx;>;Qexception;Qerror;";
    assertEquals("Signature#getReturnType is not correct3", "Qlist<Qxxx;>;",
        Signature.getReturnType(methodSig));
  }

  /**
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=112030
   *
   * @see Signature
   */
  @Test
  public void testGetReturnType2() {
    String methodSig = "(LObject;)+[I";
    assertEquals("Signature#getReturnType is not correct", "+[I",
        Signature.getReturnType(methodSig));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetThrownExceptionTypes() {
    String methodSig = "(QString;QObject;I)I";
    assertStringsEqual("Signature#getThrownExceptionTypes is not correct1", "",
        Signature.getThrownExceptionTypes(methodSig));
    try {
      Signature.getThrownExceptionTypes("");
      assertTrue("Signature#getThrownExceptionTypes is not correct: exception", false);
    }
    catch (IllegalArgumentException iae) {
      // do nothing
    }

    // tests with 1.5-specific elements
    methodSig = "<X:Qlist<Qstring;>;>(Qstring;Qobject;I)I^Qexception;^Qerror;";
    assertStringsEqual("Signature#getThrownExceptionTypes is not correct2", "Qexception;\nQerror;\n",
        Signature.getThrownExceptionTypes(methodSig));
    methodSig = "<X:Qlist<Qstring;>;>(Qlist<Qstring;>;)Qlist<Qxxx;>;^Qexception<TT;>;^Qerror;";
    assertStringsEqual("Signature#getThrownExceptionTypes is not correct3", "Qexception<TT;>;\nQerror;\n",
        Signature.getThrownExceptionTypes(methodSig));

    methodSig = "<T:Ljava/lang/Exception;>()V^TT;";
    assertStringsEqual("Signature#getThrownExceptionTypes is not correct3", "TT;\n",
        Signature.getThrownExceptionTypes(methodSig));
    methodSig = "<T:Ljava/lang/Exception;>()V^TT;^Ljava/lang/Exception;";
    assertStringsEqual("Signature#getThrownExceptionTypes is not correct3", "TT;\nLjava/lang/Exception;\n",
        Signature.getThrownExceptionTypes(methodSig));

    try {
      Signature.getThrownExceptionTypes("<T:Ljava/lang/Exception;>()VTT;");
      assertTrue("Signature#getThrownExceptionTypes is not correct: exception", false);
    }
    catch (IllegalArgumentException iae) {
      // do nothing
    }

    try {
      Signature.getThrownExceptionTypes("<T:Ljava/lang/Exception;>()V^TT;Ljava/lang/Exception;");
      assertTrue("Signature#getThrownExceptionTypes is not correct: exception", false);
    }
    catch (IllegalArgumentException iae) {
    }

  }

  /**
   * @bug 155003: [model] Missing exception types / wrong signature?
   * @test Ensure that thrown exceptions are well decoded when added at the end of the signature
   * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=155003"
   */
  @Test
  public void testGetThrownExceptions_Bug155003() {
    String methodSig = "()Ljava.lang.Object;^Ljava.lang.InstantiationException;^Ljava.lang.IllegalAccessException;";
    assertStringsEqual("Signature#Bug155003#1 is not correct",
        "Ljava.lang.InstantiationException;\nLjava.lang.IllegalAccessException;\n",
        Signature.getThrownExceptionTypes(methodSig));
    methodSig = "()V"; // no change when no thrown exceptions
    assertStringsEqual("Signature#Bug155003#2 is not correct",
        "",
        Signature.getThrownExceptionTypes(methodSig));
  }

  /**
   * @see Signature
   * @since 3.0
   */
  @Test
  public void testGetTypeVariable() {
    // tests with 1.5-specific elements
    String formalTypeParameterSignature = "Hello:";
    assertEquals("Signature#getTypeVariable is not correct1", "Hello",
        Signature.getTypeVariable(formalTypeParameterSignature));
    formalTypeParameterSignature = "Hello::Qi1;:Qi2;";
    assertEquals("Signature#getTypeVariable is not correct2", "Hello",
        Signature.getTypeVariable(formalTypeParameterSignature));
    formalTypeParameterSignature = "Hello:Qlist<Qstring;>;:Qi1;:Qi2;";
    assertEquals("Signature#getTypeVariable is not correct3", "Hello",
        Signature.getTypeVariable(formalTypeParameterSignature));
    try {
      Signature.getTypeVariable("");
      assertTrue("Signature#getTypeVariable is not correct: exception", false);
    }
    catch (IllegalArgumentException iae) {
      // do nothing
    }
  }

  /**
   * Ensures that an invalid character doesn't cause an infinite loop (regression test for
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=247118 )
   */
  @Test
  public void testInvalidCharacter() {
    assertEquals(
        "Lorg.eclipse.Identified.ClassName;",
        Signature.createTypeSignature("org.eclipse." + (char) 5760 + "Identified.ClassName", true));
  }

  /**
   * @see Signature
   * @since 3.0
   */
  @Test
  public void testGetTypeParameterBounds() {
    // tests with 1.5-specific elements
    String formalTypeParameterSignature = "Hello:";
    assertEquals("Signature#getTypeParameterBounds is not correct1", 0,
        Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
    formalTypeParameterSignature = "Hello::Qi1;:Qi2;";
    assertEquals("Signature#getTypeParameterBounds is not correct2", 2,
        Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
    assertEquals("Signature#getTypeParameterBounds is not correct2a", "Qi1;",
        Signature.getTypeParameterBounds(formalTypeParameterSignature)[0]);
    assertEquals("Signature#getTypeParameterBounds is not correct2b", "Qi2;",
        Signature.getTypeParameterBounds(formalTypeParameterSignature)[1]);
    formalTypeParameterSignature = "Hello:Qlist<Qstring;>;:Qi1;:Qi2;";
    assertEquals("Signature#getTypeParameterBounds is not correct3", 3,
        Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
    assertEquals("Signature#getTypeParameterBounds is not correct3a", "Qlist<Qstring;>;",
        Signature.getTypeParameterBounds(formalTypeParameterSignature)[0]);
    assertEquals("Signature#getTypeParameterBounds is not correct3b", "Qi1;",
        Signature.getTypeParameterBounds(formalTypeParameterSignature)[1]);
    assertEquals("Signature#getTypeParameterBounds is not correct3c", "Qi2;",
        Signature.getTypeParameterBounds(formalTypeParameterSignature)[2]);
    formalTypeParameterSignature = "Hello:Qi1;";
    assertEquals("Signature#getTypeParameterBounds is not correct4", 1,
        Signature.getTypeParameterBounds(formalTypeParameterSignature).length);
    assertEquals("Signature#getTypeParameterBounds is not correct4a", "Qi1;",
        Signature.getTypeParameterBounds(formalTypeParameterSignature)[0]);
    try {
      Signature.getTypeParameterBounds("");
      assertTrue("Signature#getTypeParameterBounds is not correct: exception", false);
    }
    catch (IllegalArgumentException iae) {
      // do nothing
    }
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetTypeArguments1() {
    assertStringsEqual(
        "Unexpected type arguments",
        "QT;\n",
        Signature.getTypeArguments("QList<QT;>;"));
  }

  @Test
  public void testGetTypeArguments2() {
    assertStringsEqual(
        "Unexpected type arguments",
        "QT;\n" +
            "QU;\n",
        Signature.getTypeArguments("QX<QT;QU;>;"));
  }

  @Test
  public void testGetTypeArguments3() {
    assertStringsEqual(
        "Unexpected type arguments",
        "*\n",
        Signature.getTypeArguments("QX<*>;"));
  }

  @Test
  public void testGetTypeArguments4() {
    assertStringsEqual(
        "Unexpected type arguments",
        "+QE;\n" +
            "-QS;\n",
        Signature.getTypeArguments("QX<+QE;-QS;>;"));
  }

  @Test
  public void testGetTypeArguments5() {
    assertStringsEqual(
        "Unexpected type arguments",
        "QList<QT;>;\n" +
            "QMap<QU;QABC<QT;>;>;\n",
        Signature.getTypeArguments("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>;"));
  }

  /**
   * getTypeArguments() on a raw type (regression test for bug 73671 [1.5] Signature.getTypeArguments should also
   * tolerate normal types)
   */
  @Test
  public void testGetTypeArguments6() {
    assertStringsEqual(
        "Unexpected type arguments",
        "",
        Signature.getTypeArguments("QList;"));
  }

  @Test
  public void testGetTypeArguments7() {
    assertStringsEqual(
        "Unexpected type arguments",
        "",
        Signature.getTypeArguments("QX<QObject;>.Member;"));
  }

  @Test
  public void testGetTypeArguments8() {
    assertStringsEqual(
        "Unexpected type arguments",
        "QObject;\n",
        Signature.getTypeArguments("QX<QObject;>.Member<QObject;>;"));
  }

  @Test
  public void testGetTypeArguments9() {
    assertStringsEqual(
        "Unexpected type arguments",
        "QObject;\n",
        Signature.getTypeArguments("QX.Member<QObject;>;"));
  }

  @Test
  public void testGetTypeArguments10() {
    assertStringsEqual(
        "Unexpected type arguments",
        "QList<QT;>;\n" +
            "QMap<QU;QABC<QT;>;>;\n",
        Signature.getTypeArguments("QX<QObject;>.Member<QList<QT;>;QMap<QU;QABC<QT;>;>;>;"));
  }

  @Test
  public void testGetTypeArguments11() {
    assertStringsEqual(
        "Unexpected type arguments",
        "QObject;\n",
        Signature.getTypeArguments("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>.Member<QObject;>;"));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetTypeErasure1() {
    assertEquals(
        "QList;",
        Signature.getTypeErasure("QList<QT;>;"));
  }

  @Test
  public void testGetTypeErasure2() {
    assertEquals(
        "QList;",
        Signature.getTypeErasure("QList;"));
  }

  @Test
  public void testGetTypeErasure3() {
    assertEquals(
        "QX;",
        Signature.getTypeErasure("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>;"));
  }

  @Test
  public void testGetTypeErasure4() {
    assertEquals(
        "QX.Member;",
        Signature.getTypeErasure("QX<QObject;>.Member;"));
  }

  @Test
  public void testGetTypeErasure5() {
    assertEquals(
        "QX.Member;",
        Signature.getTypeErasure("QX<QObject;>.Member<QObject;>;"));
  }

  @Test
  public void testGetTypeErasure6() {
    assertEquals(
        "QX.Member;",
        Signature.getTypeErasure("QX.Member<QObject;>;"));
  }

  @Test
  public void testGetTypeErasure7() {
    assertEquals(
        "QX.Member;",
        Signature.getTypeErasure("QX<QObject;>.Member<QList<QT;>;QMap<QU;QABC<QT;>;>;>;"));
  }

  @Test
  public void testGetTypeErasure8() {
    assertEquals(
        "QX.Member;",
        Signature.getTypeErasure("QX<QList<QT;>;QMap<QU;QABC<QT;>;>;>.Member<QObject;>;"));
  }

  @Test
  public void testGetTypeErasure9() {
    String sign = "Ljava.util.List;";
    assertTrue(Signature.getTypeErasure(sign) == sign);
  }

  @Test
  public void testGetTypeErasure10() {
    assertEquals(
        "Ljava.util.List;",
        Signature.getTypeErasure("Ljava.util.List<-[Ljava.lang.Number;>;"));
  }

  @Test
  public void testGetTypeErasure11() {
    String sign = "|Ljava.util.List;:Ljava.lang.Cloneable;";
    assertTrue(Signature.getTypeErasure(sign) == sign);
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetSimpleName01() {
    assertEquals("Signature#getSimpleName is not correct 1", "Object",
        Signature.getSimpleName("java.lang.Object"));
  }

  @Test
  public void testGetSimpleName02() {
    assertEquals("Signature#getSimpleName is not correct 2", "",
        Signature.getSimpleName(""));
  }

  @Test
  public void testGetSimpleName03() {
    assertEquals("Signature#getSimpleName is not correct 3",
        "MapEntry<K<T>[],V2[]>",
        Signature.getSimpleName("java.y.Map<Object[],String>.MapEntry<p.K<T>[],q.r.V2[]>"));
  }

  @Test
  public void testGetSimpleName04() {
    assertEquals("Signature#getSimpleName is not correct 4",
        "MapEntry<K<T>[],? extends V2>",
        Signature.getSimpleName("java.y.Map<Object[],String>.MapEntry<p.K<T>[],? extends q.r.V2>"));
  }

  @Test
  public void testGetSimpleName05() {
    assertEquals("Unexpected simple name", "List<?>", Signature.getSimpleName("List<?>"));
  }

  @Test
  public void testGetSimpleName06() {
    assertEquals("Unexpected simple name", "List<? extends E>", Signature.getSimpleName("List<?extends E>"));
  }

  @Test
  public void testGetSimpleName07() {
    assertEquals("Unexpected simple name", "List<? super E>", Signature.getSimpleName("List<?super E>"));
  }

  @Test
  public void testGetSimpleName08() {
    assertEquals("Unexpected simple name", "List<+Comparable<-TT;>;>;", Signature.getSimpleName(" java.util.List<+Ljava.lang.Comparable<-TT;>;>;"));
  }

  /**
   * @see Signature
   */
  @Test
  public void testGetSimpleNames01() {
    assertStringsEqual(
        "Unexpected simple names",
        "java\n" +
            "lang\n" +
            "Object\n",
        Signature.getSimpleNames("java.lang.Object"));
  }

  @Test
  public void testGetSimpleNames02() {
    assertStringsEqual(
        "Unexpected simple names",
        "",
        Signature.getSimpleNames(""));
  }

  @Test
  public void testGetSimpleNames03() {
    assertStringsEqual(
        "Unexpected simple names",
        "Object\n",
        Signature.getSimpleNames("Object"));
  }

  @Test
  public void testGetSimpleNames04() {
    assertStringsEqual(
        "Unexpected simple names",
        "java\n" +
            "util\n" +
            "List<java.lang.String>\n",
        Signature.getSimpleNames("java.util.List<java.lang.String>"));
  }

  @Test
  public void testGetSignaturesSimpleName01() {
    assertEquals(
        "Unexpected simple names",
        "? extends CharSequence",
        Signature.getSignatureSimpleName("+Ljava.lang.CharSequence;"));
  }

  @Test
  public void testGetSignaturesSimpleName02() {
    assertEquals(
        "Unexpected simple names",
        "? extends CharSequence",
        Signature.getSignatureSimpleName("+QCharSequence;"));
  }

  /**
   * @see Signature
   */
  @Test
  public void testToQualifiedName() {
    assertEquals("Signature#toQualifiedName is not correct1", "java.lang.Object",
        Signature.toQualifiedName(new String[]{"java", "lang", "Object"}));
    assertEquals("Signature#toQualifiedName is not correct2", "Object",
        Signature.toQualifiedName(new String[]{"Object"}));
    assertEquals("Signature#toQualifiedName is not correct3", "",
        Signature.toQualifiedName(new String[0]));
  }

  /**
   * Ensures that the toString() signature of an anonymous type is correct (regression test for bug 180713 Anonymous
   * type rendered as number in hover)
   */
  @Test
  public void testToStringAnonymousType() {
    assertEquals(
        "Signature#toString is not correct",
        "new X(){}",
        Signature.toString("LX$123;"));
  }

  /**
   * @see Signature#toString(String)
   */
  @Test
  public void testToStringType01() {
    assertEquals(
        "java/lang/String",
        Signature.toString("Ljava/lang/String;"));
  }

  @Test
  public void testToStringType02() {
    assertEquals(
        "java.lang.String",
        Signature.toString("Ljava.lang.String;"));
  }

  @Test
  public void testToStringType03() {
    assertEquals(
        "java.lang.String[]",
        Signature.toString("[Ljava.lang.String;"));
  }

  @Test
  public void testToStringType04() {
    assertEquals(
        "String",
        Signature.toString("QString;"));
  }

  @Test
  public void testToStringType05() {
    assertEquals(
        "String[][]",
        Signature.toString("[[QString;"));
  }

  @Test
  public void testToStringType06() {
    assertEquals(
        "boolean",
        Signature.toString("Z"));
  }

  @Test
  public void testToStringType07() {
    assertEquals(
        "byte",
        Signature.toString("B"));
  }

  @Test
  public void testToStringType08() {
    assertEquals(
        "char",
        Signature.toString("C"));
  }

  @Test
  public void testToStringType09() {
    assertEquals(
        "double",
        Signature.toString("D"));
  }

  @Test
  public void testToStringType10() {
    assertEquals(
        "float",
        Signature.toString("F"));
  }

  @Test
  public void testToStringType11() {
    assertEquals(
        "int",
        Signature.toString("I"));
  }

  @Test
  public void testToStringType12() {
    assertEquals(
        "long",
        Signature.toString("J"));
  }

  @Test
  public void testToStringType13() {
    assertEquals(
        "short",
        Signature.toString("S"));
  }

  @Test
  public void testToStringType14() {
    assertEquals(
        "void",
        Signature.toString("V"));
  }

  @Test
  public void testToStringType15() {
    assertEquals(
        "int[][][]",
        Signature.toString("[[[I"));
  }

// signatures with 1.5 elements
  @Test
  public void testToStringType16() {
    assertEquals(
        "VAR",
        Signature.toString("TVAR;"));
  }

  @Test
  public void testToStringType17() {
    assertEquals(
        "A<B>",
        Signature.toString("QA<QB;>;"));
  }

  @Test
  public void testToStringType18() {
    assertEquals(
        "A<?>",
        Signature.toString("QA<*>;"));
  }

  @Test
  public void testToStringType19() {
    assertEquals(
        "A<? extends B>",
        Signature.toString("QA<+QB;>;"));
  }

  @Test
  public void testToStringType20() {
    assertEquals(
        "A<? super B>",
        Signature.toString("QA<-QB;>;"));
  }

  @Test
  public void testToStringType21() {
    assertEquals(
        "A<?,?,?,?,?>",
        Signature.toString("LA<*****>;"));
  }

  @Test
  public void testToStringType22() {
    assertEquals(
        "a<V>.b<W>.c<X>",
        Signature.toString("La<TV;>.b<QW;>.c<LX;>;"));
  }

  @Test
  public void testToStringType23() {
    assertEquals(
        "java.y.Map<Object[],String>.MapEntry<p.K<T>[],q.r.V2[]>",
        Signature.toString("Qjava.y.Map<[QObject;QString;>.MapEntry<[Qp.K<QT;>;[Qq.r.V2;>;"));
  }

  @Test
  public void testToStringType24() {
    assertEquals(
        "Stack<List<Object>>",
        Signature.toString("QStack<QList<QObject;>;>;"));
  }

  @Test
  public void testToStringType25() {
    assertEquals(
        "?",
        Signature.toString("*"));
  }

  @Test
  public void testToStringType26() {
    assertEquals(
        "? extends Object",
        Signature.toString("+QObject;"));
  }

  @Test
  public void testToStringType27() {
    assertEquals(
        "? super InputStream",
        Signature.toString("-QInputStream;"));
  }

  /**
   * Ensures that the toString() signature of a capture with no bounds is correct
   */
  @Test
  public void testToStringType28() {
    assertEquals(
        "capture-of ?",
        Signature.toString("!*"));
  }

  /**
   * Ensures that the toString() signature of a capture with a super bound is correct
   */
  @Test
  public void testToStringType29() {
    assertEquals(
        "capture-of ? super java.util.List<T>",
        Signature.toString("!-Ljava.util.List<TT;>;"));
  }

  /**
   * Ensures that the toString() signature of a capture with an extends bound is correct
   */
  @Test
  public void testToStringType30() {
    assertEquals(
        "capture-of ? extends java.util.ArrayList",
        Signature.toString("!+Ljava.util.ArrayList;"));
  }

  /**
   * @see Signature#toString(String, String, String[], boolean, boolean)
   */
  @Test
  public void testToStringMethod01() {
    assertEquals(
        "void main(String[] args)",
        Signature.toString("([Ljava.lang.String;)V", "main", new String[]{"args"}, false, true));
  }

  @Test
  public void testToStringMethod02() {
    assertEquals(
        "main(String[] args)",
        Signature.toString("([Ljava.lang.String;)V", "main", new String[]{"args"}, false, false));
  }

  @Test
  public void testToStringMethod03() {
    assertEquals(
        "main(java.lang.String[] args)",
        Signature.toString("([Ljava.lang.String;)V", "main", new String[]{"args"}, true, false));
  }

  @Test
  public void testToStringMethod04() {
    assertEquals(
        "(java.lang.String[])",
        Signature.toString("([Ljava.lang.String;)V", null, null, true, false));
  }

  @Test
  public void testToStringMethod05() {
    assertEquals(
        "String main(String[] args)",
        Signature.toString("([Ljava.lang.String;)Ljava.lang.String;", "main", new String[]{"args"}, false, true));
  }

  @Test
  public void testToStringMethod06() {
    assertEquals(
        "java.lang.String main(java.lang.String[] args)",
        Signature.toString("([Ljava.lang.String;)Ljava.lang.String;", "main", new String[]{"args"}, true, true));
  }

  @Test
  public void testToStringMethod07() {
    assertEquals(
        "java.lang.String main(java.lang.String[] args)",
        Signature.toString("main([Ljava.lang.String;)Ljava.lang.String;", "main", new String[]{"args"}, true, true));
  }

  @Test
  public void testToStringMethod08() {
    assertEquals(
        "java.lang.String[] foo()",
        Signature.toString("()[Ljava.lang.String;", "foo", null, true, true));
  }

  @Test
  public void testToStringMethod09() {
    assertEquals(
        "I foo(C, L)",
        Signature.toString("(LC;LL;)LI;", "foo", null, true, true));
  }

  @Test
  public void testToStringMethod10() {
    assertEquals(
        "char[][] foo()",
        Signature.toString("()[[C", "foo", null, true, true));
  }

  @Test
  public void testToStringMethod11() {
    assertEquals(
        "void foo(java.lang.Object, String[][], boolean, byte, char, double, float, int, long, short)",
        Signature.toString("(Ljava.lang.Object;[[QString;ZBCDFIJS)V", "foo", null, true, true));
  }

  @Test
  public void testToStringMethod12() {
    try {
      Signature.toString("([Ljava.lang.String;V", null, null, true, false);
    }
    catch (IllegalArgumentException iae) {
      return;
    }
    assertTrue("Should get an exception", false);
  }

  /**
   * Test the toString() signature of an inner type.
   */
  @Test
  public void testToStringInnerType() {
    assertEquals(
        "Signature#toString is not correct",
        "x.y.A.Inner",
        Signature.toString("Lx.y.A$Inner;"));
  }

  /**
   * Ensures that the toString() signature of a member type with a number in its name is correct (regression test for
   * bug 180713 Anonymous type rendered as number in hover)
   */
  @Test
  public void testToStringInnerType2() {
    assertEquals(
        "Signature#toString is not correct",
        "X.Y1",
        Signature.toString("LX$Y1;"));
  }

  /**
   * @see Signature#getTypeSignatureKind(String)
   */
  @Test
  public void testGetTypeSignatureKind01() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 1",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("Ljava.lang.String;"));
  }

  @Test
  public void testGetTypeSignatureKind02() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 2",
        ISignatureConstants.ARRAY_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("[Ljava.lang.String;"));
  }

  @Test
  public void testGetTypeSignatureKind03() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 3",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QString;"));
  }

  @Test
  public void testGetTypeSignatureKind04() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 4",
        ISignatureConstants.ARRAY_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("[[QString;"));
  }

  @Test
  public void testGetTypeSignatureKind05() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 5",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("Z"));
  }

  @Test
  public void testGetTypeSignatureKind06() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 6",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("B"));
  }

  @Test
  public void testGetTypeSignatureKind07() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 7",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("C"));
  }

  @Test
  public void testGetTypeSignatureKind08() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 8",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("D"));
  }

  @Test
  public void testGetTypeSignatureKind09() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 9",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("F"));
  }

  @Test
  public void testGetTypeSignatureKind10() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 10",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("I"));
  }

  @Test
  public void testGetTypeSignatureKind11() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 11",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("J"));
  }

  @Test
  public void testGetTypeSignatureKind12() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 12",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("S"));
  }

  @Test
  public void testGetTypeSignatureKind13() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 13",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("V"));
  }

  @Test
  public void testGetTypeSignatureKind14() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 14",
        ISignatureConstants.ARRAY_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("[[[I"));
  }

// signatures with 1.5 elements
  @Test
  public void testGetTypeSignatureKind15() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 15",
        ISignatureConstants.TYPE_VARIABLE_SIGNATURE,
        Signature.getTypeSignatureKind("TVAR;"));
  }

  @Test
  public void testGetTypeSignatureKind16() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 16",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QA<QB;>;"));
  }

  @Test
  public void testGetTypeSignatureKind17() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 17",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QA<*>;"));
  }

  @Test
  public void testGetTypeSignatureKind18() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 18",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QA<+QB;>;"));
  }

  @Test
  public void testGetTypeSignatureKind19() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 19",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QA<-QB;>;"));
  }

  @Test
  public void testGetTypeSignatureKind20() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 20",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("LA<*****>;"));
  }

  @Test
  public void testGetTypeSignatureKind21() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 21",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("La<TV;>.b<QW;>.c<LX;>;"));
  }

  @Test
  public void testGetTypeSignatureKind22() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 22",
        ISignatureConstants.WILDCARD_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("*"));
  }

  @Test
  public void testGetTypeSignatureKind23() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 23",
        ISignatureConstants.WILDCARD_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("-Ljava.lang.Object;"));
  }

  @Test
  public void testGetTypeSignatureKind24() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 24",
        ISignatureConstants.WILDCARD_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("+Ljava.lang.Object;"));
  }

  /**
   * Generic type signature (regression test for bug 97273 Illegal argument exception in Signature#getTypeSignatureKind)
   */
  @Test
  public void testGetTypeSignatureKind25() {
    assertEquals(
        "Signature#getTypeSignatureKind(String) is not correct 25",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("<T:>Ljava.lang.Class;"));
  }

  /**
   * @see Signature#getTypeSignatureKind(char[])
   */
  @Test
  public void testGetTypeSignatureKindCharArray01() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 1",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("Ljava.lang.String;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray02() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 2",
        ISignatureConstants.ARRAY_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("[Ljava.lang.String;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray03() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 3",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QString;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray04() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 4",
        ISignatureConstants.ARRAY_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("[[QString;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray05() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 5",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("Z".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray06() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 6",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("B".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray07() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 7",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("C".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray08() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 8",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("D".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray09() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 9",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("F".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray10() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 10",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("I".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray11() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 11",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("J".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray12() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 12",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("S".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray13() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 13",
        ISignatureConstants.BASE_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("V".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray14() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 14",
        ISignatureConstants.ARRAY_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("[[[I".toCharArray()));
  }

// signatures with 1.5 elements
  @Test
  public void testGetTypeSignatureKindCharArray15() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 15",
        ISignatureConstants.TYPE_VARIABLE_SIGNATURE,
        Signature.getTypeSignatureKind("TVAR;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray16() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 16",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QA<QB;>;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray17() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 17",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QA<*>;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray18() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 18",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QA<+QB;>;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray19() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 19",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("QA<-QB;>;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray20() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 20",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("LA<*****>;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray21() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 21",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("La<TV;>.b<QW;>.c<LX;>;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray22() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 22",
        ISignatureConstants.WILDCARD_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("*".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray23() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 23",
        ISignatureConstants.WILDCARD_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("-Ljava.lang.Object;".toCharArray()));
  }

  @Test
  public void testGetTypeSignatureKindCharArray24() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 24",
        ISignatureConstants.WILDCARD_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("+Ljava.lang.Object;".toCharArray()));
  }

  /**
   * Generic type signature (regression test for bug 97273 Illegal argument exception in Signature#getTypeSignatureKind)
   */
  @Test
  public void testGetTypeSignatureKindCharArray25() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 25",
        ISignatureConstants.CLASS_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("<T:>Ljava.lang.Class;".toCharArray()));
  }

  /**
   * Intersection type signature
   */
  @Test
  public void testGetTypeSignatureKindCharArray26() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 26",
        ISignatureConstants.INTERSECTION_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("|Ljava.lang.Class;:Ljava.io.Serializable;".toCharArray()));
  }

  /**
   * Intersection type signature
   */
  @Test
  public void testGetTypeSignatureKindCharArray27() {
    assertEquals(
        "Signature#getTypeSignatureKind(char[]) is not correct 27",
        ISignatureConstants.INTERSECTION_TYPE_SIGNATURE,
        Signature.getTypeSignatureKind("|Ljava.lang.Class;:Ljava.io.Serializable;"));
  }

  @Test
  public void testGetTypeFragment01() {
    assertEquals(
        "C.D.E",
        Signature.getSignatureSimpleName("La.b.C$D$E;"));
  }

  @Test
  public void testGetTypeFragment02() {
    assertEquals(
        "C.D.E",
        Signature.getSignatureSimpleName("LC$D$E;"));
  }

  @Test
  public void testGetTypeFragment03() {
    assertEquals(
        "C<X>.D.E",
        Signature.getSignatureSimpleName("La.b.C<LX;>.D$E;"));
  }

  @Test
  public void testGetPackageFragment01() {
    assertEquals(
        "a.b",
        Signature.getSignatureQualifier("La.b.C$D$E;"));
  }

  @Test
  public void testGetPackageFragment02() {
    assertEquals(
        "",
        Signature.getSignatureQualifier("LC$D$E;"));
  }

  @Test
  public void testGetPackageFragment03() {
    assertEquals(
        "a.b",
        Signature.getSignatureQualifier("La.b.C<LX;>.D$E;"));
  }

  @Test
  public void testGetPackageFragment04() {
    assertEquals(
        "",
        Signature.getSignatureQualifier("LC<LX;>.D$E;"));
  }

  /**
   * Intersection type signature
   */
  @Test
  public void testGetIntersectionTypeBounds() {
    assertStringsEqual(
        "Unexpected intersection type bounds",
        "Ljava.lang.Class;\n" +
            "Ljava.io.Serializable;\n",
        Signature.getIntersectionTypeBounds("|Ljava.lang.Class;:Ljava.io.Serializable;"));
  }

  /**
   * Intersection type signature
   */
  @Test
  public void testGetIntersectionTypeBounds2() {
    assertStringsEqual(
        "Unexpected intersection type bounds",
        "QClass;\n" +
            "QSerializable;\n",
        Signature.getIntersectionTypeBounds("|QClass;:QSerializable;"));
  }

  @Test
  public void testCreateIntersectionTypeSignature() {
    String signature = Signature.createIntersectionTypeSignature(new String[]{
        "Ljava.lang.Class;",
        "Ljava.io.Serializable;"
    });
    assertStringsEqual(
        "Unexpected intersection type bounds",
        "Ljava.lang.Class;\n" +
            "Ljava.io.Serializable;\n",
        Signature.getIntersectionTypeBounds(signature));
  }

  @Test
  public void testCreateIntersectionTypeSignature2() {
    String signature = Signature.createIntersectionTypeSignature(new String[]{
        "QClass;",
        "QSerializable;"
    });
    assertStringsEqual(
        "Unexpected intersection type bounds",
        "QClass;\n" +
            "QSerializable;\n",
        Signature.getIntersectionTypeBounds(signature));
  }

  private static void assertStringsEqual(String message, String expected, String[] strings) {
    String actual = toString(strings, true/*add extra new lines*/);
    assertEquals(message, expected, actual);
  }

  private static String toString(String[] strings, boolean addExtraNewLine) {
    if (strings == null) {
      return "null";
    }
    StringBuffer buffer = new StringBuffer();
    for (int i = 0, length = strings.length; i < length; i++) {
      buffer.append(strings[i]);
      if (addExtraNewLine || i < length - 1) {
        buffer.append("\n");
      }
    }
    return buffer.toString();
  }

  /**
   * Ensures that creating an invalid type signature throws an IllegalArgumentException or return the expected
   * signature.
   */
  private static void assertInvalidTypeSignature(String typeName, boolean isResolved, String expected) {
    String actual;
    try {
      actual = Signature.createTypeSignature(typeName, isResolved);
    }
    catch (IllegalArgumentException e) {
      return;
    }
    assertEquals(expected, actual);
  }
}
