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
package org.eclipse.scout.sdk.core.model.api;

import static org.eclipse.scout.sdk.core.model.api.Flags.isEnum;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.RoundingMode;
import java.util.Map.Entry;

import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link ArrayTypeTest}</h3> Various Tests for {@link IType}s which are arrays
 *
 * @since 5.2.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class ArrayTypeTest {
  @Test
  public void testArrayTypes(IJavaEnvironment env) {
    var intArr = env.requireType("int[]");
    assertEquals(1, intArr.arrayDimension());

    var stringArr = env.requireType(String.class.getName() + "[][][]");
    assertEquals(3, stringArr.arrayDimension());

    var entryArr = env.requireType(Entry.class.getName() + "[][][][]");
    assertEquals(4, entryArr.arrayDimension());

    env.reload();
    assertEquals(4, entryArr.arrayDimension());

    var roundingModeArr = env.requireType(RoundingMode.class.getName() + "[]");
    assertEquals(1, roundingModeArr.arrayDimension());
    assertFalse(roundingModeArr.leafComponentType().get().isArray());
    assertTrue(isEnum(roundingModeArr.leafComponentType().get().flags()));
  }
}
