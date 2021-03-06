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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
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
