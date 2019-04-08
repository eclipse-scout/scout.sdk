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
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperIntefaceSignatures;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMetaValue;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.api.MetaValueType;
import org.junit.jupiter.api.Test;

import formdata.client.extensions.ExtensionToAbstractTableFieldTemplate;
import formdata.client.extensions.SimpleTableFormExtensionWithTable;

/**
 * <h3>{@link TableFieldExtensionTest}</h3>
 *
 * @since 5.2.0
 */
public class TableFieldExtensionTest {

  @Test
  public void testAbstractTableFieldExtension() {
    createFormDataAssertNoCompileErrors(ExtensionToAbstractTableFieldTemplate.class.getName(), TableFieldExtensionTest::testApiOfExtensionToAbstractTableFieldTemplateData);
  }

  @Test
  public void testTableFieldExtension() {
    createFormDataAssertNoCompileErrors(SimpleTableFormExtensionWithTable.class.getName(), TableFieldExtensionTest::testApiOfSimpleTableFormExtensionWithTableData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfExtensionToAbstractTableFieldTemplateData(IType extensionToAbstractTableFieldTemplateData) {
    assertHasFlags(extensionToAbstractTableFieldTemplateData, 1);
    assertHasSuperClass(extensionToAbstractTableFieldTemplateData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData");
    assertEquals(2, extensionToAbstractTableFieldTemplateData.annotations().stream().count(), "annotation count");
    assertAnnotation(extensionToAbstractTableFieldTemplateData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertAnnotation(extensionToAbstractTableFieldTemplateData, "javax.annotation.Generated");

    // fields of ExtensionToAbstractTableFieldTemplateData
    assertEquals(1, extensionToAbstractTableFieldTemplateData.fields().stream().count(), "field count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData'");
    IField serialVersionUID = assertFieldExist(extensionToAbstractTableFieldTemplateData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(0, extensionToAbstractTableFieldTemplateData.methods().stream().count(), "method count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData'");

    assertEquals(1, extensionToAbstractTableFieldTemplateData.innerTypes().stream().count(), "inner types count of 'ExtensionToAbstractTableFieldTemplateData'");
    // type CustomAbstractAdvisorTableFieldTableExtensionRowData
    IType customAbstractAdvisorTableFieldTableExtensionRowData = assertTypeExists(extensionToAbstractTableFieldTemplateData, "CustomAbstractAdvisorTableFieldTableExtensionRowData");
    assertHasFlags(customAbstractAdvisorTableFieldTableExtensionRowData, 9);
    assertHasSuperClass(customAbstractAdvisorTableFieldTableExtensionRowData, "java.lang.Object");
    assertHasSuperIntefaceSignatures(customAbstractAdvisorTableFieldTableExtensionRowData, new String[]{"java.io.Serializable"});
    assertEquals(1, customAbstractAdvisorTableFieldTableExtensionRowData.annotations().stream().count(), "annotation count");
    IAnnotation rowDataExtendsAnnot = assertAnnotation(customAbstractAdvisorTableFieldTableExtensionRowData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertValueOfRowDataExtendsAnnotation("formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData", rowDataExtendsAnnot);

    // fields of CustomAbstractAdvisorTableFieldTableExtensionRowData
    assertEquals(3, customAbstractAdvisorTableFieldTableExtensionRowData.fields().stream().count(),
        "field count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData$CustomAbstractAdvisorTableFieldTableExtensionRowData'");
    IField serialVersionUID1 = assertFieldExist(customAbstractAdvisorTableFieldTableExtensionRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");
    IField added = assertFieldExist(customAbstractAdvisorTableFieldTableExtensionRowData, "added");
    assertHasFlags(added, 25);
    assertFieldType(added, "java.lang.String");
    assertEquals(0, added.annotations().stream().count(), "annotation count");
    IField m_added = assertFieldExist(customAbstractAdvisorTableFieldTableExtensionRowData, "m_added");
    assertHasFlags(m_added, 2);
    assertFieldType(m_added, "java.lang.Boolean");
    assertEquals(0, m_added.annotations().stream().count(), "annotation count");

    assertEquals(2, customAbstractAdvisorTableFieldTableExtensionRowData.methods().stream().count(),
        "method count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData$CustomAbstractAdvisorTableFieldTableExtensionRowData'");
    IMethod getAdded = assertMethodExist(customAbstractAdvisorTableFieldTableExtensionRowData, "getAdded", new String[]{});
    assertMethodReturnType(getAdded, "java.lang.Boolean");
    assertEquals(0, getAdded.annotations().stream().count(), "annotation count");
    IMethod setAdded = assertMethodExist(customAbstractAdvisorTableFieldTableExtensionRowData, "setAdded", new String[]{"java.lang.Boolean"});
    assertMethodReturnType(setAdded, "void");
    assertEquals(0, setAdded.annotations().stream().count(), "annotation count");

    assertEquals(0, customAbstractAdvisorTableFieldTableExtensionRowData.innerTypes().stream().count(), "inner types count of 'CustomAbstractAdvisorTableFieldTableExtensionRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfSimpleTableFormExtensionWithTableData(IType simpleTableFormExtensionWithTableData) {
    assertHasFlags(simpleTableFormExtensionWithTableData, 1);
    assertHasSuperClass(simpleTableFormExtensionWithTableData, "org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData");
    assertEquals(2, simpleTableFormExtensionWithTableData.annotations().stream().count(), "annotation count");
    assertAnnotation(simpleTableFormExtensionWithTableData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertAnnotation(simpleTableFormExtensionWithTableData, "javax.annotation.Generated");

    // fields of SimpleTableFormExtensionWithTableData
    assertEquals(1, simpleTableFormExtensionWithTableData.fields().stream().count(), "field count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData'");
    IField serialVersionUID = assertFieldExist(simpleTableFormExtensionWithTableData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(0, simpleTableFormExtensionWithTableData.methods().stream().count(), "method count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData'");

    assertEquals(1, simpleTableFormExtensionWithTableData.innerTypes().stream().count(), "inner types count of 'SimpleTableFormExtensionWithTableData'");
    // type TestTableFieldExtensionRowData
    IType testTableFieldExtensionRowData = assertTypeExists(simpleTableFormExtensionWithTableData, "TestTableFieldExtensionRowData");
    assertHasFlags(testTableFieldExtensionRowData, 9);
    assertHasSuperIntefaceSignatures(testTableFieldExtensionRowData, new String[]{"java.io.Serializable"});
    assertEquals(1, testTableFieldExtensionRowData.annotations().stream().count(), "annotation count");
    IAnnotation rowDataExtendsAnnot = assertAnnotation(testTableFieldExtensionRowData, "org.eclipse.scout.rt.platform.extension.Extends");
    assertValueOfRowDataExtendsAnnotation("formdata.shared.services.process.SimpleTableFormData$TestTable$TestTableRowData", rowDataExtendsAnnot);

    // fields of TestTableFieldExtensionRowData
    assertEquals(3, testTableFieldExtensionRowData.fields().stream().count(), "field count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData$TestTableFieldExtensionRowData'");
    IField serialVersionUID1 = assertFieldExist(testTableFieldExtensionRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");
    IField contributed = assertFieldExist(testTableFieldExtensionRowData, "contributed");
    assertHasFlags(contributed, 25);
    assertFieldType(contributed, "java.lang.String");
    assertEquals(0, contributed.annotations().stream().count(), "annotation count");
    IField m_contributed = assertFieldExist(testTableFieldExtensionRowData, "m_contributed");
    assertHasFlags(m_contributed, 2);
    assertFieldType(m_contributed, "java.math.BigDecimal");
    assertEquals(0, m_contributed.annotations().stream().count(), "annotation count");

    assertEquals(2, testTableFieldExtensionRowData.methods().stream().count(), "method count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData$TestTableFieldExtensionRowData'");
    IMethod getContributed = assertMethodExist(testTableFieldExtensionRowData, "getContributed", new String[]{});
    assertMethodReturnType(getContributed, "java.math.BigDecimal");
    assertEquals(0, getContributed.annotations().stream().count(), "annotation count");
    IMethod setContributed = assertMethodExist(testTableFieldExtensionRowData, "setContributed", new String[]{"java.math.BigDecimal"});
    assertMethodReturnType(setContributed, "void");
    assertEquals(0, setContributed.annotations().stream().count(), "annotation count");

    assertEquals(0, testTableFieldExtensionRowData.innerTypes().stream().count(), "inner types count of 'TestTableFieldExtensionRowData'");
  }

  private static void assertValueOfRowDataExtendsAnnotation(String expectedFqn, IAnnotation rowDataExtendsAnnot) {
    IMetaValue value = rowDataExtendsAnnot.element("value").get().value();
    assertEquals(MetaValueType.Type, value.type());
    IType extendsType = value.as(IType.class);
    assertEquals(expectedFqn, extendsType.name());
  }
}