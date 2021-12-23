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
package org.eclipse.scout.sdk.core.model.api;

import static org.eclipse.scout.sdk.core.model.api.Flags.isEnum;
import static org.eclipse.scout.sdk.core.util.JavaTypes.arrayMarker;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.RoundingMode;
import java.util.Map.Entry;

import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ArrayTypeTest}</h3> Various Tests for {@link IType}s which are arrays
 *
 * @since 5.2.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class ArrayTypeTest {
  @Test
  public void testArrayTypes(IJavaEnvironment env) {
    var intArr = env.requireType("int" + arrayMarker());
    assertEquals(1, intArr.arrayDimension());

    var stringArr = env.requireType(String.class.getName() + arrayMarker(3));
    assertEquals(3, stringArr.arrayDimension());

    var entryArr = env.requireType(Entry.class.getName() + arrayMarker(4));
    assertEquals(4, entryArr.arrayDimension());

    env.reload();
    assertEquals(4, entryArr.arrayDimension());

    var roundingModeArr = env.requireType(RoundingMode.class.getName() + arrayMarker());
    assertEquals(1, roundingModeArr.arrayDimension());
    assertFalse(roundingModeArr.leafComponentType().orElseThrow().isArray());
    assertTrue(isEnum(roundingModeArr.leafComponentType().orElseThrow().flags()));
  }
}
