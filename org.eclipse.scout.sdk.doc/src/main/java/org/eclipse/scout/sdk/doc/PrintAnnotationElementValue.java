/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.doc;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.sdk.core.java.ISourceFolders;
import org.eclipse.scout.sdk.core.java.ecj.JavaEnvironmentWithEcjBuilder;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.java.annotation.ClassIdAnnotation;

@SuppressWarnings({"MethodMayBeStatic"})
public class PrintAnnotationElementValue {

  public static void main(String[] args) {
    new PrintAnnotationElementValue().printAnnotationElementValue();
  }

  public void printAnnotationElementValue() {
    new JavaEnvironmentWithEcjBuilder<>()
        .withSourceFolder(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        .accept(this::printAnnotationElementValue);
  }

  // tag::printAnnotationElementValue[]
  @ClassId("4b845c66-6b6b-40f1-89d7-99741a07aabe")
  public static class ClassWithClassId {
  }

  public void printAnnotationElementValue(IJavaEnvironment javaEnvironment) {
    var classIdAnnotation = javaEnvironment
        .requireType(ClassWithClassId.class.getName())
        .annotations()
        .withManagedWrapper(ClassIdAnnotation.class) // <1>
        .first().orElseThrow(() -> newFail("Cannot find annotation '{}' in {}.",
            ClassId.class.getName(), ClassWithClassId.class));
    SdkLog.warning("value: {}", classIdAnnotation.value()); // <2>
  }
  // end::printAnnotationElementValue[]
}
