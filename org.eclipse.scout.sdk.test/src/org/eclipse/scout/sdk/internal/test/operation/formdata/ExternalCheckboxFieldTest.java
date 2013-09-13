/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.test.operation.formdata;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataAnnotation;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

public class ExternalCheckboxFieldTest extends AbstractSdkTestWithFormDataProject {

  @Test
  public void testCreateFormData() throws Exception {
    String templateName = "AbstractTestCheckboxField";
    IType template = TypeUtility.getType("formdata.client.ui.template.formfield." + templateName);
    Assert.assertTrue(TypeUtility.exists(template));

    FormDataAnnotation annotation = ScoutTypeUtility.findFormDataAnnotation(template, TypeUtility.getSuperTypeHierarchy(template));

    FormDataUpdateOperation op = new FormDataUpdateOperation(template, TypeUtility.getTypeBySignature(annotation.getFormDataTypeSignature()).getCompilationUnit());
    executeBuildAssertNoCompileErrors(SYSTEM_PROPERTIES_FORM_DATA_USER, op);
    testApiOfAbstractTestCheckboxFieldData();

  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfAbstractTestCheckboxFieldData() throws Exception {
    // type AbstractTestCheckboxFieldData
    IType abstractTestCheckboxFieldData = SdkAssert.assertTypeExists("formdata.shared.services.process.AbstractTestCheckboxFieldData");
    SdkAssert.assertHasFlags(abstractTestCheckboxFieldData, 1025);
    SdkAssert.assertHasSuperTypeSignature(abstractTestCheckboxFieldData, "QAbstractValueFieldData<QBoolean;>;");

    // fields of AbstractTestCheckboxFieldData
    SdkAssert.assertEquals("field count of 'AbstractTestCheckboxFieldData'", 1, abstractTestCheckboxFieldData.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(abstractTestCheckboxFieldData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'AbstractTestCheckboxFieldData'", 1, abstractTestCheckboxFieldData.getMethods().length);
    IMethod abstractTestCheckboxFieldData1 = SdkAssert.assertMethodExist(abstractTestCheckboxFieldData, "AbstractTestCheckboxFieldData", new String[]{});
    SdkAssert.assertTrue(abstractTestCheckboxFieldData1.isConstructor());
    SdkAssert.assertMethodReturnTypeSignature(abstractTestCheckboxFieldData1, "V");

    SdkAssert.assertEquals("inner types count of 'AbstractTestCheckboxFieldData'", 0, abstractTestCheckboxFieldData.getTypes().length);
  }

}
