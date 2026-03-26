/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.generator.field;

import java.io.Serial;
import java.util.Optional;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.apidef.Api;
import org.eclipse.scout.sdk.core.java.apidef.ISerialApi;
import org.eclipse.scout.sdk.core.java.builder.IJavaSourceBuilder;
import org.eclipse.scout.sdk.core.java.generator.annotation.AnnotationGenerator;

/**
 * <h3>{@link SerialVersionUidFieldGenerator}</h3>
 */
public class SerialVersionUidFieldGenerator extends FieldGenerator<SerialVersionUidFieldGenerator> {

  public static final String SERIAL_VERSION_UID = "serialVersionUID";

  public SerialVersionUidFieldGenerator(long value) {
    withElementName(SERIAL_VERSION_UID)
        .withDataType(JavaTypes._long)
        .asPrivate()
        .asStatic()
        .asFinal()
        .withValue(ISourceGenerator.raw(value + "L"));
  }

  @Override
  protected void build(IJavaSourceBuilder<?> builder) {
    var isSerialAnnotationEnabled = Api.allKnownTypes().stream()
        .map(apiType -> builder.context().api(apiType))
        .flatMap(Optional::stream)
        .map(apiType -> apiType.api(ISerialApi.class))
        .flatMap(Optional::stream)
        .anyMatch(ISerialApi::isSerialAnnotationEnabled);

    withoutAnnotation(Serial.class.getName()); // remove annotation in case it already exists
    if (isSerialAnnotationEnabled) {
      withAnnotation(AnnotationGenerator.createSerial());
    }
    super.build(builder);
  }
}
