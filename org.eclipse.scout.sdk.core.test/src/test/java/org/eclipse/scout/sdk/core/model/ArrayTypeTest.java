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
package org.eclipse.scout.sdk.core.model;

import java.math.RoundingMode;
import java.util.Map.Entry;

import org.eclipse.scout.sdk.core.model.api.Flags;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link ArrayTypeTest}</h3> Various Tests for {@link IType}s which are arrays
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ArrayTypeTest {
  @Test
  public void testArrayTypes() {
    IJavaEnvironment env = CoreTestingUtils.createJavaEnvironment();
    IType intArr = env.findType("int[]");
    Assert.assertNotNull(intArr);
    Assert.assertEquals(1, intArr.arrayDimension());

    IType stringArr = env.findType(String.class.getName() + "[][][]");
    Assert.assertNotNull(stringArr);
    Assert.assertEquals(3, stringArr.arrayDimension());

    IType entryArr = env.findType(Entry.class.getName() + "[][][][]");
    Assert.assertNotNull(entryArr);
    Assert.assertEquals(4, entryArr.arrayDimension());

    env.reload();
    Assert.assertEquals(4, entryArr.arrayDimension());

    IType roundingModeArr = env.findType(RoundingMode.class.getName() + "[]");
    Assert.assertNotNull(roundingModeArr);
    Assert.assertEquals(1, roundingModeArr.arrayDimension());
    Assert.assertFalse(roundingModeArr.leafComponentType().isArray());
    Assert.assertTrue(Flags.isEnum(roundingModeArr.leafComponentType().flags()));
  }
}
