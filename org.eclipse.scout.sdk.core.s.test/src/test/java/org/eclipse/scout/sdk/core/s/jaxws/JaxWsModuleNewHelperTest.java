/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.jaxws;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
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
    Path dir = Files.createTempDirectory("parentPomTest");
    try {
      Path pom = dir.resolve(MODULE_FOLDER + '/' + IMavenConstants.POM);
      Files.createDirectories(pom.getParent());
      Files.write(pom, xml.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      Path result = JaxWsModuleNewHelper.getParentPomOf(pom);
      if (result == null) {
        if (expectedPath == null) {
          return; // ok
        }
        throw new SdkException("Parent Pom is null but expected '{}'.", expectedPath);
      }

      Path relPath = dir.relativize(result);
      assertEquals(expectedPath, Strings.replace(relPath.toString(), relPath.getFileSystem().getSeparator(), "/"));
    }
    finally {
      CoreUtils.deleteDirectory(dir);
    }
  }
}
