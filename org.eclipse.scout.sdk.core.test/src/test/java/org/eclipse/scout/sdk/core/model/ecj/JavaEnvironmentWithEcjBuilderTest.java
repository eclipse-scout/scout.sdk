/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model.ecj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;

import org.eclipse.scout.sdk.core.model.spi.ClasspathSpi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * <h3>{@link JavaEnvironmentWithEcjBuilderTest}</h3>
 *
 * @since 7.0.0
 */
public class JavaEnvironmentWithEcjBuilderTest {
  @Test
  public void testBucketOf() throws IOException {
    assertEquals(0, JavaEnvironmentWithEcjBuilder.bucketOf(createClasspathEntryMock(true, true)).intValue());
    assertEquals(1, JavaEnvironmentWithEcjBuilder.bucketOf(createClasspathEntryMock(true, false)).intValue());
    assertEquals(2, JavaEnvironmentWithEcjBuilder.bucketOf(createClasspathEntryMock(false, true)).intValue());
    assertEquals(3, JavaEnvironmentWithEcjBuilder.bucketOf(createClasspathEntryMock(false, false)).intValue());
  }

  @SuppressWarnings("resource")
  private static ClasspathEntry createClasspathEntryMock(boolean isSource, boolean isDirectory) throws IOException {
    BasicFileAttributes attributes = mock(BasicFileAttributes.class);
    when(attributes.isDirectory()).thenReturn(isDirectory);

    FileSystemProvider fsp = mock(FileSystemProvider.class);
    when(fsp.readAttributes(any(), ArgumentMatchers.<Class<BasicFileAttributes>> any(), any())).thenReturn(attributes);

    FileSystem fs = mock(FileSystem.class);
    when(fs.provider()).thenReturn(fsp);

    Path path = mock(Path.class);
    when(path.getFileSystem()).thenReturn(fs);

    ClasspathEntry mock = mock(ClasspathEntry.class);
    when(mock.mode()).thenReturn(isSource ? ClasspathSpi.MODE_SOURCE : ClasspathSpi.MODE_BINARY);
    when(mock.path()).thenReturn(path);
    return mock;
  }
}
