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
package org.eclipse.scout.sdk.internal.test.operation.service;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.service.ProcessServiceNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Test;

/**
 * <h3>{@link ProcessServiceNewOperationTest}</h3>
 *
 *  @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class ProcessServiceNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewProcessService() throws Exception {
    ProcessServiceNewOperation serviceOp = new ProcessServiceNewOperation("Test01Service");
    serviceOp.setImplementationProject(getServerJavaProject());
    serviceOp.setImplementationPackageName("sample.server.services.test.output");
    serviceOp.setInterfaceProject(getSharedJavaProject());
    serviceOp.setInterfacePackageName("sample.shared.services.test.output");
    serviceOp.setImplementationSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService, getServerJavaProject()));
    serviceOp.setFormData(TypeUtility.getType("sample.shared.form.data.TestFormData"));
    serviceOp.setPermissionsEntityName("Test01");
    serviceOp.setPermissionsProject(getSharedJavaProject());
    serviceOp.setPermissionsPackageName("sample.shared.services.test.output");
    serviceOp.setFormatSource(true);

    executeBuildAssertNoCompileErrors(serviceOp);
    IType interfaceType = serviceOp.getCreatedServiceInterface();
    IType implementationType = serviceOp.getCreatedServiceImplementation();
    SdkAssert.assertExist(interfaceType);
    SdkAssert.assertExist(implementationType);
    SdkAssert.assertEquals(interfaceType.getElementName(), implementationType.getSuperInterfaceNames()[0]);

    SdkAssert.assertTypeExists("sample.shared.services.test.output.CreateTest01Permission");
    SdkAssert.assertTypeExists("sample.shared.services.test.output.ReadTest01Permission");
    SdkAssert.assertTypeExists("sample.shared.services.test.output.UpdateTest01Permission");

    SdkAssert.assertMethodExist(interfaceType, "store");
    SdkAssert.assertMethodExist(interfaceType, "create");
    SdkAssert.assertMethodExist(interfaceType, "load");

    SdkAssert.assertMethodExist(implementationType, "store");
    SdkAssert.assertMethodExist(implementationType, "create");
    SdkAssert.assertMethodExist(implementationType, "load");
  }

}
