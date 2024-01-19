/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertHasSuperInterfaces;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertTypeExists;
import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createFormDataAssertNoCompileErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link TableFieldExtensionTest}</h3>
 *
 * @since 5.2.0
 */
public class TableFieldExtensionTest {

  @Test
  public void testAbstractTableFieldExtension() {
    createFormDataAssertNoCompileErrors("formdata.client.extensions.ExtensionToAbstractTableFieldTemplate", TableFieldExtensionTest::testApiOfExtensionToAbstractTableFieldTemplateData);
  }

  @Test
  public void testTableFieldExtension() {
    createFormDataAssertNoCompileErrors("formdata.client.extensions.SimpleTableFormExtensionWithTable", TableFieldExtensionTest::testApiOfSimpleTableFormExtensionWithTableData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfExtensionToAbstractTableFieldTemplateData(IType extensionToAbstractTableFieldTemplateData) {
    var scoutApi = extensionToAbstractTableFieldTemplateData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(extensionToAbstractTableFieldTemplateData, Flags.AccPublic);
    assertHasSuperClass(extensionToAbstractTableFieldTemplateData, scoutApi.AbstractFormFieldData());
    assertEquals(2, extensionToAbstractTableFieldTemplateData.annotations().stream().count(), "annotation count");
    assertAnnotation(extensionToAbstractTableFieldTemplateData, scoutApi.Extends());
    assertAnnotation(extensionToAbstractTableFieldTemplateData, scoutApi.Generated());

    // fields of ExtensionToAbstractTableFieldTemplateData
    assertEquals(1, extensionToAbstractTableFieldTemplateData.fields().stream().count(), "field count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData'");
    var serialVersionUID = assertFieldExist(extensionToAbstractTableFieldTemplateData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(0, extensionToAbstractTableFieldTemplateData.methods().stream().count(), "method count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData'");

    assertEquals(1, extensionToAbstractTableFieldTemplateData.innerTypes().stream().count(), "inner types count of 'ExtensionToAbstractTableFieldTemplateData'");
    // type CustomAbstractAdvisorTableFieldTableExtensionRowData
    var customAbstractAdvisorTableFieldTableExtensionRowData = assertTypeExists(extensionToAbstractTableFieldTemplateData, "CustomAbstractAdvisorTableFieldTableExtensionRowData");
    assertHasFlags(customAbstractAdvisorTableFieldTableExtensionRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(customAbstractAdvisorTableFieldTableExtensionRowData, "java.lang.Object");
    assertHasSuperInterfaces(customAbstractAdvisorTableFieldTableExtensionRowData, new String[]{"java.io.Serializable"});
    assertEquals(1, customAbstractAdvisorTableFieldTableExtensionRowData.annotations().stream().count(), "annotation count");
    // --> manual modification
    var rowDataExtendsAnnot = assertAnnotation(customAbstractAdvisorTableFieldTableExtensionRowData, scoutApi.Extends());
    assertValueOfRowDataExtendsAnnotation("formdata.shared.services.process.AbstractAddressTableFieldData$AbstractAddressTableRowData", rowDataExtendsAnnot);
    // <-- manual modification

    // fields of CustomAbstractAdvisorTableFieldTableExtensionRowData
    assertEquals(3, customAbstractAdvisorTableFieldTableExtensionRowData.fields().stream().count(),
        "field count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData$CustomAbstractAdvisorTableFieldTableExtensionRowData'");
    var serialVersionUID1 = assertFieldExist(customAbstractAdvisorTableFieldTableExtensionRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");
    var added = assertFieldExist(customAbstractAdvisorTableFieldTableExtensionRowData, "added");
    assertHasFlags(added, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(added, "java.lang.String");
    assertEquals(0, added.annotations().stream().count(), "annotation count");
    var m_added = assertFieldExist(customAbstractAdvisorTableFieldTableExtensionRowData, "m_added");
    assertHasFlags(m_added, Flags.AccPrivate);
    assertFieldType(m_added, "java.lang.Boolean");
    assertEquals(0, m_added.annotations().stream().count(), "annotation count");

    assertEquals(2, customAbstractAdvisorTableFieldTableExtensionRowData.methods().stream().count(),
        "method count of 'formdata.shared.extension.ExtensionToAbstractTableFieldTemplateData$CustomAbstractAdvisorTableFieldTableExtensionRowData'");
    var getAdded = assertMethodExist(customAbstractAdvisorTableFieldTableExtensionRowData, "getAdded");
    assertMethodReturnType(getAdded, "java.lang.Boolean");
    assertEquals(0, getAdded.annotations().stream().count(), "annotation count");
    var setAdded = assertMethodExist(customAbstractAdvisorTableFieldTableExtensionRowData, "setAdded", new String[]{"java.lang.Boolean"});
    assertMethodReturnType(setAdded, "void");
    assertEquals(0, setAdded.annotations().stream().count(), "annotation count");

    assertEquals(0, customAbstractAdvisorTableFieldTableExtensionRowData.innerTypes().stream().count(), "inner types count of 'CustomAbstractAdvisorTableFieldTableExtensionRowData'");
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.java.testing.ApiTestGenerator
   */
  private static void testApiOfSimpleTableFormExtensionWithTableData(IType simpleTableFormExtensionWithTableData) {
    var scoutApi = simpleTableFormExtensionWithTableData.javaEnvironment().requireApi(IScoutApi.class);

    assertHasFlags(simpleTableFormExtensionWithTableData, Flags.AccPublic);
    assertHasSuperClass(simpleTableFormExtensionWithTableData, scoutApi.AbstractFormFieldData());
    assertEquals(2, simpleTableFormExtensionWithTableData.annotations().stream().count(), "annotation count");
    assertAnnotation(simpleTableFormExtensionWithTableData, scoutApi.Extends());
    assertAnnotation(simpleTableFormExtensionWithTableData, scoutApi.Generated());

    // fields of SimpleTableFormExtensionWithTableData
    assertEquals(1, simpleTableFormExtensionWithTableData.fields().stream().count(), "field count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData'");
    var serialVersionUID = assertFieldExist(simpleTableFormExtensionWithTableData, "serialVersionUID");
    assertHasFlags(serialVersionUID, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(0, simpleTableFormExtensionWithTableData.methods().stream().count(), "method count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData'");

    assertEquals(1, simpleTableFormExtensionWithTableData.innerTypes().stream().count(), "inner types count of 'SimpleTableFormExtensionWithTableData'");
    // type TestTableFieldExtensionRowData
    var testTableFieldExtensionRowData = assertTypeExists(simpleTableFormExtensionWithTableData, "TestTableFieldExtensionRowData");
    assertHasFlags(testTableFieldExtensionRowData, Flags.AccPublic | Flags.AccStatic);
    assertHasSuperClass(testTableFieldExtensionRowData, "java.lang.Object");
    assertHasSuperInterfaces(testTableFieldExtensionRowData, new String[]{"java.io.Serializable"});
    assertEquals(1, testTableFieldExtensionRowData.annotations().stream().count(), "annotation count");
    // --> manual modification
    var rowDataExtendsAnnot = assertAnnotation(testTableFieldExtensionRowData, scoutApi.Extends());
    assertValueOfRowDataExtendsAnnotation("formdata.shared.services.process.SimpleTableFormData$TestTable$TestTableRowData", rowDataExtendsAnnot);
    // <-- manual modification

    // fields of TestTableFieldExtensionRowData
    assertEquals(3, testTableFieldExtensionRowData.fields().stream().count(), "field count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData$TestTableFieldExtensionRowData'");
    var serialVersionUID1 = assertFieldExist(testTableFieldExtensionRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, Flags.AccPrivate | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");
    var contributed = assertFieldExist(testTableFieldExtensionRowData, "contributed");
    assertHasFlags(contributed, Flags.AccPublic | Flags.AccStatic | Flags.AccFinal);
    assertFieldType(contributed, "java.lang.String");
    assertEquals(0, contributed.annotations().stream().count(), "annotation count");
    var m_contributed = assertFieldExist(testTableFieldExtensionRowData, "m_contributed");
    assertHasFlags(m_contributed, Flags.AccPrivate);
    assertFieldType(m_contributed, "java.math.BigDecimal");
    assertEquals(0, m_contributed.annotations().stream().count(), "annotation count");

    assertEquals(2, testTableFieldExtensionRowData.methods().stream().count(), "method count of 'formdata.shared.extension.SimpleTableFormExtensionWithTableData$TestTableFieldExtensionRowData'");
    var getContributed = assertMethodExist(testTableFieldExtensionRowData, "getContributed");
    assertMethodReturnType(getContributed, "java.math.BigDecimal");
    assertEquals(0, getContributed.annotations().stream().count(), "annotation count");
    var setContributed = assertMethodExist(testTableFieldExtensionRowData, "setContributed", new String[]{"java.math.BigDecimal"});
    assertMethodReturnType(setContributed, "void");
    assertEquals(0, setContributed.annotations().stream().count(), "annotation count");

    assertEquals(0, testTableFieldExtensionRowData.innerTypes().stream().count(), "inner types count of 'TestTableFieldExtensionRowData'");
  }

  private static void assertValueOfRowDataExtendsAnnotation(String expectedFqn, IAnnotation rowDataExtendsAnnot) {
    var value = rowDataExtendsAnnot.element("value").orElseThrow().value();
    assertEquals(MetaValueType.Type, value.type());
    var extendsType = value.as(IType.class);
    assertEquals(expectedFqn, extendsType.name());
  }
}
