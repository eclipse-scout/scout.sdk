package org.eclipse.scout.sdk.doc;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IClasspathEntry;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.RunningJavaEnvironmentFactory;

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
