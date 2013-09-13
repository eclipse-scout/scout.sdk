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
package org.eclipse.scout.sdk.internal.test.operation.lookupcall;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.lookupcall.LocalLookupCallNewOperation;
import org.eclipse.scout.sdk.operation.lookupcall.LookupCallNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.junit.Test;

/**
 * <h3>{@link LookupCallNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class LookupCallNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewLocalLookupCall() throws Exception {
    LocalLookupCallNewOperation newOp = new LocalLookupCallNewOperation("CountryLookupCall", getSharedJavaProject().getElementName() + ".lookupcall.output", getSharedJavaProject());
    newOp.setLookupCallSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.LocalLookupCall));
    executeBuildAssertNoCompileErrors(newOp);
    IType lookupCall = newOp.getCreatedLookupCall();
    SdkAssert.assertExist(lookupCall);
    SdkAssert.assertPublic(lookupCall).assertNoMoreFlags();
    SdkAssert.assertMethodExist(lookupCall, "execCreateLookupRows");
    SdkAssert.assertHasSuperType(lookupCall, RuntimeClasses.LookupCall);
  }

  @Test
  public void testNewLookupCall() throws Exception {
    LookupCallNewOperation newOp = new LookupCallNewOperation("Test01LookupCall", getSharedJavaProject().getElementName() + ".lookupcall.output", getSharedJavaProject());
    newOp.setServiceImplementationPackage(getServerJavaProject().getElementName() + ".lookupcall.output");
    newOp.setServiceImplementationProject(getServerJavaProject());
    newOp.setServiceInterfacePackageName(getSharedJavaProject().getElementName() + ".lookupcall.output");
    newOp.setServiceInterfaceProject(getSharedJavaProject());
    newOp.setServiceProxyRegistrationProject(getClientJavaProject());
    newOp.setServiceRegistrationProject(getServerJavaProject());
    newOp.setServiceSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.server.services.lookup.AbstractLookupService"));
//    TestWorkspaceUtility.executeAndBuildWorkspace(newOp);
//    System.out.println(newOp.getOutLookupCall().getSource());
//    System.out.println(newOp.getOutLookupService().getSource());
//    System.out.println(newOp.getOutLookupServiceInterface().getSource());
    executeBuildAssertNoCompileErrors(newOp);
    IType lookupCall = newOp.getOutLookupCall();
    SdkAssert.assertExist(lookupCall);
    SdkAssert.assertPublic(lookupCall).assertNoMoreFlags();
    SdkAssert.assertMethodExist(lookupCall, "getConfiguredService");
    SdkAssert.assertHasSuperType(lookupCall, RuntimeClasses.LookupCall);
    IType serviceImpl = newOp.getOutLookupService();
    SdkAssert.assertExist(serviceImpl);
    SdkAssert.assertHasSuperType(serviceImpl, RuntimeClasses.ILookupService);
    SdkAssert.assertMethodExist(serviceImpl, "getDataByAll");
    SdkAssert.assertMethodExist(serviceImpl, "getDataByKey");
    SdkAssert.assertMethodExist(serviceImpl, "getDataByText");
    SdkAssert.assertMethodExist(serviceImpl, "getDataByRec");
    IType serviceInterface = newOp.getOutLookupServiceInterface();
    SdkAssert.assertExist(serviceInterface);
    SdkAssert.assertServiceRegistered(getServerProject(), serviceImpl);
    SdkAssert.assertServiceProxyRegistered(getClientProject(), serviceInterface);
  }

  @Test
  public void testNewSqlLookupCall() throws Exception {
    LookupCallNewOperation newOp = new LookupCallNewOperation("Test02LookupCall", getSharedJavaProject().getElementName() + ".lookupcall.output", getSharedJavaProject());
    newOp.setServiceImplementationPackage(getServerJavaProject().getElementName() + ".lookupcall.output");
    newOp.setServiceImplementationProject(getServerJavaProject());
    newOp.setServiceInterfacePackageName(getSharedJavaProject().getElementName() + ".lookupcall.output");
    newOp.setServiceInterfaceProject(getSharedJavaProject());
    newOp.setServiceProxyRegistrationProject(getClientJavaProject());
    newOp.setServiceRegistrationProject(getServerJavaProject());
    newOp.setServiceSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.server.services.lookup.AbstractSqlLookupService"));
//    TestWorkspaceUtility.executeAndBuildWorkspace(newOp);
//    System.out.println(newOp.getOutLookupCall().getSource());
//    System.out.println(newOp.getOutLookupService().getSource());
//    System.out.println(newOp.getOutLookupServiceInterface().getSource());
    executeBuildAssertNoCompileErrors(newOp);
    IType lookupCall = newOp.getOutLookupCall();
    SdkAssert.assertExist(lookupCall);
    SdkAssert.assertPublic(lookupCall).assertNoMoreFlags();
    SdkAssert.assertMethodExist(lookupCall, "getConfiguredService");
    SdkAssert.assertHasSuperType(lookupCall, RuntimeClasses.LookupCall);
    IType serviceImpl = newOp.getOutLookupService();
    SdkAssert.assertExist(serviceImpl);
    SdkAssert.assertHasSuperType(serviceImpl, RuntimeClasses.ILookupService);
    SdkAssert.assertMethodExist(serviceImpl, "getConfiguredSqlSelect");
    IType serviceInterface = newOp.getOutLookupServiceInterface();
    SdkAssert.assertExist(serviceInterface);
    SdkAssert.assertServiceRegistered(getServerProject(), serviceImpl);
    SdkAssert.assertServiceProxyRegistered(getClientProject(), serviceInterface);
  }
}
