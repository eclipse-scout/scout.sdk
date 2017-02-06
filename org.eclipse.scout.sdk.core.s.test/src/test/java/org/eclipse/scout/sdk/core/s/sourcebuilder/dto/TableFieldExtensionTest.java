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

import formdata.client.extensions.ExtensionToAbstractTableFieldTemplate;
import formdata.client.extensions.SimpleTableFormExtensionWithTable;

/**
 * <h3>{@link TableFieldExtensionTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class TableFieldExtensionTest {

  @Test
  public void testAbstractTableFieldExtension() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(ExtensionToAbstractTableFieldTemplate.class.getName());
    testApiOfExtensionToAbstractTableFieldTemplateData(dto);
  }

  @Test
  public void testTableFieldExtension() {
    IType dto = CoreScoutTestingUtils.createFormDataAssertNoCompileErrors(SimpleTableFormExtensionWithTable.class.getName());
    testApiOfSimpleTableFormExtensionWithTableData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfExtensionToAbstractTableFieldTemplateData(IType extensionToAbstractTableFieldTemplateData) {
    SdkAssert.assertHasFlags(extensionToAbstractTableFieldTemplateData, 1);
    SdkAssert.assertHasSuperTypeSignature(extensionToAbstractTableFieldTemplateData, "Lorg.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;");
    Assert.assertEquals("annotation count", 2, extensionToAbstractTableFieldTemplateData.annotations().list().size());
    SdkAssert.assertAnnotation(extensionToAbstractTableFieldTemplateData, "org.eclipse.scout.rt.platform.extension.Extends");
    SdkAssert.assertAnnotation(extensionToAbstractTableFieldTemplateData, "javax.annotation.Generated");

    // fields of ExtensionToAbstractTableFieldTemplateData
    Assert.assertEquals("field count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData'", 1, extensionToAbstractTableFieldTemplateData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(extensionToAbstractTableFieldTemplateData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData'", 0, extensionToAbstractTableFieldTemplateData.methods().list().size());

    Assert.assertEquals("inner types count of 'ExtensionToAbstractTableFieldTemplateData'", 1, extensionToAbstractTableFieldTemplateData.innerTypes().list().size());
    // type CustomAbstractAdvisorTableFieldTableExtensionRowData
    IType customAbstractAdvisorTableFieldTableExtensionRowData = SdkAssert.assertTypeExists(extensionToAbstractTableFieldTemplateData, "CustomAbstractAdvisorTableFieldTableExtensionRowData");
    SdkAssert.assertHasFlags(customAbstractAdvisorTableFieldTableExtensionRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(customAbstractAdvisorTableFieldTableExtensionRowData, "Ljava.lang.Object;");
    SdkAssert.assertHasSuperIntefaceSignatures(customAbstractAdvisorTableFieldTableExtensionRowData, new String[]{"Ljava.io.Serializable;"});
    Assert.assertEquals("annotation count", 1, customAbstractAdvisorTableFieldTableExtensionRowData.annotations().list().size());
    IAnnotation rowDataExtendsAnnot = SdkAssert.assertAnnotation(customAbstractAdvisorTableFieldTableExtensionRowData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertValueOfRowDataExtendsAnnotation("formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData", rowDataExtendsAnnot);

    // fields of CustomAbstractAdvisorTableFieldTableExtensionRowData
    Assert.assertEquals("field count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData$CustomAbstractAdvisorTableFieldTableExtensionRowData'", 3,
        customAbstractAdvisorTableFieldTableExtensionRowData.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(customAbstractAdvisorTableFieldTableExtensionRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID1.annotations().list().size());
    IField added = SdkAssert.assertFieldExist(customAbstractAdvisorTableFieldTableExtensionRowData, "added");
    SdkAssert.assertHasFlags(added, 25);
    SdkAssert.assertFieldSignature(added, "Ljava.lang.String;");
    Assert.assertEquals("annotation count", 0, added.annotations().list().size());
    IField m_added = SdkAssert.assertFieldExist(customAbstractAdvisorTableFieldTableExtensionRowData, "m_added");
    SdkAssert.assertHasFlags(m_added, 2);
    SdkAssert.assertFieldSignature(m_added, "Ljava.lang.Boolean;");
    Assert.assertEquals("annotation count", 0, m_added.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData$CustomAbstractAdvisorTableFieldTableExtensionRowData'", 2,
        customAbstractAdvisorTableFieldTableExtensionRowData.methods().list().size());
    IMethod getAdded = SdkAssert.assertMethodExist(customAbstractAdvisorTableFieldTableExtensionRowData, "getAdded", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getAdded, "Ljava.lang.Boolean;");
    Assert.assertEquals("annotation count", 0, getAdded.annotations().list().size());
    IMethod setAdded = SdkAssert.assertMethodExist(customAbstractAdvisorTableFieldTableExtensionRowData, "setAdded", new String[]{"Ljava.lang.Boolean;"});
    SdkAssert.assertMethodReturnTypeSignature(setAdded, "V");
    Assert.assertEquals("annotation count", 0, setAdded.annotations().list().size());

    Assert.assertEquals("inner types count of 'CustomAbstractAdvisorTableFieldTableExtensionRowData'", 0, customAbstractAdvisorTableFieldTableExtensionRowData.innerTypes().list().size());
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
    assertValueOfRowDataExtendsAnnotation("formdata.shared.services.process.SimpleTableFormData$TestTable$TestTableRowData", rowDataExtendsAnnot);

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

  private static void assertValueOfRowDataExtendsAnnotation(String expectedFqn, IAnnotation rowDataExtendsAnnot) {
    IMetaValue value = rowDataExtendsAnnot.element("value").value();
    Assert.assertEquals(MetaValueType.Type, value.type());
    IType extendsType = value.get(IType.class);
    Assert.assertEquals(expectedFqn, extendsType.name());
  }
}
