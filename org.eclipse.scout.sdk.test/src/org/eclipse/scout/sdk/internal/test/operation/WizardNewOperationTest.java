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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.WizardNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Test;

/**
 * <h3>{@link WizardNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 15.04.2013
 */
public class WizardNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testApi() throws Exception {
    IType abstractWizard = TypeUtility.getType("org.eclipse.scout.rt.client.ui.wizard.AbstractWizard");
    SdkAssert.assertMethodExist(abstractWizard, "getConfiguredTitle");
  }

  @Test
  public void testNewWizard() throws Exception {
    WizardNewOperation newOp = new WizardNewOperation("Test01Wizard", getClientJavaProject().getElementName() + ".wizard.output", getClientJavaProject());
    newOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IWizard, getClientJavaProject()));
    executeBuildAssertNoCompileErrors(newOp);
    IType wizard = newOp.getCreatedWizard();
    SdkAssert.assertExist(wizard);
    SdkAssert.assertPublic(wizard).assertNoMoreFlags();
    SdkAssert.assertHasSuperType(wizard, RuntimeClasses.IWizard);
    SdkAssert.assertMethodExist(wizard, "Test01Wizard");
  }

  @Test
  public void testNewOutlineOnDesktop() throws Exception {
    WizardNewOperation newOp = new WizardNewOperation("Test02Wizard", getClientJavaProject().getElementName() + ".wizard.output", getClientJavaProject());
    newOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IWizard, getClientJavaProject()));
    // nls
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    INlsEntry entry = nlsProject.getEntry("Text02");
    newOp.setNlsEntry(entry);
    executeBuildAssertNoCompileErrors(newOp);
    IType wizard = newOp.getCreatedWizard();
    SdkAssert.assertExist(wizard);
    SdkAssert.assertPublic(wizard).assertNoMoreFlags();
    SdkAssert.assertHasSuperType(wizard, RuntimeClasses.IWizard);
    SdkAssert.assertMethodExist(wizard, "Test02Wizard");
    SdkAssert.assertMethodExist(wizard, "getConfiguredTitle");
  }
}
