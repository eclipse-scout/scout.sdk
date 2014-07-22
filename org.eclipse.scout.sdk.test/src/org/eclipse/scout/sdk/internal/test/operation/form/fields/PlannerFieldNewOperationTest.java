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
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.field.FormFieldDeleteOperation;
import org.eclipse.scout.sdk.operation.form.field.PlannerFieldNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link PlannerFieldNewOperationTest}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class PlannerFieldNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void apiTests() throws Exception {
    IType abstractPlannerField = SdkAssert.assertTypeExists(IRuntimeClasses.AbstractPlannerField);
    SdkAssert.assertMethodExistInSuperTypeHierarchy(abstractPlannerField, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
    IType abstractTable = SdkAssert.assertTypeExists("org.eclipse.scout.rt.client.ui.basic.table.AbstractTable");
    SdkAssert.assertMethodExist(abstractTable, "getConfiguredAutoResizeColumns");
    SdkAssert.assertMethodExist(abstractTable, "getConfiguredSortEnabled");
  }

  @Test
  public void testNewField() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    PlannerFieldNewOperation fieldNewOp = new PlannerFieldNewOperation("TestField01", mainBox);
    fieldNewOp.setSuperTypeSignature(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractPlannerField));
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(-1000));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(10));
    SdkAssert.assertHasSuperType(field, RuntimeClasses.IPlannerField);
    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewFieldWithNlsName() throws Exception {
    Assert.assertTrue(TypeUtility.exists(getSharedJavaProject()));
    IType mainBox = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox");
    PlannerFieldNewOperation fieldNewOp = new PlannerFieldNewOperation("TestField02", mainBox);
    fieldNewOp.setSuperTypeSignature(SignatureCache.createTypeSignature(IRuntimeClasses.AbstractPlannerField));
    //nls
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    INlsEntry entry = nlsProject.getEntry("Text02");
    fieldNewOp.setNlsEntry(entry);
    IType okButton = TypeUtility.getType("sample.client.empty.EmptyForm.MainBox.OkButton");
    fieldNewOp.setSibling(okButton);
    executeBuildAssertNoCompileErrors(fieldNewOp);
    IType field = fieldNewOp.getCreatedField();
    SdkAssert.assertExist(field);
    SdkAssert.assertPublic(field).assertNoMoreFlags();
    SdkAssert.assertOrderAnnotation(field, Double.valueOf(-1000));
    SdkAssert.assertOrderAnnotation(okButton, Double.valueOf(10));
    SdkAssert.assertHasSuperType(field, RuntimeClasses.IPlannerField);
    SdkAssert.assertMethodExist(field, SdkProperties.METHOD_NAME_GET_CONFIGURED_LABEL);
    // clean up
    FormFieldDeleteOperation delOp = new FormFieldDeleteOperation(field, true);
    executeBuildAssertNoCompileErrors(delOp);
  }

}
