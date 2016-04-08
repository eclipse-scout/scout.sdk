/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.sourcebuilder.dto;

import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.testing.CoreScoutTestingUtils;
import org.eclipse.scout.sdk.core.testing.SdkAssert;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link ReplacingPageTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class ReplacingPageTest {
  @Test
  public void testPageWithTableExtensionData() {
    IType dto = CoreScoutTestingUtils.createPageDataAssertNoCompileErrors("formdata.client.ui.desktop.outline.pages.ReplacingPage");
    testApiOfReplacingPageData(dto);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.core.testing.ApiTestGenerator
   */
  private static void testApiOfReplacingPageData(IType replacingPageData) {
    SdkAssert.assertHasFlags(replacingPageData, 1);
    SdkAssert.assertHasSuperTypeSignature(replacingPageData, "Lformdata.shared.services.pages.BaseTablePageData;");
    Assert.assertEquals("annotation count", 2, replacingPageData.annotations().list().size());
    SdkAssert.assertAnnotation(replacingPageData, "org.eclipse.scout.rt.platform.Replace");
    SdkAssert.assertAnnotation(replacingPageData, "javax.annotation.Generated");

    // fields of ReplacingPageData
    Assert.assertEquals("field count of 'formdata.shared.services.pages.ReplacingPageData'", 1, replacingPageData.fields().list().size());
    IField serialVersionUID = SdkAssert.assertFieldExist(replacingPageData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.services.pages.ReplacingPageData'", 7, replacingPageData.methods().list().size());
    IMethod addRow = SdkAssert.assertMethodExist(replacingPageData, "addRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(addRow, "Lformdata.shared.services.pages.ReplacingPageData$ReplacingRowData;");
    Assert.assertEquals("annotation count", 1, addRow.annotations().list().size());
    SdkAssert.assertAnnotation(addRow, "java.lang.Override");
    IMethod addRow1 = SdkAssert.assertMethodExist(replacingPageData, "addRow", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(addRow1, "Lformdata.shared.services.pages.ReplacingPageData$ReplacingRowData;");
    Assert.assertEquals("annotation count", 1, addRow1.annotations().list().size());
    SdkAssert.assertAnnotation(addRow1, "java.lang.Override");
    IMethod createRow = SdkAssert.assertMethodExist(replacingPageData, "createRow", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(createRow, "Lformdata.shared.services.pages.ReplacingPageData$ReplacingRowData;");
    Assert.assertEquals("annotation count", 1, createRow.annotations().list().size());
    SdkAssert.assertAnnotation(createRow, "java.lang.Override");
    IMethod getRowType = SdkAssert.assertMethodExist(replacingPageData, "getRowType", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRowType, "Ljava.lang.Class<+Lorg.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;>;");
    Assert.assertEquals("annotation count", 1, getRowType.annotations().list().size());
    SdkAssert.assertAnnotation(getRowType, "java.lang.Override");
    IMethod getRows = SdkAssert.assertMethodExist(replacingPageData, "getRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getRows, "[Lformdata.shared.services.pages.ReplacingPageData$ReplacingRowData;");
    Assert.assertEquals("annotation count", 1, getRows.annotations().list().size());
    SdkAssert.assertAnnotation(getRows, "java.lang.Override");
    IMethod rowAt = SdkAssert.assertMethodExist(replacingPageData, "rowAt", new String[]{"I"});
    SdkAssert.assertMethodReturnTypeSignature(rowAt, "Lformdata.shared.services.pages.ReplacingPageData$ReplacingRowData;");
    Assert.assertEquals("annotation count", 1, rowAt.annotations().list().size());
    SdkAssert.assertAnnotation(rowAt, "java.lang.Override");
    IMethod setRows = SdkAssert.assertMethodExist(replacingPageData, "setRows", new String[]{"[Lformdata.shared.services.pages.ReplacingPageData$ReplacingRowData;"});
    SdkAssert.assertMethodReturnTypeSignature(setRows, "V");
    Assert.assertEquals("annotation count", 0, setRows.annotations().list().size());

    Assert.assertEquals("inner types count of 'ReplacingPageData'", 1, replacingPageData.innerTypes().list().size());
    // type ReplacingRowData
    IType replacingRowData = SdkAssert.assertTypeExists(replacingPageData, "ReplacingRowData");
    SdkAssert.assertHasFlags(replacingRowData, 9);
    SdkAssert.assertHasSuperTypeSignature(replacingRowData, "Lformdata.shared.services.pages.BaseTablePageData$BaseTableRowData;");
    Assert.assertEquals("annotation count", 0, replacingRowData.annotations().list().size());

    // fields of ReplacingRowData
    Assert.assertEquals("field count of 'formdata.shared.services.pages.ReplacingPageData$ReplacingRowData'", 1, replacingRowData.fields().list().size());
    IField serialVersionUID1 = SdkAssert.assertFieldExist(replacingRowData, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID1, 26);
    SdkAssert.assertFieldSignature(serialVersionUID1, "J");
    Assert.assertEquals("annotation count", 0, serialVersionUID1.annotations().list().size());

    Assert.assertEquals("method count of 'formdata.shared.services.pages.ReplacingPageData$ReplacingRowData'", 0, replacingRowData.methods().list().size());

    Assert.assertEquals("inner types count of 'ReplacingRowData'", 0, replacingRowData.innerTypes().list().size());
  }

}
