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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.service.CalendarServiceNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.junit.Test;

/**
 * <h3>{@link CalendarServiceNewOperationTest}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 14.03.2013
 */
public class CalendarServiceNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewSqlLookupService() throws Exception {
    CalendarServiceNewOperation serviceOp = new CalendarServiceNewOperation("ITestService01", "TestService01");
    serviceOp.setImplementationProject(getServerJavaProject());
    serviceOp.setImplementationPackageName("sample.server.services.test.output");
    serviceOp.setInterfaceProject(getSharedJavaProject());
    serviceOp.setInterfacePackageName("sample.shared.services.test.output");
    serviceOp.setImplementationSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService2, getServerJavaProject()));
    serviceOp.addInterfaceInterfaceSignature(SignatureCache.createTypeSignature(RuntimeClasses.ICalendarService));

    executeBuildAssertNoCompileErrors(serviceOp);
    IType interfaceType = serviceOp.getCreatedServiceInterface();
    IType implementationType = serviceOp.getCreatedServiceImplementation();
    SdkAssert.assertExist(interfaceType);
    SdkAssert.assertMethodExist(interfaceType, "getItems");
    SdkAssert.assertMethodExist(interfaceType, "storeItems");
    SdkAssert.assertEquals(2, interfaceType.getMethods().length);
    SdkAssert.assertExist(implementationType);
    SdkAssert.assertEquals(interfaceType.getElementName(), implementationType.getSuperInterfaceNames()[0]);
    IMethod getItemsMethod = SdkAssert.assertMethodExist(implementationType, "getItems");
    SdkAssert.assertMethodExist(implementationType, "storeItems");
    SdkAssert.assertEquals(2, implementationType.getMethods().length);
    SdkAssert.assertEquals("[QICalendarItem;", getItemsMethod.getReturnType());
  }

}
