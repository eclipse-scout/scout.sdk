/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.template.formfield.AbstractExternalGroupBox;

public class ExternalGroupboxTest {

  @Test
  public void testCreateFormData() {
    createFormDataAssertNoCompileErrors(AbstractExternalGroupBox.class.getName(), ExternalGroupboxTest::testApiOfAbstractExternalGroupBoxData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private static void testApiOfAbstractExternalGroupBoxData(IType abstractExternalGroupBoxData) {
    // type AbstractExternalGroupBoxData
    assertHasFlags(abstractExternalGroupBoxData, 1025);
    assertHasSuperClass(abstractExternalGroupBoxData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData");

    // fields of AbstractExternalGroupBoxData
    assertEquals(1, abstractExternalGroupBoxData.fields().stream().count(), "field count of 'AbstractExternalGroupBoxData'");
    IField serialVersionUID = assertFieldExist(abstractExternalGroupBoxData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");

    assertEquals(1, abstractExternalGroupBoxData.methods().stream().count(), "method count of 'AbstractExternalGroupBoxData'");
    IMethod getExternalString = assertMethodExist(abstractExternalGroupBoxData, "getExternalString", new String[]{});
    assertMethodReturnType(getExternalString, "formdata.shared.services.process.AbstractExternalGroupBoxData$ExternalString");

    assertEquals(1, abstractExternalGroupBoxData.innerTypes().stream().count(), "inner types count of 'AbstractExternalGroupBoxData'");
    // type ExternalString
    IType externalString = assertTypeExists(abstractExternalGroupBoxData, "ExternalString");
    assertHasFlags(externalString, 9);
    assertHasSuperClass(externalString, "org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData<java.lang.String>");

    // fields of ExternalString
    assertEquals(1, externalString.fields().stream().count(), "field count of 'ExternalString'");
    IField serialVersionUID1 = assertFieldExist(externalString, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");

    assertEquals(0, externalString.methods().stream().count(), "method count of 'ExternalString'");

    assertEquals(0, externalString.innerTypes().stream().count(), "inner types count of 'ExternalString'");
  }
}
