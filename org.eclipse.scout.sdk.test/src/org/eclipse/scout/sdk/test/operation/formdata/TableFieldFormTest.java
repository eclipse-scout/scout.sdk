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
package org.eclipse.scout.sdk.test.operation.formdata;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TableFieldFormTest extends AbstractScoutSdkTest {

  private IType m_formData;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("operation/formData", "formdata.shared", "formdata.client");
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    deleteProjects("formdata.client", "formdata.shared");
  }

  @Before
  public void testCreateFormData() throws Exception {
    if (m_formData == null) {
      String formName = "TableFieldForm";
      IType form = ScoutSdk.getType("formdata.client.ui.forms." + formName);
      Assert.assertTrue(TypeUtility.exists(form));

      IProject sharedProject = getProject("formdata.shared");
      Assert.assertNotNull(sharedProject);

      FormDataUpdateOperation op = new FormDataUpdateOperation(form);
      OperationJob job = new OperationJob(op);
      job.schedule();
      job.join();
      refreshAndBuildProject(sharedProject);
      m_formData = op.getFormDataType();
      Assert.assertTrue(TypeUtility.exists(m_formData));
      Assert.assertTrue(TypeUtility.exists(m_formData));
      Assert.assertEquals(m_formData.getFullyQualifiedName(), "formdata.shared.services.process." + formName + "Data");
      Assert.assertEquals(m_formData.getSuperclassTypeSignature(), "QAbstractFormData;");
    }
  }

  @Test
  public void testTableField() throws Exception {
    IType tableField = m_formData.getType("PersonTable");
    Assert.assertTrue(TypeUtility.exists(tableField));
    Assert.assertEquals("QAbstractTableFieldData;", tableField.getSuperclassTypeSignature());
    IMethod tableFieldGetter = TypeUtility.getMethod(m_formData, "getPersonTable");
    Assert.assertTrue(TypeUtility.exists(tableFieldGetter));
    Assert.assertEquals(tableFieldGetter.getReturnType(), "QPersonTable;");
  }

  @Test
  public void testColumns() throws Exception {
    IType tableField = m_formData.getType("PersonTable");
    Assert.assertTrue(TypeUtility.exists(tableField));
    // personNr
    IMethod setPersonNr = TypeUtility.getMethod(tableField, "setPersonNr");
    Assert.assertTrue(TypeUtility.exists(setPersonNr));
    IMethod getPersonNr = TypeUtility.getMethod(tableField, "getPersonNr");
    Assert.assertTrue(TypeUtility.exists(getPersonNr));
    Assert.assertEquals(getPersonNr.getReturnType(), "QLong;");

    IMethod setName = TypeUtility.getMethod(tableField, "setName");
    Assert.assertTrue(TypeUtility.exists(setName));
    IMethod getName = TypeUtility.getMethod(tableField, "getName");
    Assert.assertTrue(TypeUtility.exists(getName));
    Assert.assertEquals(getName.getReturnType(), "QString;");

    IMethod setObject = TypeUtility.getMethod(tableField, "setAnObject");
    Assert.assertTrue(TypeUtility.exists(setObject));
    IMethod getObject = TypeUtility.getMethod(tableField, "getAnObject");
    Assert.assertTrue(TypeUtility.exists(getObject));
    Assert.assertEquals(getObject.getReturnType(), "QObject;");

    IMethod setSmartLongColumn = TypeUtility.getMethod(tableField, "setSmartLong");
    Assert.assertTrue(TypeUtility.exists(setSmartLongColumn));
    IMethod getSmartLongColumn = TypeUtility.getMethod(tableField, "getSmartLong");
    Assert.assertTrue(TypeUtility.exists(getSmartLongColumn));
    Assert.assertEquals(getSmartLongColumn.getReturnType(), "QLong;");

    IMethod setCustom = TypeUtility.getMethod(tableField, "setCustom");
    Assert.assertTrue(TypeUtility.exists(setCustom));
    IMethod getCustom = TypeUtility.getMethod(tableField, "getCustom");
    Assert.assertTrue(TypeUtility.exists(getCustom));
    Assert.assertEquals(getCustom.getReturnType(), "QSet<QMap<QString;QInteger;>;>;");

    // column count
    IMethod columnCount = TypeUtility.getMethod(tableField, "getColumnCount");
    Assert.assertTrue(TypeUtility.exists(columnCount));
    Assert.assertTrue(columnCount.getSource().contains("return 5"));

    // setValueAt
    IMethod setValueAT = TypeUtility.getMethod(tableField, "setValueAt");
    Assert.assertTrue(TypeUtility.exists(setValueAT));
    // getValueAt
    IMethod getValueAT = TypeUtility.getMethod(tableField, "getValueAt");
    Assert.assertTrue(TypeUtility.exists(getValueAT));

  }

  @Test
  public void testCompanyTableField() throws Exception {
    IType tableField = m_formData.getType("Company");
    Assert.assertTrue(TypeUtility.exists(tableField));
    Assert.assertEquals("QAbstractCompanyTableFieldData;", tableField.getSuperclassTypeSignature());
    IMethod[] methods = tableField.getMethods();
    Assert.assertEquals(methods.length, 1);
    Assert.assertTrue(methods[0].isConstructor());
  }

}
