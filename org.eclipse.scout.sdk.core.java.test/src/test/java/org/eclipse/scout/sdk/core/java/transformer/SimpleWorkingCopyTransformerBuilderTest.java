/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.sdk.core.java.fixture.ChildClass;
import org.eclipse.scout.sdk.core.java.model.api.IJavaElement;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.FixtureHelper.CoreJavaEnvironmentWithSourceFactory;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.transformer.IWorkingCopyTransformer.ITransformInput;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link SimpleWorkingCopyTransformerBuilderTest}</h3>
 *
 * @since 8.0.0
 */
@ExtendWithJavaEnvironmentFactory(CoreJavaEnvironmentWithSourceFactory.class)
public class SimpleWorkingCopyTransformerBuilderTest {

  private final AtomicInteger m_counter = new AtomicInteger();

  @Test
  public void testBuilder(IJavaEnvironment env) {
    m_counter.set(0);
    var transformer = new SimpleWorkingCopyTransformerBuilder()
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
    assertEquals(24, m_counter.get());
  }

  private <M extends IJavaElement, G> G count(ITransformInput<M, G> input) {
    m_counter.incrementAndGet();
    return input.requestDefaultWorkingCopy();
  }
}
