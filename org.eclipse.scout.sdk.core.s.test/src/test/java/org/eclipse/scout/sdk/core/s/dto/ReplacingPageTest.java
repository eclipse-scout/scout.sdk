/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.dto;

import static org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.createPageDataAssertNoCompileErrors;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertAnnotation;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertFieldType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasFlags;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertHasSuperClass;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodExist;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertMethodReturnType;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertTypeExists;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.scout.sdk.core.model.api.IType;
import org.junit.jupiter.api.Test;

import formdata.client.ui.desktop.outline.pages.ReplacingPage;

/**
 * <h3>{@link ReplacingPageTest}</h3>
 *
 * @since 5.2.0
 */
public class ReplacingPageTest {
  @Test
  public void testPageWithTableExtensionData() {
    createPageDataAssertNoCompileErrors(ReplacingPage.class.getName(), ReplacingPageTest::testApiOfReplacingPageData);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfReplacingPageData(IType replacingPageData) {
    assertHasFlags(replacingPageData, 1);
    assertHasSuperClass(replacingPageData, "formdata.shared.services.pages.BaseTablePageData");
    assertEquals(2, replacingPageData.annotations().stream().count(), "annotation count");
    assertAnnotation(replacingPageData, "org.eclipse.scout.rt.platform.Replace");
    assertAnnotation(replacingPageData, "javax.annotation.Generated");

    // fields of ReplacingPageData
    assertEquals(1, replacingPageData.fields().stream().count(), "field count of 'formdata.shared.services.pages.ReplacingPageData'");
    var serialVersionUID = assertFieldExist(replacingPageData, "serialVersionUID");
    assertHasFlags(serialVersionUID, 26);
    assertFieldType(serialVersionUID, "long");
    assertEquals(0, serialVersionUID.annotations().stream().count(), "annotation count");

    assertEquals(7, replacingPageData.methods().stream().count(), "method count of 'formdata.shared.services.pages.ReplacingPageData'");
    var addRow = assertMethodExist(replacingPageData, "addRow");
    assertMethodReturnType(addRow, "formdata.shared.services.pages.ReplacingPageData$ReplacingRowData");
    assertEquals(1, addRow.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow, "java.lang.Override");
    var addRow1 = assertMethodExist(replacingPageData, "addRow", new String[]{"int"});
    assertMethodReturnType(addRow1, "formdata.shared.services.pages.ReplacingPageData$ReplacingRowData");
    assertEquals(1, addRow1.annotations().stream().count(), "annotation count");
    assertAnnotation(addRow1, "java.lang.Override");
    var createRow = assertMethodExist(replacingPageData, "createRow");
    assertMethodReturnType(createRow, "formdata.shared.services.pages.ReplacingPageData$ReplacingRowData");
    assertEquals(1, createRow.annotations().stream().count(), "annotation count");
    assertAnnotation(createRow, "java.lang.Override");
    var getRowType = assertMethodExist(replacingPageData, "getRowType");
    assertMethodReturnType(getRowType, "java.lang.Class<? extends org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData>");
    assertEquals(1, getRowType.annotations().stream().count(), "annotation count");
    assertAnnotation(getRowType, "java.lang.Override");
    var getRows = assertMethodExist(replacingPageData, "getRows");
    assertMethodReturnType(getRows, "formdata.shared.services.pages.ReplacingPageData$ReplacingRowData[]");
    assertEquals(1, getRows.annotations().stream().count(), "annotation count");
    assertAnnotation(getRows, "java.lang.Override");
    var rowAt = assertMethodExist(replacingPageData, "rowAt", new String[]{"int"});
    assertMethodReturnType(rowAt, "formdata.shared.services.pages.ReplacingPageData$ReplacingRowData");
    assertEquals(1, rowAt.annotations().stream().count(), "annotation count");
    assertAnnotation(rowAt, "java.lang.Override");
    var setRows = assertMethodExist(replacingPageData, "setRows", new String[]{"formdata.shared.services.pages.ReplacingPageData$ReplacingRowData[]"});
    assertMethodReturnType(setRows, "void");
    assertEquals(0, setRows.annotations().stream().count(), "annotation count");

    assertEquals(1, replacingPageData.innerTypes().stream().count(), "inner types count of 'ReplacingPageData'");
    // type ReplacingRowData
    var replacingRowData = assertTypeExists(replacingPageData, "ReplacingRowData");
    assertHasFlags(replacingRowData, 9);
    assertHasSuperClass(replacingRowData, "formdata.shared.services.pages.BaseTablePageData$BaseTableRowData");
    assertEquals(0, replacingRowData.annotations().stream().count(), "annotation count");

    // fields of ReplacingRowData
    assertEquals(1, replacingRowData.fields().stream().count(), "field count of 'formdata.shared.services.pages.ReplacingPageData$ReplacingRowData'");
    var serialVersionUID1 = assertFieldExist(replacingRowData, "serialVersionUID");
    assertHasFlags(serialVersionUID1, 26);
    assertFieldType(serialVersionUID1, "long");
    assertEquals(0, serialVersionUID1.annotations().stream().count(), "annotation count");

    assertEquals(0, replacingRowData.methods().stream().count(), "method count of 'formdata.shared.services.pages.ReplacingPageData$ReplacingRowData'");

    assertEquals(0, replacingRowData.innerTypes().stream().count(), "inner types count of 'ReplacingRowData'");
  }

}
