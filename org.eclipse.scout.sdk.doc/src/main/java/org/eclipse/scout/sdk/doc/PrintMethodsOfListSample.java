package org.eclipse.scout.sdk.doc;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

import java.util.List;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;

@SuppressWarnings("MethodMayBeStatic")
public class PrintMethodsOfListSample {

  public static void main(String[] args) {
    new PrintMethodsOfListSample().printMethodsOfList();
  }

  // tag::printMethodsOfList[]
  public void printMethodsOfList() {
    new JavaEnvironmentWithEcjBuilder<>() // <1>
        .withSourceFolder(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER) // <2>
        .withAbsoluteBinaryPath("/dev/libs/mylib.jar") // <3>
        .accept(this::printMethodsOfList);
  }

  public void printMethodsOfList(IJavaEnvironment javaEnvironment) {
    var methodsOfList = javaEnvironment
        .requireType(List.class.getName()) // <4>
        .methods().stream() // <5>
        .map(IMethod::identifier) // <6>
        .collect(joining(lineSeparator()));
    SdkLog.warning(methodsOfList);
  }
  // end::printMethodsOfList[]
}
