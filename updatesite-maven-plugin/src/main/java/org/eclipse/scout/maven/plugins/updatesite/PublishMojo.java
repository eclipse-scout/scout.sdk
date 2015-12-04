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
package org.eclipse.scout.maven.plugins.updatesite;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "publish")
public class PublishMojo extends AbstractStagingMojo {

  @Parameter(defaultValue = "/home/data/httpd/download.eclipse.org/scout/stagingArea")
  private String stagingArea;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      File input = getStageTargetDir();
      File stagingAreaFile = new File(getStagingArea());
      if (input.isDirectory()) {
        for (File f : input.listFiles()) {
          if (f.getName().startsWith("stage")) {
            FileUtility.copyToDir(f, stagingAreaFile);
          }
        }
        for (File f : input.listFiles()) {
          if (f.getName().startsWith("do")) {
            FileUtility.copyToDir(f, stagingAreaFile);
          }
        }
      }
    }
    catch (IOException e) {
      throw new MojoExecutionException("Publishing failed ", e);
    }
  }

  /**
   * @return the stagingArea
   */
  public String getStagingArea() {
    return stagingArea;
  }

  /**
   * @param stagingArea
   *          the stagingArea to set
   */
  public void setStagingArea(String stagingArea) {
    this.stagingArea = stagingArea;
  }
}
