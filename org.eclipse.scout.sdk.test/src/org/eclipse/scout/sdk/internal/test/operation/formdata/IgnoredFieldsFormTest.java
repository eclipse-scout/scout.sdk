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

public class IgnoredFieldsFormTest extends AbstractScoutSdkTest {

  private IType m_formData;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("operation/formData", "formdata.shared", "formdata.client");
  }

  @Before
  public void testCreateFormData() throws Exception {
    if (m_formData == null) {
      String formName = "IgnoredFieldsForm";
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

  @Test
  public void testNotIgnoredField() throws Exception {
    // string field
    IType notIgnoredField = m_formData.getType("NotIgnored");
    Assert.assertTrue(TypeUtility.exists(notIgnoredField));
    Assert.assertEquals("QAbstractValueFieldData<QString;>;", notIgnoredField.getSuperclassTypeSignature());
    IMethod notIgnoredGetter = TypeUtility.getMethod(m_formData, "getNotIgnored");
    Assert.assertTrue(TypeUtility.exists(notIgnoredGetter));
    Assert.assertEquals(notIgnoredGetter.getReturnType(), "QNotIgnored;");
  }

  @Test
  public void testIgnoredField() throws Exception {
    IType ignoredField = m_formData.getType("IgnoredInteger");
    Assert.assertFalse(TypeUtility.exists(ignoredField));
    IMethod ignoredGetter = TypeUtility.getMethod(m_formData, "getIgnoredInteger");
    Assert.assertFalse(TypeUtility.exists(ignoredGetter));
  }

  @Test
  public void testIgnoredInheritedField() throws Exception {
    IType ignoredField = m_formData.getType("InheritedIgnored");
    Assert.assertFalse(TypeUtility.exists(ignoredField));
    IMethod ignoredGetter = TypeUtility.getMethod(m_formData, "getInheritedIgnored");
    Assert.assertFalse(TypeUtility.exists(ignoredGetter));
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    clearWorkspace();
  }
}
