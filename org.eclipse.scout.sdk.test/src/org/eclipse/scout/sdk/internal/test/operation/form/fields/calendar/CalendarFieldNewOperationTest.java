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
package org.eclipse.scout.sdk.internal.test.operation.form.fields.calendar;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.field.CalendarFieldNewOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldDeleteOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link CalendarFieldNewOperationTest}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class CalendarFieldNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewField() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    CalendarFieldNewOperation fieldNewOp = new CalendarFieldNewOperation("TestField01", mainBox);
    fieldNewOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ICalendarField, mainBox.getJavaProject()));
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);

    testApiOfMainBox();

    IType field = fieldNewOp.getCreatedCalendarField();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(-1000));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(10));
    IType calendar = fieldNewOp.getCreatedCalendar();
    SdkAssert.assertExist(calendar);
    SdkAssert.assertOrderAnnotation(calendar, Double.valueOf(1000));

    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewFieldCustom() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    CalendarFieldNewOperation fieldNewOp = new CalendarFieldNewOperation("TestField02", mainBox);
    fieldNewOp.setSuperTypeSignature(Signature.createTypeSignature("sample.client.field.ext.AbstractCustomCalendarField", true));
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedCalendarField();

    testApiOfMainBox01();
    // additional asserts
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(-1000));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(10));

    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfMainBox() throws Exception {
    // type MainBox
    IType mainBox = SdkAssert.assertTypeExists("sample.client.empty.EmptyForm$MainBox");
    SdkAssert.assertHasFlags(mainBox, 1);
    SdkAssert.assertHasSuperTypeSignature(mainBox, "QAbstractGroupBox;");
    SdkAssert.assertAnnotation(mainBox, "org.eclipse.scout.commons.annotations.Order");

    // fields of MainBox
    SdkAssert.assertEquals("field count of 'MainBox'", 0, mainBox.getFields().length);

    SdkAssert.assertEquals("method count of 'MainBox'", 0, mainBox.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'MainBox'", 3, mainBox.getTypes().length);
    // type TestField01
    IType testField01 = SdkAssert.assertTypeExists(mainBox, "TestField01");
    SdkAssert.assertHasFlags(testField01, 1);
    SdkAssert.assertHasSuperTypeSignature(testField01, "QAbstractCalendarField<QCalendar;>;");
    SdkAssert.assertAnnotation(testField01, "org.eclipse.scout.commons.annotations.Order");

    // fields of TestField01
    SdkAssert.assertEquals("field count of 'TestField01'", 0, testField01.getFields().length);

    SdkAssert.assertEquals("method count of 'TestField01'", 0, testField01.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'TestField01'", 1, testField01.getTypes().length);
    // type Calendar
    IType calendar = SdkAssert.assertTypeExists(testField01, "Calendar");
    SdkAssert.assertHasFlags(calendar, 1);
    SdkAssert.assertHasSuperTypeSignature(calendar, "QAbstractExtensibleCalendar;");
    SdkAssert.assertAnnotation(calendar, "org.eclipse.scout.commons.annotations.Order");

    // fields of Calendar
    SdkAssert.assertEquals("field count of 'Calendar'", 0, calendar.getFields().length);

    SdkAssert.assertEquals("method count of 'Calendar'", 0, calendar.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'Calendar'", 0, calendar.getTypes().length);
    // type OkButton
    IType okButton = SdkAssert.assertTypeExists(mainBox, "OkButton");
    SdkAssert.assertHasFlags(okButton, 1);
    SdkAssert.assertHasSuperTypeSignature(okButton, "QAbstractOkButton;");
    SdkAssert.assertAnnotation(okButton, "org.eclipse.scout.commons.annotations.Order");

    // fields of OkButton
    SdkAssert.assertEquals("field count of 'OkButton'", 0, okButton.getFields().length);

    SdkAssert.assertEquals("method count of 'OkButton'", 0, okButton.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'OkButton'", 0, okButton.getTypes().length);
    // type CancelButton
    IType cancelButton = SdkAssert.assertTypeExists(mainBox, "CancelButton");
    SdkAssert.assertHasFlags(cancelButton, 1);
    SdkAssert.assertHasSuperTypeSignature(cancelButton, "QAbstractCancelButton;");
    SdkAssert.assertAnnotation(cancelButton, "org.eclipse.scout.commons.annotations.Order");

    // fields of CancelButton
    SdkAssert.assertEquals("field count of 'CancelButton'", 0, cancelButton.getFields().length);

    SdkAssert.assertEquals("method count of 'CancelButton'", 0, cancelButton.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'CancelButton'", 0, cancelButton.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfMainBox01() throws Exception {
    // type MainBox
    IType mainBox = SdkAssert.assertTypeExists("sample.client.empty.EmptyForm$MainBox");
    SdkAssert.assertHasFlags(mainBox, 1);
    SdkAssert.assertHasSuperTypeSignature(mainBox, "QAbstractGroupBox;");
    SdkAssert.assertAnnotation(mainBox, "org.eclipse.scout.commons.annotations.Order");

    // fields of MainBox
    SdkAssert.assertEquals("field count of 'MainBox'", 0, mainBox.getFields().length);

    SdkAssert.assertEquals("method count of 'MainBox'", 0, mainBox.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'MainBox'", 3, mainBox.getTypes().length);
    // type TestField02
    IType testField02 = SdkAssert.assertTypeExists(mainBox, "TestField02");
    SdkAssert.assertHasFlags(testField02, 1);
    SdkAssert.assertHasSuperTypeSignature(testField02, "QAbstractCustomCalendarField;");
    SdkAssert.assertAnnotation(testField02, "org.eclipse.scout.commons.annotations.Order");

    // fields of TestField02
    SdkAssert.assertEquals("field count of 'TestField02'", 0, testField02.getFields().length);

    SdkAssert.assertEquals("method count of 'TestField02'", 0, testField02.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'TestField02'", 0, testField02.getTypes().length);
    // type OkButton
    IType okButton = SdkAssert.assertTypeExists(mainBox, "OkButton");
    SdkAssert.assertHasFlags(okButton, 1);
    SdkAssert.assertHasSuperTypeSignature(okButton, "QAbstractOkButton;");
    SdkAssert.assertAnnotation(okButton, "org.eclipse.scout.commons.annotations.Order");

    // fields of OkButton
    SdkAssert.assertEquals("field count of 'OkButton'", 0, okButton.getFields().length);

    SdkAssert.assertEquals("method count of 'OkButton'", 0, okButton.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'OkButton'", 0, okButton.getTypes().length);
    // type CancelButton
    IType cancelButton = SdkAssert.assertTypeExists(mainBox, "CancelButton");
    SdkAssert.assertHasFlags(cancelButton, 1);
    SdkAssert.assertHasSuperTypeSignature(cancelButton, "QAbstractCancelButton;");
    SdkAssert.assertAnnotation(cancelButton, "org.eclipse.scout.commons.annotations.Order");

    // fields of CancelButton
    SdkAssert.assertEquals("field count of 'CancelButton'", 0, cancelButton.getFields().length);

    SdkAssert.assertEquals("method count of 'CancelButton'", 0, cancelButton.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'CancelButton'", 0, cancelButton.getTypes().length);
  }
}
