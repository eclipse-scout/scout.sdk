/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link SourceRangeTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class SourceRangeTest {
  @Test
  public void testSourceRange(IJavaEnvironment env) {
    IType baseClassType = env.requireType(ChildClass.class.getName()).requireSuperClass();
    IType childClassType = env.requireType(ChildClass.class.getName());

    assertFalse(baseClassType.containingPackage().source().isPresent());
    assertTrue(baseClassType.source().isPresent());

    ISourceRange source = childClassType.source().get();
    assertTrue(source.start() > childClassType.requireCompilationUnit().source().get().start());
    assertTrue(source.end() < childClassType.requireCompilationUnit().source().get().end());
    assertTrue(source.end() < childClassType.requireCompilationUnit().source().get().end());
    assertTrue(source.asCharSequence().toString().contains("ChildClass<X extends AbstractList<String> & Runnable & Serializable> extends BaseClass<X, Long> implements InterfaceLevel0 {"));
    assertTrue(source.length() < childClassType.requireCompilationUnit().source().get().length());
  }
}
