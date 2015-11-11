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
