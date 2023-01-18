/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.testing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.opentest4j.AssertionFailedError;

public final class SdkAssertions {

  private SdkAssertions() {
  }

  /**
   * Asserts that the given actual content equals the content of the given file path.
   *
   * @param fileWithExpectedContent
   *          The absolute path on the java classpath where the reference file can be found.
   * @param actualContent
   *          The actual content to compare against.
   */
  public static void assertEqualsRefFile(String fileWithExpectedContent, CharSequence actualContent) {
    CharSequence refSrc;
    try (var in = SdkAssertions.class.getClassLoader().getResourceAsStream(Ensure.notNull(fileWithExpectedContent))) {
      refSrc = Strings.fromInputStream(Ensure.notNull(in, "File '{}' could not be found on classpath.", fileWithExpectedContent), StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new SdkException(e);
    }
    var expected = CoreTestingUtils.normalizeNewLines(refSrc);
    var actual = CoreTestingUtils.normalizeNewLines(actualContent);
    if (!Strings.equals(expected, actual)) {
      throw new AssertionFailedError(null, expected, actual);
    }
  }
}
