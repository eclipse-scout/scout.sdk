/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link JaxWsModuleNewHelperTest}</h3>
 *
 * @since 5.2.0
 */
public class JaxWsModuleNewHelperTest {

  private static final String MODULE_FOLDER = "module";

  @Test
  public void testGetParentPomOf() throws IOException {
    assertParentPomPath("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project></project>", IMavenConstants.POM);
    assertParentPomPath("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent></parent></project>", IMavenConstants.POM);
    assertParentPomPath("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><relativePath></relativePath></parent></project>", null);
    assertParentPomPath("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><relativePath>../myParentModule</relativePath></parent></project>", "myParentModule/pom.xml");
    assertParentPomPath("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><relativePath>../myParentModule/</relativePath></parent></project>", "myParentModule/pom.xml");
    assertParentPomPath("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project><parent><relativePath>../myParentModule</relativePath></parent></project>", "myParentModule/pom.xml");
  }

  protected static void assertParentPomPath(String xml, String expectedPath) throws IOException {
    var dir = Files.createTempDirectory("parentPomTest");
    try {
      var pom = dir.resolve(MODULE_FOLDER + '/' + IMavenConstants.POM);
      Files.createDirectories(pom.getParent());
      Files.writeString(pom, xml, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      var result = JaxWsModuleNewHelper.getParentPomOf(pom);
      if (result == null) {
        if (expectedPath == null) {
          return; // ok
        }
        throw new SdkException("Parent Pom is null but expected '{}'.", expectedPath);
      }

      var relPath = dir.relativize(result);
      assertEquals(expectedPath, relPath.toString().replace(File.separatorChar, '/'));
    }
    finally {
      CoreUtils.deleteDirectory(dir);
    }
  }
}
