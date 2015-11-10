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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.cli.MavenCli;
import org.eclipse.scout.sdk.core.util.SdkLog;

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

  protected static String[] getMavenArgs(String[] in, String globalSettings, String settings) {
    List<String> args = new ArrayList<>(in.length + 2);
    for (String s : in) {
      args.add(s);
    }
    if (StringUtils.isNotBlank(globalSettings)) {
      overwriteIfExisting(args, "-gs=", globalSettings);
    }
    if (StringUtils.isNotBlank(settings)) {
      overwriteIfExisting(args, "-s=", settings);
    }

    return args.toArray(new String[args.size()]);
  }

  protected static void overwriteIfExisting(Collection<String> args, String prefix, String value) {
    String existing = getEntryStartingWith(args, prefix);
    if (existing != null) {
      args.remove(existing);
    }
    args.add(prefix + value);
  }

  protected static String getEntryStartingWith(Iterable<String> c, String prefix) {
    for (String s : c) {
      if (s != null && s.startsWith(prefix)) {
        return s;
      }
    }
    return null;
  }

  public void execute(File workingDirectory, String[] args, String globalSettings, String settings) throws IOException {
    String oldMultiModuleProjectDir = System.getProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY);
    ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      System.setProperty(MavenCli.MULTIMODULE_PROJECT_DIRECTORY, workingDirectory.getAbsolutePath());
      String charset = StandardCharsets.UTF_16.name();
      try (ByteArrayOutputStream bOut = new ByteArrayOutputStream(); PrintStream out = new PrintStream(bOut, true, charset); ByteArrayOutputStream bErr = new ByteArrayOutputStream(); PrintStream err = new PrintStream(bErr, true, charset)) {

        String[] mavenArgs = getMavenArgs(args, globalSettings, settings);
        SdkLog.debug("Executing embedded maven with arguments:  " + Arrays.toString(mavenArgs));

        MavenCli cli = new MavenCli();
        int result = cli.doMain(mavenArgs, workingDirectory.getAbsolutePath(), out, err);

        m_stdOut = bOut.toString(charset);
        m_errOut = bErr.toString(charset);

        if (StringUtils.isNotEmpty(m_stdOut)) {
          SdkLog.debug(m_stdOut);
        }
        if (StringUtils.isNotEmpty(m_errOut)) {
          SdkLog.debug(m_errOut);
        }

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
