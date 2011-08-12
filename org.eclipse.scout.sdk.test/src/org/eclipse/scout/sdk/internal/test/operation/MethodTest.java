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

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <h1>Bug 77'596</h1>
 * <p>
 */
@Ignore
public class MethodTest extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("operation/method", "test.client", "test.shared");
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    deleteProjects("test.client", "test.shared");
  }

  @Test
  public void overrideFormExecInitForm() throws Exception {

    IType testForm = ScoutSdk.getType("test.client.ui.forms.Test1Form");

    Assert.assertTrue(TypeUtility.exists(testForm));
    // execDataChanged
    String methodName = "execDataChanged";
    ConfigurationMethod method = SdkTypeUtility.getConfigurationMethod(testForm, methodName);
    Assert.assertFalse(method.isImplemented());
    MethodOverrideOperation op = new MethodOverrideOperation(method.getType(), methodName, true);
    op.setSibling(SdkTypeUtility.createStructuredForm(testForm).getSiblingMethodConfigExec(methodName));
    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();
    IMethod implMethod = TypeUtility.getMethod(testForm, methodName);
    Assert.assertTrue(TypeUtility.exists(implMethod));
    // execInitForm
    methodName = "execValidate";
    method = SdkTypeUtility.getConfigurationMethod(testForm, methodName);
    Assert.assertFalse(method.isImplemented());
    op = new MethodOverrideOperation(method.getType(), methodName, true);
    op.setSibling(SdkTypeUtility.createStructuredForm(testForm).getSiblingMethodConfigExec(methodName));
    job = new OperationJob(op);
    job.schedule();
    job.join();
    implMethod = TypeUtility.getMethod(testForm, methodName);
    Assert.assertTrue(TypeUtility.exists(implMethod));
    // getconfiguredMinimized
    methodName = "getConfiguredMinimized";
    method = SdkTypeUtility.getConfigurationMethod(testForm, methodName);
    Assert.assertFalse(method.isImplemented());
    op = new MethodOverrideOperation(method.getType(), methodName, true);
    op.setSibling(SdkTypeUtility.createStructuredForm(testForm).getSiblingMethodConfigGetConfigured(methodName));
    op.setSimpleBody("return true;");
    job = new OperationJob(op);
    job.schedule();
    job.join();
    implMethod = TypeUtility.getMethod(testForm, methodName);
    Assert.assertTrue(TypeUtility.exists(implMethod));
    // execInitForm
    methodName = "execInitForm";
    method = SdkTypeUtility.getConfigurationMethod(testForm, methodName);
    Assert.assertFalse(method.isImplemented());
    op = new MethodOverrideOperation(method.getType(), methodName, true);
    op.setSibling(SdkTypeUtility.createStructuredForm(testForm).getSiblingMethodConfigExec(methodName));
    job = new OperationJob(op);
    job.schedule();
    job.join();
    implMethod = TypeUtility.getMethod(testForm, methodName);
    Assert.assertTrue(TypeUtility.exists(implMethod));

    InputStream refIs = getInputStream("operation/method/formReferences/Test1FormWithExecInitForm.java");
    IFile orig = (IFile) testForm.getCompilationUnit().getResource();
    Assert.assertTrue(equalContents(refIs, orig.getContents()));
  }

  //@Test
  public void testOverrideFormExecMethods() throws Exception {
    IType testForm = ScoutSdk.getType("test.client.ui.forms.Test2Form");
    // stringfield
    IType stringField = ScoutSdk.getType("test.client.ui.forms.Test2Form.MainBox.StringField");
    Assert.assertNotNull(stringField);
    overrideExecMethod(stringField, "execValidateValue");
    overrideExecMethod(stringField, "execParseValue");

    // stringfield
    IType smartField = ScoutSdk.getType("test.client.ui.forms.Test2Form.MainBox.SmartField");
    Assert.assertNotNull(smartField);
    overrideExecMethod(smartField, "execParseValue");
    overrideExecMethod(smartField, "execDataChanged");
    overrideExecMethod(smartField, "execChangedValue");
    overrideExecMethod(smartField, "execFormatValue");

    // listbox
    IType listBoxField = ScoutSdk.getType("test.client.ui.forms.Test2Form.MainBox.ListboxField");
    Assert.assertNotNull(listBoxField);
    overrideExecMethod(listBoxField, "execFormatValue");
    overrideExecMethod(listBoxField, "execValidateValue");

    // tablefield
    IType tableField = ScoutSdk.getType("test.client.ui.forms.Test2Form.MainBox.TableField");
    Assert.assertNotNull(tableField);
    overrideExecMethod(tableField, "execDataChanged");
    overrideExecMethod(tableField, "execSaveUpdatedRow");
    // tablefield.table
    IType tableFieldTable = ScoutSdk.getType("test.client.ui.forms.Test2Form.MainBox.TableField.Table");
    Assert.assertNotNull(tableFieldTable);
    overrideExecMethod(tableFieldTable, "execRowClick");
    overrideExecMethod(tableFieldTable, "execDecorateCell");

    InputStream refIs = getInputStream("operation/method/formReferences/Test2FormWithExecs.java");
    IFile orig = (IFile) testForm.getCompilationUnit().getResource();
    Assert.assertTrue(equalContents(refIs, orig.getContents()));
  }

  private void overrideExecMethod(IType declaringType, String name) throws Exception {
    ConfigurationMethod configMethod = SdkTypeUtility.getConfigurationMethod(declaringType, name);
    Assert.assertFalse(configMethod.isImplemented());

    MethodOverrideOperation op = new MethodOverrideOperation(configMethod.getType(), configMethod.getMethodName(), true);
    IStructuredType structuredFormField = SdkTypeUtility.createStructuredFormField(declaringType);
    op.setSibling(structuredFormField.getSiblingMethodConfigExec(configMethod.getMethodName()));
    OperationJob job = new OperationJob(op);
    job.schedule();
    job.join();
    IMethod implMethod = TypeUtility.getMethod(declaringType, name);
    Assert.assertTrue(TypeUtility.exists(implMethod));
  }

}
