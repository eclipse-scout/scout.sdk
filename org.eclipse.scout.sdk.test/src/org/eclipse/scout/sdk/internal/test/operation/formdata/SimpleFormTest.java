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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleFormTest extends AbstractScoutSdkTest {

  private IType m_formData;

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("operation/formData", "formdata.shared", "formdata.client");
  }

  @Before
  public void testCreateFormData() throws Exception {
    if (m_formData == null) {
      IType form = TypeUtility.getType("formdata.client.ui.forms.SimpleForm");
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
      Assert.assertEquals(m_formData.getFullyQualifiedName(), "formdata.shared.services.process.SimpleFormData");
      Assert.assertEquals(m_formData.getSuperclassTypeSignature(), "QAbstractFormData;");
    }
  }

  @Test
  public void testSimpleProperty() throws Exception {
    IType simpleNrProperty = m_formData.getType("SimpleNrProperty");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<QLong;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "getSimpleNrProperty");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "QSimpleNrProperty;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "getSimpleNr");
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "QLong;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "setSimpleNr");
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "QLong;");
  }

  @Test
  public void testFields() throws Exception {
    // string field
    IType stringField = m_formData.getType("SampleString");
    Assert.assertTrue(TypeUtility.exists(stringField));
    Assert.assertEquals("QAbstractValueFieldData<QString;>;", stringField.getSuperclassTypeSignature());
    IMethod stringGetter = TypeUtility.getMethod(m_formData, "getSampleString");
    Assert.assertTrue(TypeUtility.exists(stringGetter));
    Assert.assertEquals(stringGetter.getReturnType(), "QSampleString;");
    // double field
    IType doubleField = m_formData.getType("SampleDouble");
    Assert.assertTrue(TypeUtility.exists(doubleField));
    Assert.assertEquals("QAbstractValueFieldData<QDouble;>;", doubleField.getSuperclassTypeSignature());
    IMethod doubleGetter = TypeUtility.getMethod(m_formData, "getSampleDouble");
    Assert.assertTrue(TypeUtility.exists(doubleGetter));
    Assert.assertEquals(doubleGetter.getReturnType(), "QSampleDouble;");
    // smart field
    IType smartField = m_formData.getType("SampleSmart");
    Assert.assertTrue(TypeUtility.exists(smartField));
    Assert.assertEquals("QAbstractValueFieldData<QLong;>;", smartField.getSuperclassTypeSignature());
    IMethod smartGetter = TypeUtility.getMethod(m_formData, "getSampleSmart");
    Assert.assertTrue(TypeUtility.exists(smartGetter));
    Assert.assertEquals(smartGetter.getReturnType(), "QSampleSmart;");
    // composer field
    IType composerField = m_formData.getType("SampleComposer");
    Assert.assertTrue(TypeUtility.exists(composerField));
    Assert.assertEquals("QAbstractComposerData;", composerField.getSuperclassTypeSignature());
    IMethod composerGetter = TypeUtility.getMethod(m_formData, "getSampleComposer");
    Assert.assertTrue(TypeUtility.exists(composerGetter));
    Assert.assertEquals(composerGetter.getReturnType(), "QSampleComposer;");

  }

}
