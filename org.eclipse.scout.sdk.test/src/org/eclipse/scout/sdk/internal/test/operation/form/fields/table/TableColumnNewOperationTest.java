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
package org.eclipse.scout.sdk.internal.test.operation.form.fields.table;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.field.table.TableColumnDeleteOperation;
import org.eclipse.scout.sdk.operation.form.field.table.TableColumnNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Test;

/**
 * <h3>{@link TableColumnNewOperationTest}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class TableColumnNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewColumn() throws Exception {
    IType table = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.EmptyTableField.Table");
    TableColumnNewOperation colOp = new TableColumnNewOperation("SampleColumn01", table);
    colOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IStringColumn, getClientJavaProject()));
    executeBuildAssertNoCompileErrors(colOp);
    IType column = colOp.getCreatedColumn();
    SdkAssert.assertExist(column);
    SdkAssert.assertPublic(column).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(column, Double.valueOf(1000));
    SdkAssert.assertHasSuperType(column, IRuntimeClasses.IStringColumn);
    SdkAssert.assertMethodExist(table, "getSampleColumn01");
    // clean up
    TableColumnDeleteOperation delOp = new TableColumnDeleteOperation(column);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewColumnWithNlsText() throws Exception {
    IType table = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.EmptyTableField.Table");
    TableColumnNewOperation colOp = new TableColumnNewOperation("SampleColumn02", table);
    colOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.IDateColumn, getClientJavaProject()));
    // nls
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    INlsEntry entry = nlsProject.getEntry("Text02");
    colOp.setNlsEntry(entry);
    executeBuildAssertNoCompileErrors(colOp);
    IType column = colOp.getCreatedColumn();
    SdkAssert.assertExist(column);
    SdkAssert.assertPublic(column).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(column, Double.valueOf(1000));
    SdkAssert.assertHasSuperType(column, IRuntimeClasses.IDateColumn);
    SdkAssert.assertMethodExist(column, SdkProperties.METHOD_NAME_GET_CONFIGURED_HEADER_TEXT);
    SdkAssert.assertMethodExist(table, "getSampleColumn02");

    // clean up
    TableColumnDeleteOperation delOp = new TableColumnDeleteOperation(column);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewFirstColumn() throws Exception {
    IType firstColumn = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table.DoubleColumn");
    IType secondColumn = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table.LongColumn");
    IType thirdColumn = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table.StringColumn");

    IType table = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table");
    TableColumnNewOperation colOp = new TableColumnNewOperation("SampleColumn03", table);
    colOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.ISmartColumn, getClientJavaProject()));
    colOp.setSibling(firstColumn);
    executeBuildAssertNoCompileErrors(colOp);
    IType column = colOp.getCreatedColumn();
    SdkAssert.assertExist(column);
    SdkAssert.assertPublic(column).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(column, Double.valueOf(0));
    SdkAssert.assertOrderAnnotation(firstColumn, Double.valueOf(1000));
    SdkAssert.assertOrderAnnotation(secondColumn, Double.valueOf(2000));
    SdkAssert.assertOrderAnnotation(thirdColumn, Double.valueOf(3000));
    SdkAssert.assertElementSequenceInSource(new IMember[]{column, firstColumn, secondColumn, thirdColumn});
    SdkAssert.assertHasSuperType(column, IRuntimeClasses.ISmartColumn);
    SdkAssert.assertMethodExist(table, "getSampleColumn03");

    // clean up
    TableColumnDeleteOperation delOp = new TableColumnDeleteOperation(column);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewMiddleColumn() throws Exception {
    IType firstColumn = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table.DoubleColumn");
    IType secondColumn = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table.LongColumn");
    IType thirdColumn = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table.StringColumn");

    IType table = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table");
    TableColumnNewOperation colOp = new TableColumnNewOperation("SampleColumn04", table);
    colOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.ISmartColumn, getClientJavaProject()));
    colOp.setSibling(secondColumn);
    executeBuildAssertNoCompileErrors(colOp);
    IType column = colOp.getCreatedColumn();
    SdkAssert.assertExist(column);
    SdkAssert.assertPublic(column).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(firstColumn, Double.valueOf(1000));
    SdkAssert.assertOrderAnnotation(column, Double.valueOf(1500));
    SdkAssert.assertOrderAnnotation(secondColumn, Double.valueOf(2000));
    SdkAssert.assertOrderAnnotation(thirdColumn, Double.valueOf(3000));
    SdkAssert.assertElementSequenceInSource(new IMember[]{firstColumn, column, secondColumn, thirdColumn});
    SdkAssert.assertHasSuperType(column, IRuntimeClasses.ISmartColumn);
    SdkAssert.assertMethodExist(table, "getSampleColumn04");

    // clean up
    TableColumnDeleteOperation delOp = new TableColumnDeleteOperation(column);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewLastColumn() throws Exception {
    IType firstColumn = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table.DoubleColumn");
    IType secondColumn = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table.LongColumn");
    IType thirdColumn = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table.StringColumn");

    IType table = SdkAssert.assertTypeExists("sample.client.person.PersonForm.MainBox.TableField.Table");
    TableColumnNewOperation colOp = new TableColumnNewOperation("SampleColumn05", table);
    colOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.ILongColumn, getClientJavaProject()));
    executeBuildAssertNoCompileErrors(colOp);
    IType column = colOp.getCreatedColumn();
    SdkAssert.assertExist(column);
    SdkAssert.assertPublic(column).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(firstColumn, Double.valueOf(1000));
    SdkAssert.assertOrderAnnotation(secondColumn, Double.valueOf(2000));
    SdkAssert.assertOrderAnnotation(thirdColumn, Double.valueOf(3000));
    SdkAssert.assertOrderAnnotation(column, Double.valueOf(4000));
    SdkAssert.assertElementSequenceInSource(new IMember[]{firstColumn, secondColumn, thirdColumn, column});
    SdkAssert.assertHasSuperType(column, IRuntimeClasses.ILongColumn);
    SdkAssert.assertMethodExist(table, "getSampleColumn05");

    // clean up
    TableColumnDeleteOperation delOp = new TableColumnDeleteOperation(column);
    executeBuildAssertNoCompileErrors(delOp);
  }
}
