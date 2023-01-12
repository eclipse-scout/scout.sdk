/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.maven.plugins.updatesite;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractStagingMojo extends AbstractMojo {

  /**
   * The directory where the generated archive file will be put.
   */
  @Parameter(defaultValue = "${project.build.directory}")
  private String outputDirectory;

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public File getStageTargetDir() {
    return new File(getOutputDirectory(), "stageTarget");
  }
}
