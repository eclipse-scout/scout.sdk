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
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.model.api.IMethodParameter;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.log.SdkLog;

@SuppressWarnings("MethodMayBeStatic")
public class PrintMethodParamTypes {

  public static void main(String[] args) {
    new PrintMethodParamTypes().printMethodParamTypes();
  }

  // tag::printMethodParamTypes[]
  public void printMethodParamTypes() {
    new EmptyJavaEnvironmentFactory().accept(this::printMethodParamTypes); // <1>
  }

  public void printMethodParamTypes(IJavaEnvironment javaEnvironment) {
    var methodName = "getChars";
    var argTypeNames = javaEnvironment
        .requireType(String.class.getName())
        .methods()
        .withName(methodName) // <2>
        .withFlags(Flags.AccPublic) // <3>
        .first().orElseThrow(() -> newFail("Cannot find method '{}' in {}.",
            methodName, String.class))
        .parameters().stream() // <4>
        .map(IMethodParameter::dataType) // <5>
        .map(IType::reference) // <6>
        .collect(joining(", "));
    SdkLog.warning(argTypeNames);
  }
  // end::printMethodParamTypes[]
}
