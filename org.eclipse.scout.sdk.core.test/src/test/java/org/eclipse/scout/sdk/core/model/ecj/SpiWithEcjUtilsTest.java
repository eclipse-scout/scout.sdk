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
package org.eclipse.scout.sdk.core.model.ecj;

import static org.eclipse.scout.sdk.core.model.ecj.SpiWithEcjUtils.qualifiedNameOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

public class SpiWithEcjUtilsTest {
  @Test
  public void testQualifiedNameOf() {
    var className = "MyClass";
    var innerClassName = ".Inner.Inner";
    var expectedInnerClassName = innerClassName.replace(JavaTypes.C_DOT, JavaTypes.C_DOLLAR);
    assertEquals(className, qualifiedNameOf("".toCharArray(), className.toCharArray()));
    assertEquals(className + expectedInnerClassName, qualifiedNameOf("".toCharArray(), (className + innerClassName).toCharArray()));

    var packageName = "test.pck";
    assertEquals(packageName + "." + className, qualifiedNameOf(packageName.toCharArray(), className.toCharArray()));
    assertEquals(packageName + "." + className + expectedInnerClassName, qualifiedNameOf(packageName.toCharArray(), (className + innerClassName).toCharArray()));
  }
}
