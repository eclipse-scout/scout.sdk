/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.maven.cli.MavenCli;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link ScoutProjectNewTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ScoutProjectNewTest {
  private static final String PROJECT_SYMBOLIC_NAME = "sdk.test.app";

  @Test
  public void testProjectCreation() throws IOException {
    File targetDirectory = Files.createTempDirectory(ScoutProjectNewTest.class.getSimpleName() + "-projectDir").toFile();
    String oldMultiModuleProjectDir = System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
    try {
      ScoutProjectNewHelper.createProject(targetDirectory, PROJECT_SYMBOLIC_NAME, "test", getJavaVersion());
      File pomDir = new File(targetDirectory, PROJECT_SYMBOLIC_NAME + ".parent");
      System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, targetDirectory.getAbsolutePath());

      MavenCli cli = new MavenCli();
      int result = cli.doMain(new String[]{"clean", "test", "-X", "-Dmaven.ext.class.path=''"}, pomDir.getAbsolutePath(), System.out, System.err);
      Assert.assertEquals("ERROR: Created Scout project does not compile. See former log entries for details.", 0, result);
    }
    finally {
      // cleanup
      if (oldMultiModuleProjectDir == null) {
        System.clearProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
      }
      else {
        System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, oldMultiModuleProjectDir);
      }
      CoreUtils.deleteFolder(targetDirectory);
    }
  }

  private static String getJavaVersion() {
    String version = System.getProperty("java.version");
    int pos = version.indexOf('.');
    pos = version.indexOf('.', pos + 1);
    return version.substring(0, pos);
  }
}
