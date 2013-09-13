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
package org.eclipse.scout.sdk.internal.test.operation.form;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.form.FormStackNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link FormStackNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class FormStackNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewForm() throws Exception {
    IJavaProject clientProject = getClientJavaProject();
    Assert.assertTrue(TypeUtility.exists(clientProject));
    String typeName = "TestForm01";
    String packageName = "sample.client.test.output";
    FormStackNewOperation formOp = new FormStackNewOperation(typeName, packageName, getClientJavaProject());
    formOp.setFormatSource(true);
    formOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IForm, getClientJavaProject()));
    formOp.addServiceProxyRegistrationProject(getClientJavaProject());
    formOp.addServiceRegistrationProject(getServerJavaProject());
    formOp.setCreateButtonCancel(true);
    formOp.setCreateButtonOk(true);
    formOp.setCreateModifyHandler(true);
    formOp.setCreateNewHandler(true);
    formOp.setFormDataPackage("sample.shared.test.output");
    formOp.setFormDataProject(getSharedJavaProject());
    formOp.setFormIdName("testId");
    formOp.setFormIdSignature(Signature.SIG_LONG);
    // nls
    INlsProject nlsProject = ScoutTypeUtility.findNlsProject(getSharedJavaProject());
    INlsEntry entry = nlsProject.getEntry("Text02");
    formOp.setNlsEntry(entry);
    formOp.setPermissionCreateName("CreateTestForm01Permission");
    formOp.setPermissionCreatePackage("sample.shared.test.output");
    formOp.setPermissionCreateProject(getSharedJavaProject());
    formOp.setPermissionReadName("ReadTestForm01Permission");
    formOp.setPermissionReadPackage("sample.shared.test.output");
    formOp.setPermissionReadProject(getSharedJavaProject());
    formOp.setPermissionUpdateName("UpdateTestForm01Permission");
    formOp.setPermissionUpdatePackage("sample.shared.test.output");
    formOp.setPermissionUpdateProject(getSharedJavaProject());
    formOp.setServiceImplementationName("Test01Service");
    formOp.setServiceImplementationProject(getServerJavaProject());
    formOp.setServiceImplementationPackage("sample.server.test.output");
    formOp.setServiceInterfaceName("ITest01Service");
    formOp.setServiceInterfaceProject(getSharedJavaProject());
    formOp.setServiceInterfacePackage("sample.shared.test.output");

    executeBuildAssertNoCompileErrors(formOp);
    IType form = formOp.getCreatedType();
    SdkAssert.assertFieldExist(form, "m_testId");
    SdkAssert.assertMethodExist(form, "getTestId");
    SdkAssert.assertMethodExist(form, "setTestId");
    SdkAssert.assertExist(form);
    IType mainBox = formOp.getCreatedMainBox();
    SdkAssert.assertExist(mainBox);
    SdkAssert.assertTypeExists(mainBox, "OkButton");
    SdkAssert.assertTypeExists(mainBox, "CancelButton");

    SdkAssert.assertExist(formOp.getCreatedCreatePermission());
    SdkAssert.assertExist(formOp.getCreatedReadPermission());
    SdkAssert.assertExist(formOp.getCreatedUpdatePermission());
    SdkAssert.assertExist(formOp.getCreatedFormData());
    SdkAssert.assertExist(formOp.getCreatedService());
    SdkAssert.assertMethodExist(formOp.getCreatedService(), "prepareCreate");
    SdkAssert.assertMethodExist(formOp.getCreatedService(), "create");
    SdkAssert.assertMethodExist(formOp.getCreatedService(), "load");
    SdkAssert.assertMethodExist(formOp.getCreatedService(), "store");
    SdkAssert.assertExist(formOp.getCreatedServiceInterface());
    SdkAssert.assertMethodExist(formOp.getCreatedServiceInterface(), "prepareCreate");
    SdkAssert.assertMethodExist(formOp.getCreatedServiceInterface(), "create");
    SdkAssert.assertMethodExist(formOp.getCreatedServiceInterface(), "load");
    SdkAssert.assertMethodExist(formOp.getCreatedServiceInterface(), "store");
    IType newHandler = SdkAssert.assertTypeExists(form, "NewHandler");
    SdkAssert.assertMethodExist(newHandler, "execLoad");
    SdkAssert.assertMethodExist(newHandler, "execStore");
    IType modifyHandler = SdkAssert.assertTypeExists(form, "ModifyHandler");
    SdkAssert.assertMethodExist(modifyHandler, "execLoad");
    SdkAssert.assertMethodExist(modifyHandler, "execStore");
    // service registration
    SdkAssert.assertServiceProxyRegistered(getClientProject(), formOp.getCreatedServiceInterface());
    SdkAssert.assertServiceRegistered(getServerProject(), formOp.getCreatedService());

    PluginModelHelper h = new PluginModelHelper(getClientProject());
    SdkAssert.assertTrue(h.Manifest.existsExportPackage(packageName));

  }
}
