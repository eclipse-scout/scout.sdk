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
package org.eclipse.scout.sdk.core.testing;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.parser.JavaParser;
import org.eclipse.scout.sdk.core.util.SdkException;

/**
 *
 */
public final class TestingUtils {

  private TestingUtils() {
  }

  public static List<File> getRunningClasspath() {
    return getRunningClasspath(new String[]{});
  }

  public static List<File> getRunningClasspath(String... sourceFolders) {
    List<File> cp = new ArrayList<>();

    // bootstrap: locate rt.jar only for now
    String javaHome = System.getProperty("java.home");
    if (StringUtils.isNotBlank(javaHome)) {
      File javaLocation = new File(javaHome);
      if (javaLocation.isDirectory()) {
        File rtJar = new File(javaLocation, "lib/rt.jar");
        if (rtJar.isFile()) {
          cp.add(rtJar);
        }
      }
    }

    if (sourceFolders != null && sourceFolders.length > 0) {
      File curDir = new File("").getAbsoluteFile();
      for (String s : sourceFolders) {
        File f = new File(curDir, s);
        if (f.exists()) {
          cp.add(f);
        }
      }
    }

    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader instanceof URLClassLoader) {
      URL[] urls = ((URLClassLoader) contextClassLoader).getURLs();
      if (urls != null) {
        for (URL u : urls) {
          try {
            File f = new File(u.toURI());
            if (f.exists()) {
              cp.add(f);
            }
          }
          catch (URISyntaxException e) {
            throw new SdkException("Invalid URI: '" + u.toString() + "'.", e);
          }
        }
      }
    }
    return cp;
  }

  public static IType getType(String fqn, String... sourceFolders) {
    return JavaParser.create(getRunningClasspath(sourceFolders), false).findType(fqn);
  }
}
