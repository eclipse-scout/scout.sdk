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
import java.security.GeneralSecurityException;

import org.eclipse.scout.sdk.core.s.util.MavenCliRunner;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.junit.Test;

/**
 * <h3>{@link ScoutProjectNewTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ScoutProjectNewTest {
  private static final String PROJECT_GROUP_ID = "group";
  private static final String PROJECT_ARTIFACT_ID = "artifact";

  @Test
  public void testProjectCreation() throws IOException, GeneralSecurityException {
    File targetDirectory = Files.createTempDirectory(ScoutProjectNewTest.class.getSimpleName() + "-projectDir").toFile();
    try {
      ScoutProjectNewHelper.createProject(targetDirectory, PROJECT_GROUP_ID, PROJECT_ARTIFACT_ID, "Display Name", getJavaVersion());
      File pomDir = new File(targetDirectory, PROJECT_ARTIFACT_ID);
      new MavenCliRunner().execute(pomDir, new String[]{"clean", "test", "-B", "-X", "-Dmaven.ext.class.path=''", "-Dmaster_test_forkCount=1", "-Dmaster_test_runOrder=filesystem"}, null, null);
    }
    finally {
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
