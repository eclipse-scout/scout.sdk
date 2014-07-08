/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.testing.compare;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.testing.compare.internal.LineCompareResult;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.osgi.framework.Bundle;

/**
 * <h3>{@link CompareUtility}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.9.0 15.03.2013
 */
public final class CompareUtility {
  private CompareUtility() {
  }

  public static ICompareResult<String> compareSource(ICompilationUnit icu01, ICompilationUnit icu02, boolean stopWithFirstDiff) throws JavaModelException, IOException {
    if (!TypeUtility.exists(icu01)) {
      throw new IllegalArgumentException("Compilation unit 1: " + ((icu01 == null) ? ("is null!") : ("'" + icu01.getElementName() + "' does not exist!")));
    }
    if (!TypeUtility.exists(icu02)) {
      throw new IllegalArgumentException("Compilation unit 2: " + ((icu02 == null) ? ("is null!") : ("'" + icu02.getElementName() + "' does not exist!")));
    }
    String source01 = icu01.getSource();
    if (StringUtility.isNullOrEmpty(source01)) {
      throw new IllegalArgumentException("No source for compilation unit: '" + icu01.getElementName() + "'");
    }
    String source02 = icu02.getSource();
    if (StringUtility.isNullOrEmpty(source02)) {
      throw new IllegalArgumentException("No source for compilation unit: '" + icu02.getElementName() + "'");
    }
    return compareLines(new ByteArrayInputStream(icu01.getSource().getBytes()), new ByteArrayInputStream(icu02.getSource().getBytes()), stopWithFirstDiff);
  }

  public static ICompareResult<String> compareSource(ISourceReference sourceRef, Bundle bundle, String resourcePath, boolean stopWithFirstDiff) throws JavaModelException, IOException {
    if (sourceRef == null || !sourceRef.exists()) {
      throw new IllegalArgumentException("Source reference does not exist!");
    }
    String source01 = sourceRef.getSource();
    if (StringUtility.isNullOrEmpty(source01)) {
      throw new IllegalArgumentException("Source reference does not contain any source!");
    }
    return compareLines(new ByteArrayInputStream(sourceRef.getSource().getBytes()), getInputStream(bundle, resourcePath), stopWithFirstDiff);
  }

  public static ICompareResult<String> compareSource(ISourceReference sourceRef, InputStream stream02, boolean stopWithFirstDiff) throws JavaModelException, IOException {
    if (sourceRef == null || !sourceRef.exists()) {
      throw new IllegalArgumentException("Source reference does not exist!");
    }
    String source01 = sourceRef.getSource();
    if (StringUtility.isNullOrEmpty(source01)) {
      throw new IllegalArgumentException("Source reference does not contain any source!");
    }
    return compareLines(new ByteArrayInputStream(sourceRef.getSource().getBytes()), stream02, stopWithFirstDiff);
  }

  public static ICompareResult<String> compareLines(InputStream stream01, InputStream stream02, boolean stopWithFirstDiff) throws IOException {
    BufferedReader readerA = null;
    BufferedReader readerB = null;
    try {
      int lineNr = 1;
      LineCompareResult compareResult = new LineCompareResult();
      readerA = new BufferedReader(new InputStreamReader(stream01));
      readerB = new BufferedReader(new InputStreamReader(stream02));
      String lineA = readerA.readLine();
      String lineB = readerB.readLine();
      while (lineA != null) {
        lineA = replaceUserName(lineA);
        lineB = replaceUserName(lineB);
        if (lineA.equals(lineB)) {
          lineA = readerA.readLine();
          lineB = readerB.readLine();
        }
        else {
          compareResult.addDifference(new LineCompareResult.LineDifference(lineNr, lineA, lineB));
          if (stopWithFirstDiff) {
            return compareResult;
          }
        }
        lineNr++;
      }
      if (lineB != null) {
        compareResult.addDifference(new LineCompareResult.LineDifference(lineNr, null, lineB));
      }

      return compareResult;
    }
    finally {
      if (readerA != null) {
        readerA.close();
      }
      if (readerB != null) {
        readerB.close();
      }
    }
  }

  private static String replaceUserName(String s) {
    return s.replaceAll("\\$\\{user.name\\}", System.getProperty("user.name"));
  }

  private static InputStream getInputStream(Bundle bundle, String pathToResource) throws IOException {
    URL resource = FileLocator.find(bundle, new Path(pathToResource), null);
    InputStream is = null;
    if (resource != null) {
      is = resource.openStream();
    }
    return is;
  }

}
