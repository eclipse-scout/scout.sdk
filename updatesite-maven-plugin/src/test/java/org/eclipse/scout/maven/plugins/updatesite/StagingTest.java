/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.maven.plugins.updatesite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class StagingTest {

  private File tempDir;

  private File sourceDirectory;

  private File targetDirectory;

  @BeforeEach
  public void setUp() {
    var testRepo = getClass().getResource("/repository");
    sourceDirectory = new File(testRepo.getFile());

    targetDirectory = new File("target");
    targetDirectory.mkdirs();

    tempDir = new File("target/temp");
    tempDir.mkdirs();
  }

  @AfterEach
  public void tearDown() {
    FileUtility.deleteFile(tempDir);
  }

  /**
   * @throws MojoExecutionException
   */
  @Test
  public void testCreateCompositeRepo() throws MojoExecutionException {
    var stagingMojo = new StagingMojo();
    stagingMojo.setP2InputDirectory(sourceDirectory.getPath());
    stagingMojo.setOutputDirectory(targetDirectory.getPath());
    stagingMojo.setCompositeDirName("nightly");
    stagingMojo.setUpdatesiteDir("N20120202");
    stagingMojo.createCompositeRepo();
    assertTrue(true);
  }

  @Test
  public void testCreateSha256() throws IOException, MojoExecutionException {
    var tmp = Files.createTempFile("stagingMojoTest", null);
    try {
      Files.writeString(tmp, "testcontent", StandardCharsets.UTF_8);
      var sha = StagingMojo.createSha256(tmp.toFile());
      assertEquals("25edaa1f62bd4f2a7e4aa7088cf4c93449c1881af03434bfca027f1f82d69dba", sha);
    }
    finally {
      Files.delete(tmp);
    }
  }

  /**
   * @throws MojoExecutionException
   */
  @Test
  public void testCreateStageZip() throws MojoExecutionException {
    var stagingMojo = new StagingMojo();
    stagingMojo.setOutputDirectory(targetDirectory.getPath());
    var zipFile = stagingMojo.createStageZip(sourceDirectory, "");
    assertTrue(zipFile.exists());
  }

  /**
   * @throws MojoExecutionException
   */
  @Test
  public void testAppendChildXML() throws MojoExecutionException {
    var stagingMojo = new StagingMojo();
    stagingMojo.setOutputDirectory(targetDirectory.getPath());
    StagingMojo.appendChild(getTestXMLFile(), "test");
  }

  /**
   * @throws MojoExecutionException
   */
  @Test
  public void testAppendChildJAR() throws MojoExecutionException {
    var stagingMojo = new StagingMojo();
    stagingMojo.setOutputDirectory(targetDirectory.getPath());
    var contentXML = StagingMojo.extractCompositeArchive(stagingMojo.getStageTargetDir(), getTestJARFile());
    StagingMojo.appendChild(contentXML, "test");
  }

  private File getTestXMLFile() {
    var pathname = getClass().getResource("/compositeContent.xml").getFile();
    return new File(pathname);
  }

  private File getTestJARFile() {
    var pathname = getClass().getResource("/compositeContent.jar").getFile();
    return new File(pathname);
  }
}
