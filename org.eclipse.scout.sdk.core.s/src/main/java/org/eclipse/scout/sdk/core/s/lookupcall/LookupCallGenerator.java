/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.lookupcall;

import java.util.Optional;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.java.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.s.java.generator.annotation.ScoutAnnotationGenerator;
import org.eclipse.scout.sdk.core.s.java.generator.method.ScoutMethodGenerator;
import org.eclipse.scout.sdk.core.util.Strings;

/**
 * <h3>{@link LookupCallGenerator}</h3>
 *
 * @since 5.2.0
 */
public class LookupCallGenerator<TYPE extends LookupCallGenerator<TYPE>> extends PrimaryTypeGenerator<TYPE> {

  private String m_superType;
  private String m_keyType;
  private String m_lookupServiceInterface;
  private String m_classIdValue;

  @Override
  protected void setup() {
    if (superType().isPresent() && keyType().isPresent()) {
      var superTypeBuilder = new StringBuilder(superType().orElseThrow());
      superTypeBuilder.append(JavaTypes.C_GENERIC_START);
      superTypeBuilder.append(keyType().orElseThrow());
      superTypeBuilder.append(JavaTypes.C_GENERIC_END);
      withSuperClass(superTypeBuilder.toString());
    }
    withAnnotation(classIdValue()
        .map(ScoutAnnotationGenerator::createClassId)
        .orElse(null))
            .withField(FieldGenerator.createSerialVersionUid());

    if (lookupServiceInterface().isPresent() && keyType().isPresent()) {
      withMethod(ScoutMethodGenerator.createGetConfiguredService(lookupServiceInterface().orElseThrow(), keyType().orElseThrow()));
    }
  }

  public Optional<String> superType() {
    return Strings.notBlank(m_superType);
  }

  public TYPE withSuperType(String superType) {
    m_superType = superType;
    return thisInstance();
  }

  public Optional<String> keyType() {
    return Strings.notBlank(m_keyType);
  }

  public TYPE withKeyType(String keyType) {
    m_keyType = keyType;
    return thisInstance();
  }

  public Optional<String> lookupServiceInterface() {
    return Strings.notBlank(m_lookupServiceInterface);
  }

  public TYPE withLookupServiceInterface(String lookupServiceIfc) {
    m_lookupServiceInterface = lookupServiceIfc;
    return thisInstance();
  }

  public Optional<String> classIdValue() {
    return Strings.notBlank(m_classIdValue);
  }

  public TYPE withClassIdValue(String classIdValue) {
    m_classIdValue = classIdValue;
    return thisInstance();
  }
}
