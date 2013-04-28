/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.maven.plugins.updatesite;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class StagingTest
{

  private File tempDir;

  private File sourceDirectory;

  private File targetDirectory;

  @Before
  public void setUp() throws IOException {
    URL testRepo = getClass().getResource("/repository");
    sourceDirectory = new File(testRepo.getFile());

    //TODO use temp file instead
    targetDirectory = new File("target");
    targetDirectory.mkdirs();

    tempDir = new File("target/temp");
    tempDir.mkdirs();
  }

  @After
  public void tearDown() throws IOException {
//    	FileUtility.deleteFile(tempDir);
  }

  /**
   * @throws MojoExecutionException
   */
  @Test
  public void testCreateCompositeRepo() throws MojoExecutionException {
    StagingMojo stagingMojo = new StagingMojo();
    stagingMojo.setP2InputDirectory(sourceDirectory.getPath());
    stagingMojo.setOutputDirectory(targetDirectory.getPath());
    stagingMojo.setCompositeDirName("nightly");
    stagingMojo.setUpdatesiteDir("N20120202");
    stagingMojo.createCompositeRepo();
    assertTrue(true);
  }

  @Test
  public void testCreateStageZip() throws MojoExecutionException {
    StagingMojo stagingMojo = new StagingMojo();
    stagingMojo.setOutputDirectory(targetDirectory.getPath());
    File zipFile = stagingMojo.createStageZip(sourceDirectory);
    assertTrue(zipFile.exists());
  }

  @Test
  public void testAppendChild() throws MojoExecutionException {
    StagingMojo stagingMojo = new StagingMojo();
    stagingMojo.appendChild(getTestXMLFile(), "test");
  }

  private File getTestXMLFile() {
    String pathname = getClass().getResource("/compositeContent.xml").getFile();
    return new File(pathname);
  }

  private File getTestJARFile() {
    String pathname = getClass().getResource("/compositeContent.jar").getFile();
    return new File(pathname);
  }

  @Test
  public void testUpdateComposite() throws MojoExecutionException {
    StagingMojo stagingMojo = new StagingMojo();
    stagingMojo.updateComposite(targetDirectory, getTestJARFile(), "compositeContent");
  }

}
