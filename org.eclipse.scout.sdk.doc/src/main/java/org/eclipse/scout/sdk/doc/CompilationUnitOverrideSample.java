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

import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;

@SuppressWarnings("MethodMayBeStatic")
public class CompilationUnitOverrideSample {

  public static void main(String[] args) {
    new CompilationUnitOverrideSample().run();
  }

  public void run() {
    new EmptyJavaEnvironmentFactory().accept(this::generatorToModel);
  }

  protected void generatorToModel(IJavaEnvironment javaEnvironment) {
    // tag::generatorToModel[]
    var packageName = "org.eclipse.scout.sdk.doc";
    var className = "EmptyCloseable";

    var source = PrimaryTypeGenerator.create() // <1>
        .withPackageName(packageName)
        .asPublic()
        .withInterface(AutoCloseable.class.getName())
        .withElementName(className)
        .withAllMethodsImplemented()
        .toJavaSource(javaEnvironment);
    var requiresReload = javaEnvironment.registerCompilationUnitOverride(source,
        packageName, className + JavaTypes.JAVA_FILE_SUFFIX); // <2>
    if (requiresReload) {
      javaEnvironment.reload(); // <3>
    }

    var closeMethodSource = javaEnvironment.requireType(packageName + '.' + className)
        .methods()
        .withName("close")
        .first().orElseThrow()
        .source().orElseThrow()
        .asCharSequence().toString(); // <4>
    SdkLog.warning(closeMethodSource);
    // end::generatorToModel[]
  }
}
