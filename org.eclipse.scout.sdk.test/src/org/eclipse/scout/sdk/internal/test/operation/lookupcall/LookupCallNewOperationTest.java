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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.test.AbstractSdkTestWithSampleProject;
import org.eclipse.scout.sdk.operation.lookupcall.LocalLookupCallNewOperation;
import org.eclipse.scout.sdk.operation.lookupcall.LookupCallNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.junit.Test;

/**
 * <h3>{@link LookupCallNewOperationTest}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 14.03.2013
 */
public class LookupCallNewOperationTest extends AbstractSdkTestWithSampleProject {

  @Test
  public void testNewLocalLookupCall() throws Exception {
    LocalLookupCallNewOperation newOp = new LocalLookupCallNewOperation("CountryLookupCall", getSharedJavaProject().getElementName() + ".lookupcall.output", getSharedJavaProject());
    newOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.LocalLookupCall));

    executeBuildAssertNoCompileErrors(newOp);

    IType lookupCall = newOp.getCreatedType();
    SdkAssert.assertExist(lookupCall);

    testApiOfCountryLookupCall();
  }

  @Test
  public void testNewLookupCall() throws Exception {
    LookupCallNewOperation newOp = new LookupCallNewOperation("Test01LookupCall", getSharedJavaProject().getElementName() + ".lookupcall.output", getSharedJavaProject());
    newOp.setServiceImplementationPackage(getServerJavaProject().getElementName() + ".lookupcall.output");
    newOp.setServiceImplementationProject(getServerJavaProject());
    newOp.setServiceInterfacePackageName(getSharedJavaProject().getElementName() + ".lookupcall.output");
    newOp.setServiceInterfaceProject(getSharedJavaProject());
    newOp.setServiceProxyRegistrationProject(getClientJavaProject());
    newOp.addServiceRegistration(new ServiceRegistrationDescription(getServerJavaProject()));
    newOp.setServiceSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.server.services.lookup.AbstractLookupService<java.lang.Object>"));
    newOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.ILookupCall, getSharedJavaProject()) + "<java.lang.Object>");

    executeBuildAssertNoCompileErrors(newOp);

    testApiOfTest01LookupCall();
    testApiOfTest01LookupService();
    testApiOfITest01LookupService();
  }

  @Test
  public void testNewSqlLookupCall() throws Exception {
    LookupCallNewOperation newOp = new LookupCallNewOperation("Test02LookupCall", getSharedJavaProject().getElementName() + ".lookupcall.output", getSharedJavaProject());
    newOp.setServiceImplementationPackage(getServerJavaProject().getElementName() + ".lookupcall.output");
    newOp.setServiceImplementationProject(getServerJavaProject());
    newOp.setServiceInterfacePackageName(getSharedJavaProject().getElementName() + ".lookupcall.output");
    newOp.setServiceInterfaceProject(getSharedJavaProject());
    newOp.setServiceProxyRegistrationProject(getClientJavaProject());
    newOp.addServiceRegistration(new ServiceRegistrationDescription(getServerJavaProject()));
    newOp.setServiceSuperTypeSignature(SignatureCache.createTypeSignature("org.eclipse.scout.rt.server.services.lookup.AbstractSqlLookupService<java.lang.Object>"));
    newOp.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(IRuntimeClasses.ILookupCall, getSharedJavaProject()) + "<java.lang.Object>");

    executeBuildAssertNoCompileErrors(newOp);

    testApiOfTest02LookupCall();
    testApiOfTest02LookupService();
    testApiOfITest02LookupService();
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfCountryLookupCall() throws Exception {
    // type CountryLookupCall
    IType countryLookupCall = SdkAssert.assertTypeExists("sample.shared.lookupcall.output.CountryLookupCall");
    SdkAssert.assertHasFlags(countryLookupCall, 1);
    SdkAssert.assertHasSuperTypeSignature(countryLookupCall, "QLocalLookupCall;");

    // fields of CountryLookupCall
    SdkAssert.assertEquals("field count of 'CountryLookupCall'", 1, countryLookupCall.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(countryLookupCall, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'CountryLookupCall'", 1, countryLookupCall.getMethods().length);
    IMethod execCreateLookupRows = SdkAssert.assertMethodExist(countryLookupCall, "execCreateLookupRows", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(execCreateLookupRows, "QList<+QILookupRow<QObject;>;>;");
    SdkAssert.assertAnnotation(execCreateLookupRows, "java.lang.Override");

    SdkAssert.assertEquals("inner types count of 'CountryLookupCall'", 0, countryLookupCall.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTest01LookupCall() throws Exception {
    // type Test01LookupCall
    IType test01LookupCall = SdkAssert.assertTypeExists("sample.shared.lookupcall.output.Test01LookupCall");
    SdkAssert.assertHasFlags(test01LookupCall, 1);
    SdkAssert.assertHasSuperTypeSignature(test01LookupCall, "QLookupCall;");

    // fields of Test01LookupCall
    SdkAssert.assertEquals("field count of 'Test01LookupCall'", 1, test01LookupCall.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(test01LookupCall, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'Test01LookupCall'", 1, test01LookupCall.getMethods().length);
    IMethod getConfiguredService = SdkAssert.assertMethodExist(test01LookupCall, "getConfiguredService", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getConfiguredService, "QClass<+QILookupService<QObject;>;>;");
    SdkAssert.assertAnnotation(getConfiguredService, "java.lang.Override");

    SdkAssert.assertEquals("inner types count of 'Test01LookupCall'", 0, test01LookupCall.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTest01LookupService() throws Exception {
    // type Test01LookupService
    IType test01LookupService = SdkAssert.assertTypeExists("sample.server.lookupcall.output.Test01LookupService");
    SdkAssert.assertHasFlags(test01LookupService, 1);
    SdkAssert.assertHasSuperTypeSignature(test01LookupService, "QAbstractLookupService<QObject;>;");
    SdkAssert.assertHasSuperIntefaceSignatures(test01LookupService, new String[]{"QITest01LookupService;"});

    // fields of Test01LookupService
    SdkAssert.assertEquals("field count of 'Test01LookupService'", 0, test01LookupService.getFields().length);

    SdkAssert.assertEquals("method count of 'Test01LookupService'", 4, test01LookupService.getMethods().length);
    IMethod getDataByAll = SdkAssert.assertMethodExist(test01LookupService, "getDataByAll", new String[]{"QILookupCall<QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(getDataByAll, "QList<+QILookupRow<QObject;>;>;");
    SdkAssert.assertAnnotation(getDataByAll, "java.lang.Override");
    IMethod getDataByKey = SdkAssert.assertMethodExist(test01LookupService, "getDataByKey", new String[]{"QILookupCall<QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(getDataByKey, "QList<+QILookupRow<QObject;>;>;");
    SdkAssert.assertAnnotation(getDataByKey, "java.lang.Override");
    IMethod getDataByRec = SdkAssert.assertMethodExist(test01LookupService, "getDataByRec", new String[]{"QILookupCall<QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(getDataByRec, "QList<+QILookupRow<QObject;>;>;");
    SdkAssert.assertAnnotation(getDataByRec, "java.lang.Override");
    IMethod getDataByText = SdkAssert.assertMethodExist(test01LookupService, "getDataByText", new String[]{"QILookupCall<QObject;>;"});
    SdkAssert.assertMethodReturnTypeSignature(getDataByText, "QList<+QILookupRow<QObject;>;>;");
    SdkAssert.assertAnnotation(getDataByText, "java.lang.Override");

    SdkAssert.assertEquals("inner types count of 'Test01LookupService'", 0, test01LookupService.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfITest01LookupService() throws Exception {
    // type ITest01LookupService
    IType iTest01LookupService = SdkAssert.assertTypeExists("sample.shared.lookupcall.output.ITest01LookupService");
    SdkAssert.assertHasFlags(iTest01LookupService, 513);
    SdkAssert.assertHasSuperIntefaceSignatures(iTest01LookupService, new String[]{"QILookupService<QObject;>;"});

    // fields of ITest01LookupService
    SdkAssert.assertEquals("field count of 'ITest01LookupService'", 0, iTest01LookupService.getFields().length);

    SdkAssert.assertEquals("method count of 'ITest01LookupService'", 0, iTest01LookupService.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'ITest01LookupService'", 0, iTest01LookupService.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTest02LookupCall() throws Exception {
    // type Test02LookupCall
    IType test02LookupCall = SdkAssert.assertTypeExists("sample.shared.lookupcall.output.Test02LookupCall");
    SdkAssert.assertHasFlags(test02LookupCall, 1);
    SdkAssert.assertHasSuperTypeSignature(test02LookupCall, "QLookupCall;");

    // fields of Test02LookupCall
    SdkAssert.assertEquals("field count of 'Test02LookupCall'", 1, test02LookupCall.getFields().length);
    IField serialVersionUID = SdkAssert.assertFieldExist(test02LookupCall, "serialVersionUID");
    SdkAssert.assertHasFlags(serialVersionUID, 26);
    SdkAssert.assertFieldSignature(serialVersionUID, "J");

    SdkAssert.assertEquals("method count of 'Test02LookupCall'", 1, test02LookupCall.getMethods().length);
    IMethod getConfiguredService = SdkAssert.assertMethodExist(test02LookupCall, "getConfiguredService", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getConfiguredService, "QClass<+QILookupService<QObject;>;>;");
    SdkAssert.assertAnnotation(getConfiguredService, "java.lang.Override");

    SdkAssert.assertEquals("inner types count of 'Test02LookupCall'", 0, test02LookupCall.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfTest02LookupService() throws Exception {
    // type Test02LookupService
    IType test02LookupService = SdkAssert.assertTypeExists("sample.server.lookupcall.output.Test02LookupService");
    SdkAssert.assertHasFlags(test02LookupService, 1);
    SdkAssert.assertHasSuperTypeSignature(test02LookupService, "QAbstractSqlLookupService<QObject;>;");
    SdkAssert.assertHasSuperIntefaceSignatures(test02LookupService, new String[]{"QITest02LookupService;"});

    // fields of Test02LookupService
    SdkAssert.assertEquals("field count of 'Test02LookupService'", 0, test02LookupService.getFields().length);

    SdkAssert.assertEquals("method count of 'Test02LookupService'", 1, test02LookupService.getMethods().length);
    IMethod getConfiguredSqlSelect = SdkAssert.assertMethodExist(test02LookupService, "getConfiguredSqlSelect", new String[]{});
    SdkAssert.assertMethodReturnTypeSignature(getConfiguredSqlSelect, "QString;");
    SdkAssert.assertAnnotation(getConfiguredSqlSelect, "java.lang.Override");

    SdkAssert.assertEquals("inner types count of 'Test02LookupService'", 0, test02LookupService.getTypes().length);
  }

  /**
   * @Generated with org.eclipse.scout.sdk.testing.codegen.ApiTestGenerator
   */
  private void testApiOfITest02LookupService() throws Exception {
    // type ITest02LookupService
    IType iTest02LookupService = SdkAssert.assertTypeExists("sample.shared.lookupcall.output.ITest02LookupService");
    SdkAssert.assertHasFlags(iTest02LookupService, 513);
    SdkAssert.assertHasSuperIntefaceSignatures(iTest02LookupService, new String[]{"QILookupService<QObject;>;"});

    // fields of ITest02LookupService
    SdkAssert.assertEquals("field count of 'ITest02LookupService'", 0, iTest02LookupService.getFields().length);

    SdkAssert.assertEquals("method count of 'ITest02LookupService'", 0, iTest02LookupService.getMethods().length);

    SdkAssert.assertEquals("inner types count of 'ITest02LookupService'", 0, iTest02LookupService.getTypes().length);
  }
}
