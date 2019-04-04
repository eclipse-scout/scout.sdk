/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.generator.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link SimpleWorkingCopyTransformerBuilderTest}</h3>
 *
 * @since 8.0.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class SimpleWorkingCopyTransformerBuilderTest {

  private final AtomicInteger m_counter = new AtomicInteger();

  @Test
  public void testBuilder(IJavaEnvironment env) {
    m_counter.set(0);
    IWorkingCopyTransformer transformer = new SimpleWorkingCopyTransformerBuilder()
        .withAnnotationElementMapper(this::count)
        .withAnnotationMapper(this::count)
        .withCompilationUnitMapper(this::count)
        .withFieldMapper(this::count)
        .withImportMapper(this::count)
        .withMethodMapper(this::count)
        .withMethodParameterMapper(this::count)
        .withPackageMapper(this::count)
        .withTypeMapper(this::count)
        .withTypeParameterMapper(this::count)
        .withUnresolvedTypeMapper(this::count)
        .build();
    env.requireType(ChildClass.class.getName())
        .requireCompilationUnit()
        .toWorkingCopy(transformer);
    assertEquals(25, m_counter.get());
  }

  private <A> A count(A a) {
    m_counter.incrementAndGet();
    return a;
  }
}
