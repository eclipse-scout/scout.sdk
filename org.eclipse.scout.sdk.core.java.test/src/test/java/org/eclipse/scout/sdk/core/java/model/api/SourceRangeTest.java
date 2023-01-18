/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.model.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link SourceRangeTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class SourceRangeTest {
  @Test
  public void testSourceRange(IJavaEnvironment env) {
    var baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    var childClassType = env.requireType(ChildClass.class.getName());

    assertFalse(baseClassType.containingPackage().source().isPresent());
    assertTrue(baseClassType.source().isPresent());

    var source = childClassType.source().orElseThrow();
    assertTrue(source.start() > childClassType.requireCompilationUnit().source().orElseThrow().start());
    assertTrue(source.end() < childClassType.requireCompilationUnit().source().orElseThrow().end());
    assertTrue(source.end() < childClassType.requireCompilationUnit().source().orElseThrow().end());
    assertTrue(source.asCharSequence().toString().contains("ChildClass<X extends AbstractList<String> & Runnable & Serializable> extends BaseClass<X, Long> implements InterfaceLevel0 {"));
    assertTrue(source.length() < childClassType.requireCompilationUnit().source().orElseThrow().length());
  }
}
