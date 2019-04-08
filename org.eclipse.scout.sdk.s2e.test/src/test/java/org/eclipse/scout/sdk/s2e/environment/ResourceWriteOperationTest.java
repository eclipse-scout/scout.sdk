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
package org.eclipse.scout.sdk.s2e.environment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link ResourceWriteOperationTest}</h3>
 *
 * @since 7.0.0
 */
public class ResourceWriteOperationTest {
  @Test
  public void testAreContentsEqual() {
    assertResourceEquals("", "");
    assertResourceEquals("a", "a");
    assertResourceEquals("bb", "bb");
    assertResourceEquals("ccc", "ccc");
    assertResourceEquals("dddd", "dddd");

    assertResourceNotEquals("", "a");
    assertResourceNotEquals("", "bb");
    assertResourceNotEquals("", "ccc");
    assertResourceNotEquals("", "dddd");
    assertResourceNotEquals("a", "");
    assertResourceNotEquals("bb", "");
    assertResourceNotEquals("ccc", "");
    assertResourceNotEquals("dddd", "");
    assertResourceNotEquals("a", "b");
    assertResourceNotEquals("bb", "cc");
    assertResourceNotEquals("ccc", "ddd");
    assertResourceNotEquals("dddd", "eeee");

    assertResourceNotEquals(null, "new", false);
    assertResourceNotEquals(null, null, false);
  }

  protected static void assertResourceEquals(String existingContent, CharSequence newContent) {
    assertTrue(ResourceWriteOperation.areContentsEqual(createMock(true, existingContent), newContent, 3));
  }

  protected static void assertResourceNotEquals(String existingContent, CharSequence newContent) {
    assertResourceNotEquals(existingContent, newContent, true);
  }

  protected static void assertResourceNotEquals(String existingContent, CharSequence newContent, boolean fileExists) {
    assertFalse(ResourceWriteOperation.areContentsEqual(createMock(fileExists, existingContent), newContent, 3));
  }

  protected static IFile createMock(boolean exists, String fileContent) {
    IFile file = mock(IFile.class);
    when(file.exists()).thenReturn(exists);
    try {
      if (exists) {
        when(file.getContents()).thenReturn(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)));
      }
      when(file.getCharset()).thenReturn(StandardCharsets.UTF_8.name());
    }
    catch (CoreException e) {
      throw new SdkException(e);
    }
    return file;
  }
}