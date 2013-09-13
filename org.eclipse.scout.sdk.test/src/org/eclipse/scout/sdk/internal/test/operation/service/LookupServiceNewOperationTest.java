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
import org.eclipse.scout.sdk.operation.service.LookupServiceNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.junit.Test;

/**
 * <h3>{@link LookupServiceNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class LookupServiceNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewSqlLookupService() throws Exception {
    LookupServiceNewOperation serviceOp = new LookupServiceNewOperation("ITestService01", "TestService01");
    serviceOp.setImplementationProject(getServerJavaProject());
    serviceOp.setImplementationPackageName("sample.server.services.test.output");
    serviceOp.setInterfaceProject(getSharedJavaProject());
    serviceOp.setInterfacePackageName("sample.shared.services.test.output");
    serviceOp.setImplementationSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractSqlLookupService));
    serviceOp.addInterfaceInterfaceSignature(SignatureCache.createTypeSignature(RuntimeClasses.ILookupService));

    executeBuildAssertNoCompileErrors(serviceOp);
    IType interfaceType = serviceOp.getCreatedServiceInterface();
    IType implementationType = serviceOp.getCreatedServiceImplementation();
    System.out.println(interfaceType.getCompilationUnit().getSource());
    System.out.println(implementationType.getCompilationUnit().getSource());
    SdkAssert.assertExist(interfaceType);
    SdkAssert.assertExist(implementationType);
    SdkAssert.assertEquals(interfaceType.getElementName(), implementationType.getSuperInterfaceNames()[0]);
    SdkAssert.assertMethodExist(implementationType, "getConfiguredSqlSelect");
    SdkAssert.assertEquals(1, implementationType.getMethods().length);
  }

  @Test
  public void testNewLookupService() throws Exception {
    LookupServiceNewOperation serviceOp = new LookupServiceNewOperation("ITestService02", "TestService02");
    serviceOp.setImplementationProject(getServerJavaProject());
    serviceOp.setImplementationPackageName("sample.server.services.test.output");
    serviceOp.setInterfaceProject(getSharedJavaProject());
    serviceOp.setInterfacePackageName("sample.shared.services.test.output");
    serviceOp.setImplementationSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractLookupService));
    serviceOp.addInterfaceInterfaceSignature(SignatureCache.createTypeSignature(RuntimeClasses.ILookupService));

    executeBuildAssertNoCompileErrors(serviceOp);
    IType interfaceType = serviceOp.getCreatedServiceInterface();
    IType implementationType = serviceOp.getCreatedServiceImplementation();
    System.out.println(interfaceType.getCompilationUnit().getSource());
    System.out.println(implementationType.getCompilationUnit().getSource());
    SdkAssert.assertExist(interfaceType);
    SdkAssert.assertExist(implementationType);
    SdkAssert.assertEquals(interfaceType.getElementName(), implementationType.getSuperInterfaceNames()[0]);
    SdkAssert.assertMethodExist(implementationType, "getDataByAll");
    SdkAssert.assertMethodExist(implementationType, "getDataByKey");
    SdkAssert.assertMethodExist(implementationType, "getDataByRec");
    SdkAssert.assertMethodExist(implementationType, "getDataByText");
    SdkAssert.assertEquals(4, implementationType.getMethods().length);
  }
}
