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
package org.eclipse.scout.sdk.internal.test.operation.formdata;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExternalTableFieldTest extends AbstractScoutSdkTest {

  private IType m_formData;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("operation/formData", "formdata.shared", "formdata.client");
  }

  @Before
  public void testCreateFormData() throws Exception {
    if (m_formData == null) {
      String formName = "AbstractCompanyTableField";
      IType form = TypeUtility.getType("formdata.client.ui.template.formfield." + formName);
      Assert.assertTrue(TypeUtility.exists(form));

      IProject sharedProject = getProject("formdata.shared");
      Assert.assertNotNull(sharedProject);

      FormDataUpdateOperation op = new FormDataUpdateOperation(form);
      OperationJob job = new OperationJob(op);
      job.schedule();
      job.join();
      buildWorkspace();
      m_formData = op.getFormDataType();
      Assert.assertTrue(TypeUtility.exists(m_formData));
      Assert.assertTrue(TypeUtility.exists(m_formData));
      Assert.assertEquals(m_formData.getFullyQualifiedName(), "formdata.shared.services.process." + formName + "Data");
      Assert.assertEquals(m_formData.getSuperclassTypeSignature(), "QAbstractTableFieldData;");
    }
  }

  @Test
  public void testColumns() throws Exception {
    IType tableField = m_formData;
    Assert.assertTrue(TypeUtility.exists(tableField));

    IMethod setName = TypeUtility.getMethod(tableField, "setName");
    Assert.assertTrue(TypeUtility.exists(setName));
    IMethod getName = TypeUtility.getMethod(tableField, "getName");
    Assert.assertTrue(TypeUtility.exists(getName));
    Assert.assertEquals(getName.getReturnType(), "QString;");

    // column count
    IMethod columnCount = TypeUtility.getMethod(tableField, "getColumnCount");
    Assert.assertTrue(TypeUtility.exists(columnCount));
    Assert.assertTrue(columnCount.getSource().contains("return 1"));

    // setValueAt
    IMethod setValueAT = TypeUtility.getMethod(tableField, "setValueAt");
    Assert.assertTrue(TypeUtility.exists(setValueAT));
    // getValueAt
    IMethod getValueAT = TypeUtility.getMethod(tableField, "getValueAt");
    Assert.assertTrue(TypeUtility.exists(getValueAT));

  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
