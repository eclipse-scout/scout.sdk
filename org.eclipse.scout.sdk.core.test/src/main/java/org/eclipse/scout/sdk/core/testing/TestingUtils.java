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

  private static void collectBootstrapClassPath(List<File> collector) {
    try {
      Class<?> launcher = Class.forName("sun.misc.Launcher");
      Object urlClassPath = launcher.getMethod("getBootstrapClassPath").invoke(null);
      URL[] urls = (URL[]) urlClassPath.getClass().getMethod("getURLs").invoke(urlClassPath);
      for (URL url : urls) {
        File f = new File(url.toURI());
        if (f.exists()) {
          collector.add(f);
        }
      }
    }
    catch (Exception e) {
      System.out.println("Unable to read running bootstrap classpath. Fallback to minimal bootstrap classpath. Nested exception: ");
      e.printStackTrace(System.out);

      String javaHome = System.getProperty("java.home");
      if (StringUtils.isNotBlank(javaHome)) {
        File javaLocation = new File(javaHome);
        if (javaLocation.isDirectory()) {
          File rtJar = new File(javaLocation, "lib/rt.jar");
          if (rtJar.isFile()) {
            collector.add(rtJar);
          }
        }
      }
    }
  }

  private static void collectSourceFolders(List<File> collector, String... sourceFolders) {
    if (sourceFolders != null && sourceFolders.length > 0) {
      File curDir = new File("").getAbsoluteFile();
      for (String s : sourceFolders) {
        File f = new File(curDir, s);
        if (f.exists()) {
          collector.add(f);
        }
      }
    }
  }

  private static void collectRunningClassPath(List<File> collector) {
    String javaClassPathRaw = System.getProperty("java.class.path");
    if (javaClassPathRaw != null && !javaClassPathRaw.isEmpty()) {
      String separator = System.getProperty("path.separator");
      String[] elements = javaClassPathRaw.split(separator);
      for (String cpElement : elements) {
        File f = new File(cpElement);
        if (f.exists()) {
          collector.add(f);
        }
      }
    }
  }

  private static void collectCurrentClassLoaderUrls(List<File> collector) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader instanceof URLClassLoader) {
      URL[] urls = ((URLClassLoader) contextClassLoader).getURLs();
      if (urls != null) {
        for (URL u : urls) {
          try {
            File f = new File(u.toURI());
            if (f.exists()) {
              collector.add(f);
            }
          }
          catch (URISyntaxException e) {
            throw new SdkException("Invalid URI: '" + u.toString() + "'.", e);
          }
        }
      }
    }
  }

  public static List<File> getRunningClasspath(String... sourceFolders) {
    List<File> cp = new ArrayList<>();
    collectBootstrapClassPath(cp);
    collectSourceFolders(cp, sourceFolders);
    collectRunningClassPath(cp);
    collectCurrentClassLoaderUrls(cp);
    return cp;
  }

  public static IType getType(String fqn, String... sourceFolders) {
    return JavaParser.create(getRunningClasspath(sourceFolders), false).findType(fqn);
  }
}
