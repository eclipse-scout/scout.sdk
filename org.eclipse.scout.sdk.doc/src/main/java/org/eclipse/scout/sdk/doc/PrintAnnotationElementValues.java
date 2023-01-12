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

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import javax.annotation.Generated;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.annotation.GeneratedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;

@SuppressWarnings({"MethodMayBeStatic"})
public class PrintAnnotationElementValues {

  public static void main(String[] args) {
    new PrintAnnotationElementValues().printAnnotationElementValues();
  }

  public void printAnnotationElementValues() {
    new JavaEnvironmentWithEcjBuilder<>()
        .withSourceFolder(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        .accept(this::printAnnotationElementValues);
  }

  // tag::printAnnotationElementValues[]
  @Generated(value = "org.eclipse.scout.sdk.doc.Generator", comments = "For demonstration only :)")
  public static class GeneratedClass {
  }

  public void printAnnotationElementValues(IJavaEnvironment javaEnvironment) {
    var generatedAnnotation = javaEnvironment
        .requireType(GeneratedClass.class.getName())
        .annotations()
        .withManagedWrapper(GeneratedAnnotation.class) // <1>
        .first().orElseThrow(() -> newFail("Cannot find annotation '{}' in {}.",
            Generated.class.getName(), GeneratedClass.class));
    SdkLog.warning("comments: {}", generatedAnnotation.comments()); // <2>
  }
  // end::printAnnotationElementValues[]
}
