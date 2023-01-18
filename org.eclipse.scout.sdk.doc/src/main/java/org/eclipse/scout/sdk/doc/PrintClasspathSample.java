/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.doc;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.RunningJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.log.SdkLog;

@SuppressWarnings("MethodMayBeStatic")
public class PrintClasspathSample {

  public static void main(String[] args) {
    new PrintClasspathSample().printRunningClasspath();
  }

  // tag::printRunningClassPath[]
  public void printRunningClasspath() {
    new RunningJavaEnvironmentFactory().accept(this::printRunningClasspath); // <1>
  }

  public void printRunningClasspath(IJavaEnvironment javaEnvironment) {
    var cp = javaEnvironment
        .classpath() // <2>
        .map(IClasspathEntry::path)
        .map(Path::toString)
        .collect(joining(File.pathSeparator));
    SdkLog.warning("classpath={}", cp); // <3>
  }
  // end::printRunningClassPath[]
}
