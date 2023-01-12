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
