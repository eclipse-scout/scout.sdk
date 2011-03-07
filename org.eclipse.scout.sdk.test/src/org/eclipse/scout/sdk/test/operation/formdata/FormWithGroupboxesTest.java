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

public class FormWithGroupboxesTest extends AbstractScoutSdkTest {

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
      String formName = "FormWithGroupBoxesForm";
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

  public void testIgnoreGroupBox() throws Exception {
    IType stringField = m_formData.getType("InnerBox");
    Assert.assertFalse(TypeUtility.exists(stringField));
  }

  @Test
  public void testFlatField() throws Exception {
    // string field
    IType stringField = m_formData.getType("FlatString");
    Assert.assertTrue(TypeUtility.exists(stringField));
    Assert.assertEquals("QAbstractValueFieldData<QString;>;", stringField.getSuperclassTypeSignature());
    IMethod stringGetter = TypeUtility.getMethod(m_formData, "getFlatString");
    Assert.assertTrue(TypeUtility.exists(stringGetter));
    Assert.assertEquals(stringGetter.getReturnType(), "QFlatString;");
  }

  @Test
  public void testInnerFieldField() throws Exception {
    // string field
    IType stringField = m_formData.getType("InnerInteger");
    Assert.assertTrue(TypeUtility.exists(stringField));
    Assert.assertEquals("QAbstractValueFieldData<QInteger;>;", stringField.getSuperclassTypeSignature());
    IMethod stringGetter = TypeUtility.getMethod(m_formData, "getInnerInteger");
    Assert.assertTrue(TypeUtility.exists(stringGetter));
    Assert.assertEquals(stringGetter.getReturnType(), "QInnerInteger;");
  }

  @Test
  public void testIgnoredIntegerFieldField() throws Exception {
    IType ignoredField = m_formData.getType("IgnoredInteger");
    Assert.assertFalse(TypeUtility.exists(ignoredField));
    IMethod ignoredGetter = TypeUtility.getMethod(m_formData, "getIgnoredInteger");
    Assert.assertFalse(TypeUtility.exists(ignoredGetter));

  }

}
