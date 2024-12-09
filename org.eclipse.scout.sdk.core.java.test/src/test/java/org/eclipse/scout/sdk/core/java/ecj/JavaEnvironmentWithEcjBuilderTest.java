/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.ecj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.eclipse.scout.sdk.core.java.model.spi.ClasspathSpi;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JavaEnvironmentWithEcjBuilderTest}</h3>
 *
 * @since 7.0.0
 */
public class JavaEnvironmentWithEcjBuilderTest {
  @Test
  public void testBucketOf() {
    assertEquals(0, JavaEnvironmentWithEcjBuilder.bucketOf(createClasspathEntryMock(true), p -> true).intValue());
    assertEquals(1, JavaEnvironmentWithEcjBuilder.bucketOf(createClasspathEntryMock(true), p -> false).intValue());
    assertEquals(2, JavaEnvironmentWithEcjBuilder.bucketOf(createClasspathEntryMock(false), p -> true).intValue());
    assertEquals(3, JavaEnvironmentWithEcjBuilder.bucketOf(createClasspathEntryMock(false), p -> false).intValue());
  }

  private static ClasspathEntry createClasspathEntryMock(boolean isSource) {
    var path = mock(Path.class);
    var mock = mock(ClasspathEntry.class);
    when(mock.mode()).thenReturn(isSource ? ClasspathSpi.MODE_SOURCE : ClasspathSpi.MODE_BINARY);
    when(mock.path()).thenReturn(path);
    return mock;
  }
}
