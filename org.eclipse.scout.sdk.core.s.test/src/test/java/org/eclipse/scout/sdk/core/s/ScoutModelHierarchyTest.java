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
package org.eclipse.scout.sdk.core.s;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ScoutModelHierarchyTest}</h3>
 *
 * @since 7.0.0
 */
public class ScoutModelHierarchyTest {
  @Test
  public void testGetPossibleChildren() {
    assertEquals(singleton(IScoutRuntimeTypes.IMenu), ScoutModelHierarchy.getPossibleChildren(IScoutRuntimeTypes.AbstractValueField));
    assertEquals(emptySet(), ScoutModelHierarchy.getPossibleChildren(JavaTypes.Float));
  }

  @Test
  public void testIsSubtypeOf() {
    assertTrue(ScoutModelHierarchy.isSubtypeOf(IScoutRuntimeTypes.ITabBox, IScoutRuntimeTypes.ICompositeField));
    assertTrue(ScoutModelHierarchy.isSubtypeOf(IScoutRuntimeTypes.ITabBox, IScoutRuntimeTypes.ITabBox));
    assertTrue(ScoutModelHierarchy.isSubtypeOf(IScoutRuntimeTypes.ITabBox, IScoutRuntimeTypes.IOrdered));

  }
}
