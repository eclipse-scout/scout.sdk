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
package org.eclipse.scout.sdk.core.s.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.cli.MavenCli;

/**
 * <h3>{@link MavenCliRunner}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class MavenCliRunner {

  private String m_stdOut;
  private String m_errOut;

  public String getErrorOutput() {
    return m_errOut;
  }

  public String getStandardOutput() {
    return m_stdOut;
  }

  public void execute(File workingDirectory, String[] args) throws IOException {
    String oldMultiModuleProjectDir = System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
    ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, workingDirectory.getAbsolutePath());
      String charset = StandardCharsets.UTF_8.name();
      try (ByteArrayOutputStream bOut = new ByteArrayOutputStream(); PrintStream out = new PrintStream(bOut, true, charset); ByteArrayOutputStream bErr = new ByteArrayOutputStream(); PrintStream err = new PrintStream(bErr, true, charset)) {
        MavenCli cli = new MavenCli();
        int result = cli.doMain(args, workingDirectory.getAbsolutePath(), out, err);
        m_stdOut = bOut.toString(charset);
        m_errOut = bErr.toString(charset);
        if (result != 0) {
          String msg = getErrorOutput();
          if (StringUtils.isEmpty(msg)) {
            msg = getStandardOutput();
          }
          throw new IOException("Maven call failed:\n" + msg);
        }
      }
    }
    finally {
      Thread.currentThread().setContextClassLoader(oldContextClassLoader);
      if (oldMultiModuleProjectDir == null) {
        System.clearProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
      }
      else {
        System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, oldMultiModuleProjectDir);
      }
    }
  }
}
