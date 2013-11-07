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
package org.eclipse.scout.sdk.internal.test.operation.form.fields;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.field.BoxDeleteOperation;
import org.eclipse.scout.sdk.operation.form.field.SequenceBoxNewOperation;
import org.eclipse.scout.sdk.operation.template.sequencebox.DateFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.DateTimeFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.DoubleFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.IntegerFromToTemplate;
import org.eclipse.scout.sdk.operation.template.sequencebox.LongFromToTemplate;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Test;

/**
 * <h3>{@link SequenceBoxNewOperationTest}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class SequenceBoxNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewSequenceBox() throws Exception {
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SequenceBoxNewOperation fieldNewOp = new SequenceBoxNewOperation("Test01Box", mainBox);
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest01Box");
    // clean up
    BoxDeleteOperation delOp = new BoxDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testDateSequenceBox() throws Exception {
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SequenceBoxNewOperation fieldNewOp = new SequenceBoxNewOperation("Test02Box", mainBox, true);
    fieldNewOp.setContentTemplate(new DateFromToTemplate());
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    IType from = SdkAssert.assertTypeExists(field, "Test02From");
    IType to = SdkAssert.assertTypeExists(field, "Test02To");
    SdkAssert.assertHasSuperType(from, RuntimeClasses.IDateField);
    SdkAssert.assertHasSuperType(to, RuntimeClasses.IDateField);
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest02Box");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest02From");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest02To");
    // clean up
    BoxDeleteOperation delOp = new BoxDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testDateTimeSequenceBox() throws Exception {
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SequenceBoxNewOperation fieldNewOp = new SequenceBoxNewOperation("Test03Box", mainBox, true);
    fieldNewOp.setContentTemplate(new DateTimeFromToTemplate());
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    IType from = SdkAssert.assertTypeExists(field, "Test03From");
    IType to = SdkAssert.assertTypeExists(field, "Test03To");
    SdkAssert.assertHasSuperType(from, RuntimeClasses.IDateField);
    SdkAssert.assertHasSuperType(to, RuntimeClasses.IDateField);
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest03Box");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest03From");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest03To");
    SdkAssert.assertMethodExist(from, "getConfiguredHasTime");
    SdkAssert.assertMethodExist(to, "getConfiguredHasTime");
    // clean up
    BoxDeleteOperation delOp = new BoxDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testDobleSequenceBox() throws Exception {
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SequenceBoxNewOperation fieldNewOp = new SequenceBoxNewOperation("Test03Box", mainBox, true);
    fieldNewOp.setContentTemplate(new DoubleFromToTemplate());
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    IType from = SdkAssert.assertTypeExists(field, "Test03From");
    IType to = SdkAssert.assertTypeExists(field, "Test03To");
    SdkAssert.assertHasSuperType(from, RuntimeClasses.IDoubleField);
    SdkAssert.assertHasSuperType(to, RuntimeClasses.IDoubleField);
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest03Box");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest03From");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest03To");
    // clean up
    BoxDeleteOperation delOp = new BoxDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testIntegerSequenceBox() throws Exception {
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SequenceBoxNewOperation fieldNewOp = new SequenceBoxNewOperation("Test04Box", mainBox, true);
    fieldNewOp.setContentTemplate(new IntegerFromToTemplate());
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    IType from = SdkAssert.assertTypeExists(field, "Test04From");
    IType to = SdkAssert.assertTypeExists(field, "Test04To");
    SdkAssert.assertHasSuperType(from, RuntimeClasses.IIntegerField);
    SdkAssert.assertHasSuperType(to, RuntimeClasses.IIntegerField);
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest04Box");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest04From");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest04To");
    // clean up
    BoxDeleteOperation delOp = new BoxDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testLongSequenceBox() throws Exception {
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    SequenceBoxNewOperation fieldNewOp = new SequenceBoxNewOperation("Test05Box", mainBox, true);
    fieldNewOp.setContentTemplate(new LongFromToTemplate());
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(10));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(20));
    IType from = SdkAssert.assertTypeExists(field, "Test05From");
    IType to = SdkAssert.assertTypeExists(field, "Test05To");
    SdkAssert.assertHasSuperType(from, RuntimeClasses.ILongField);
    SdkAssert.assertHasSuperType(to, RuntimeClasses.ILongField);
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest05Box");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest05From");
    SdkAssert.assertMethodExist(TypeUtility.getToplevelType(mainBox), "getTest05To");
    // clean up
    BoxDeleteOperation delOp = new BoxDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }
}
