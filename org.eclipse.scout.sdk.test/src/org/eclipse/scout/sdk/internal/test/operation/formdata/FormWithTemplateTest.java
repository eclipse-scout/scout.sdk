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
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FormWithTemplateTest extends AbstractScoutSdkTest {

  private IType m_formData;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("operation/formData", "formdata.shared", "formdata.client");
  }

  @Before
  public void testCreateFormData() throws Exception {
    if (m_formData == null) {
      String formName = "UsingTemplateForm";
      IType form = TypeUtility.getType("formdata.client.ui.forms." + formName);
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
      Assert.assertEquals(m_formData.getSuperclassTypeSignature(), "QAbstractFormData;");
    }
  }

  public void testIgnoreGroupBox() throws Exception {
    IType stringField = m_formData.getType("InnerBox");
    Assert.assertFalse(TypeUtility.exists(stringField));
  }

  @Test
  public void testInternalField() throws Exception {
    // string field
    IType stringField = m_formData.getType("InternalHtml");
    Assert.assertTrue(TypeUtility.exists(stringField));
    Assert.assertEquals("QAbstractValueFieldData<QString;>;", stringField.getSuperclassTypeSignature());
    IMethod stringGetter = TypeUtility.getMethod(m_formData, "getInternalHtml");
    Assert.assertTrue(TypeUtility.exists(stringGetter));
    Assert.assertEquals(stringGetter.getReturnType(), "QInternalHtml;");
  }

  @Test
  public void testExternalGroup() throws Exception {
    // string field
    IType stringField = m_formData.getType("ExternalGroupBox");
    Assert.assertTrue(TypeUtility.exists(stringField));
    Assert.assertEquals("QAbstractExternalGroupBoxData;", stringField.getSuperclassTypeSignature());
    IMethod stringGetter = TypeUtility.getMethod(m_formData, "getExternalGroupBox");
    Assert.assertTrue(TypeUtility.exists(stringGetter));
    Assert.assertEquals(stringGetter.getReturnType(), "QExternalGroupBox;");
  }

  @Test
  public void testIgnoredTemplateField() throws Exception {
    IType ignoredField = m_formData.getType("ExternalString");
    Assert.assertFalse(TypeUtility.exists(ignoredField));
    IMethod ignoredGetter = TypeUtility.getMethod(m_formData, "getExternalString");
    Assert.assertFalse(TypeUtility.exists(ignoredGetter));

  }

  @Test
  public void testExternalWithNoAnnotation() throws Exception {
    IType stringField = m_formData.getType("Name");
    Assert.assertFalse(TypeUtility.exists(stringField));

    IMethod stringGetter = TypeUtility.getMethod(m_formData, "getName");
    Assert.assertFalse(TypeUtility.exists(stringGetter));

    IType plzField = m_formData.getType("Plz");
    Assert.assertFalse(TypeUtility.exists(plzField));

    IMethod plzGetter = TypeUtility.getMethod(m_formData, "getPlz");
    Assert.assertFalse(TypeUtility.exists(plzGetter));
  }

  @Test
  public void testExternalCheckboxField() throws Exception {
    // string field
    IType fieldData = m_formData.getType("TestCheckbox");
    Assert.assertTrue(TypeUtility.exists(fieldData));
    Assert.assertEquals("QAbstractTestCheckboxFieldData;", fieldData.getSuperclassTypeSignature());
    IMethod stringGetter = TypeUtility.getMethod(m_formData, "getTestCheckbox");
    Assert.assertTrue(TypeUtility.exists(stringGetter));
    Assert.assertEquals(stringGetter.getReturnType(), "QTestCheckbox;");
  }

}
