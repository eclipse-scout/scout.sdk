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

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.service.ServiceMethod;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.junit.Test;

/**
 * <h3>{@link ServiceNewOperationTest}</h3> ...
 *
 *  @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class ServiceNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewService() throws Exception {
    ServiceNewOperation serviceOp = new ServiceNewOperation("ITestService01", "TestService01");
    serviceOp.setImplementationProject(getServerJavaProject());
    serviceOp.setImplementationPackageName("sample.server.services.test.output");
    serviceOp.setInterfaceProject(getSharedJavaProject());
    serviceOp.setInterfacePackageName("sample.shared.services.test.output");
    serviceOp.setImplementationSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService, getServerJavaProject()));

    executeBuildAssertNoCompileErrors(serviceOp);
    SdkAssert.assertExist(serviceOp.getCreatedServiceInterface());
    SdkAssert.assertExist(serviceOp.getCreatedServiceImplementation());
    SdkAssert.assertEquals(serviceOp.getCreatedServiceInterface().getElementName(), serviceOp.getCreatedServiceImplementation().getSuperInterfaceNames()[0]);
  }

  @Test
  public void testNewServiceWithoutInterface() throws Exception {
    ServiceNewOperation serviceOp = new ServiceNewOperation(null, "TestService02");
    serviceOp.setImplementationProject(getServerJavaProject());
    serviceOp.setImplementationPackageName("sample.server.services.test.output");
    serviceOp.setImplementationSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService, getServerJavaProject()));
    executeBuildAssertNoCompileErrors(serviceOp);
    SdkAssert.assertNotExist(serviceOp.getCreatedServiceInterface());
    SdkAssert.assertExist(serviceOp.getCreatedServiceImplementation());
  }

  @Test
  public void testNewServiceWithRegistrations() throws Exception {
    ServiceNewOperation serviceOp = new ServiceNewOperation("ITestService03", "TestService03");
    serviceOp.setImplementationProject(getServerJavaProject());
    serviceOp.setImplementationPackageName("sample.server.services.test.output");
    serviceOp.addServiceRegistrationProject(getServerJavaProject());
    serviceOp.setInterfaceProject(getSharedJavaProject());
    serviceOp.setInterfacePackageName("sample.shared.services.test.output");
    serviceOp.addProxyRegistrationProject(getClientJavaProject());
    serviceOp.setImplementationSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService, getServerJavaProject()));

    executeBuildAssertNoCompileErrors(serviceOp);
    IType serviceInterface = serviceOp.getCreatedServiceInterface();
    SdkAssert.assertExist(serviceInterface);
    IType serviceImplementation = serviceOp.getCreatedServiceImplementation();
    SdkAssert.assertExist(serviceImplementation);
    SdkAssert.assertEquals(serviceInterface.getElementName(), serviceImplementation.getSuperInterfaceNames()[0]);
    SdkAssert.assertServiceProxyRegistered(getClientProject(), serviceInterface);
    SdkAssert.assertServiceRegistered(getServerProject(), serviceImplementation);
  }

  @Test
  public void testNewServiceWithMethod() throws Exception {
    ServiceNewOperation serviceOp = new ServiceNewOperation("ITestService04", "TestService04");

    serviceOp.setImplementationProject(getServerJavaProject());
    serviceOp.setImplementationPackageName("sample.server.services.test.output");
    serviceOp.setInterfaceProject(getSharedJavaProject());
    serviceOp.setInterfacePackageName("sample.shared.services.test.output");
    serviceOp.setImplementationSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.IService, getServerJavaProject()));

    String methodName = "doStuff";
    ServiceMethod method = new ServiceMethod(methodName, serviceOp.getInterfacePackageName() + "." + serviceOp.getInterfaceName());
    method.setReturnTypeSignature(Signature.createTypeSignature(List.class.getName() + "<" + File.class.getName() + ">", true));
    method.addParameter(new MethodParameter("param01", Signature.createTypeSignature(String.class.getName(), true)));
    method.addParameter(new MethodParameter("param02", Signature.createTypeSignature(Long.class.getName(), true)));
    method.addExceptionSignature(Signature.createTypeSignature(RuntimeClasses.ProcessingException, true));
    method.addExceptionSignature(Signature.createTypeSignature(IllegalArgumentException.class.getName(), true));
    serviceOp.addServiceMethodBuilder(method);

    executeBuildAssertNoCompileErrors(serviceOp);
    IType serviceInterface = serviceOp.getCreatedServiceInterface();
    SdkAssert.assertExist(serviceInterface);
    IType serviceImplementation = serviceOp.getCreatedServiceImplementation();
    SdkAssert.assertExist(serviceImplementation);
    SdkAssert.assertEquals(serviceInterface.getElementName(), serviceImplementation.getSuperInterfaceNames()[0]);

    // method interface
    IMethod interfaceMethod = SdkAssert.assertMethodExist(serviceInterface, methodName);
    SdkAssert.assertPublic(interfaceMethod).assertNoMoreFlags();
    SdkAssert.assertEquals(2, interfaceMethod.getParameterTypes().length);
    SdkAssert.assertEquals("QString;", interfaceMethod.getParameterTypes()[0]);
    SdkAssert.assertEquals("QLong;", interfaceMethod.getParameterTypes()[1]);
    SdkAssert.assertEquals(2, interfaceMethod.getExceptionTypes().length);
    SdkAssert.assertEquals("QProcessingException;", interfaceMethod.getExceptionTypes()[0]);
    SdkAssert.assertEquals("QIllegalArgumentException;", interfaceMethod.getExceptionTypes()[1]);

    // method implementation
    IMethod implementationMethod = SdkAssert.assertMethodExist(serviceImplementation, methodName);
    SdkAssert.assertPublic(implementationMethod).assertNoMoreFlags();
    SdkAssert.assertEquals(2, implementationMethod.getParameterTypes().length);
    SdkAssert.assertEquals("QString;", implementationMethod.getParameterTypes()[0]);
    SdkAssert.assertEquals("QLong;", implementationMethod.getParameterTypes()[1]);
    SdkAssert.assertEquals(2, implementationMethod.getExceptionTypes().length);
    SdkAssert.assertEquals("QProcessingException;", implementationMethod.getExceptionTypes()[0]);
    SdkAssert.assertEquals("QIllegalArgumentException;", implementationMethod.getExceptionTypes()[1]);

  }
}
