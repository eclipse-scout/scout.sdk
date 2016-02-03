/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.sourcebuilder.dto;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

import formdata.client.extensions.SimpleTableFormExtensionWithTable;

/**
 * <h3>{@link TableFieldExtensionTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class TableFieldExtensionTest {

  @Test
  public void testTableFieldExtension() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(SimpleTableFormExtensionWithTable.class.getName());
    testApiOfSimpleTableFormExtensionWithTableData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfSimpleTableFormExtensionWithTableData(IType simpleTableFormExtensionWithTableData) {
    SdkAssert.assertHasFlags(simpleTableFormExtensionWithTableData, 1);
    SdkAssert.assertHasSuperTypeSignature(simpleTableFormExtensionWithTableData, "Lorg.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;");
    Assert.assertEquals("annotation count", 2, simpleTableFormExtensionWithTableData.annotations().list().size());
    SdkAssert.assertAnnotation(simpleTableFormExtensionWithTableData, "org.eclipse.scout.rt.platform.extension.Extends");
    SdkAssert.assertAnnotation(simpleTableFormExtensionWithTableData, "javax.annotation.Generated");

    // fields of SimpleTableFormExtensionWithTableData
    Assert.assertEquals("field count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData'", 1, simpleTableFormExtensionWithTableData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(simpleTableFormExtensionWithTableData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData'", 0, simpleTableFormExtensionWithTableData.methods().list().size());

    Assert.assertEquals("inner types count of 'SimpleTableFormExtensionWithTableData'", 1, simpleTableFormExtensionWithTableData.innerTypes().list().size());
    // type TestTableFieldExtensionRowData
    IType testTableFieldExtensionRowData = SdkAssert.assertTypeExists(simpleTableFormExtensionWithTableData, "TestTableFieldExtensionRowData");
    SdkAssert.assertHasFlags(testTableFieldExtensionRowData, 9);
    SdkAssert.assertHasSuperIntefaceSignatures(testTableFieldExtensionRowData, new String[]{"Ljava.io.Serializable;"});
    Assert.assertEquals("annotation count", 1, testTableFieldExtensionRowData.annotations().list().size());
    IAnnotation rowDataExtendsAnnot = SdkAssert.assertAnnotation(testTableFieldExtensionRowData, "org.eclipse.scout.rt.platform.extension.Extends");
    testValueOfRowDataExtendsAnnotation(rowDataExtendsAnnot);

    // fields of TestTableFieldExtensionRowData
    Assert.assertEquals("field count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData$TestTableFieldExtensionRowData'", 3, testTableFieldExtensionRowData.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(testTableFieldExtensionRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID1.annotations().list().size());
    IField contributed = SdkAssert.assertFieldExist(testTableFieldExtensionRowData, "contributed");
    SdkAssert.assertHasFlags(contributed, 25);
    SdkAssert.assertFieldSignature(contributed, "Ljava.lang.String;");
    Assert.assertEquals("annotation count", 0, contributed.annotations().list().size());
    IField m_contributed = SdkAssert.assertFieldExist(testTableFieldExtensionRowData, "m_contributed");
    SdkAssert.assertHasFlags(m_contributed, 2);
    SdkAssert.assertFieldSignature(m_contributed, "Ljava.math.BigDecimal;");
    Assert.assertEquals("annotation count", 0, m_contributed.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData$TestTableFieldExtensionRowData'", 2, testTableFieldExtensionRowData.methods().list().size());
    IMethod getContributed = SdkAssert.assertMethodExist(testTableFieldExtensionRowData, "getContributed", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getContributed, "Ljava.math.BigDecimal;");
    Assert.assertEquals("annotation count", 0, getContributed.annotations().list().size());
    IMethod setContributed = SdkAssert.assertMethodExist(testTableFieldExtensionRowData, "setContributed", new String[]{"Ljava.math.BigDecimal;"});
    SdkAssert.assertMethodReturnTypeSignature(setContributed, "V");
    Assert.assertEquals("annotation count", 0, setContributed.annotations().list().size());

    Assert.assertEquals("inner types count of 'TestTableFieldExtensionRowData'", 0, testTableFieldExtensionRowData.innerTypes().list().size());
  }

  private static void testValueOfRowDataExtendsAnnotation(IAnnotation rowDataExtendsAnnot) {
    IMetaValue value = rowDataExtendsAnnot.element("value").value();
    Assert.assertEquals(MetaValueType.Type, value.type());
    IType extendsType = value.get(IType.class);
    Assert.assertEquals("formdata.shared.services.process.SimpleTableFormData$TestTable$TestTableRowData", extendsType.name());
  }
}
