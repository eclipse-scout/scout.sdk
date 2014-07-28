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
package org.eclipse.scout.sdk.internal.test.operation;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.WizardStepNewOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Test;

/**
 * <h3>{@link WizardStepNewOperationTest}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 15.04.2013
 */
public class WizardStepNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testApi() throws Exception {
    IType abstractWizard = SdkAssert.assertTypeExists(IRuntimeClasses.AbstractWizardStep);
    SdkAssert.assertMethodExist(abstractWizard, "getConfiguredTitle");
    SdkAssert.assertMethodExist(abstractWizard, "getForm");
    SdkAssert.assertMethodExist(abstractWizard, "execActivate");
    SdkAssert.assertMethodExist(abstractWizard, "setForm");
    SdkAssert.assertMethodExist(abstractWizard, "getWizard");
    SdkAssert.assertMethodExist(abstractWizard, "execDeactivate");
    IType wizardStep = SdkAssert.assertTypeExists(RuntimeClasses.IWizardStep);
    SdkAssert.assertFieldExist(wizardStep, "STEP_NEXT");
  }

  @Test
  public void testNewWizardStep() throws Exception {
    IType wizard = TypeUtility.getType("sample.client.ui.wizards.EmptyWizard");
    WizardStepNewOperation newOp = new WizardStepNewOperation("Step01", wizard, true);
    newOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IWizardStep, getClientJavaProject()));
    executeBuildAssertNoCompileErrors(newOp);
    IType wizardStep = newOp.getCreatedWizardStep();
    SdkAssert.assertExist(wizardStep);
    SdkAssert.assertPublic(wizardStep).assertNoMoreFlags();
    SdkAssert.assertHasSuperType(wizardStep, RuntimeClasses.IWizardStep);
    IMethod stepGetter = SdkAssert.assertMethodExist(wizard, "getStep01");
    SdkAssert.assertElementSequenceInSource(stepGetter, wizardStep);
    // clean up
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation(true);
    delOp.addMember(wizardStep);
    delOp.addMember(stepGetter);
    executeBuildAssertNoCompileErrors(delOp);
  }

  @Test
  public void testNewWizardStepWithForm() throws Exception {
    IType wizard = TypeUtility.getType("sample.client.ui.wizards.EmptyWizard");
    WizardStepNewOperation newOp = new WizardStepNewOperation("Step02", wizard, true);
    newOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IWizardStep, getClientJavaProject()));
    newOp.setForm(TypeUtility.getType("sample.client.empty.EmptyForm"));
    newOp.setFormHandler(TypeUtility.getType("sample.client.empty.EmptyForm.ModifyHandler"));
    executeBuildAssertNoCompileErrors(newOp);
    IType wizardStep = newOp.getCreatedWizardStep();
    SdkAssert.assertExist(wizardStep);
    SdkAssert.assertPublic(wizardStep).assertNoMoreFlags();
    SdkAssert.assertHasSuperType(wizardStep, RuntimeClasses.IWizardStep);
    IMethod stepGetter = SdkAssert.assertMethodExist(wizard, "getStep02");
    SdkAssert.assertMethodExist(wizardStep, "execDeactivate");
    SdkAssert.assertMethodExist(wizardStep, "execActivate");
    SdkAssert.assertEquals("QAbstractWizardStep<QEmptyForm;>;", wizardStep.getSuperclassTypeSignature());
    SdkAssert.assertElementSequenceInSource(stepGetter, wizardStep);
    // clean up
    JavaElementDeleteOperation delOp = new JavaElementDeleteOperation(true);
    delOp.addMember(wizardStep);
    delOp.addMember(stepGetter);
    executeBuildAssertNoCompileErrors(delOp);
  }

}
