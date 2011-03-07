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
import org.eclipse.jdt.core.Signature;
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

public class FormPropertiesTest extends AbstractScoutSdkTest {

  private static IType m_formData;

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
      IType form = ScoutSdk.getType("formdata.client.ui.forms.PropertyTestForm");
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
      Assert.assertEquals(m_formData.getFullyQualifiedName(), "formdata.shared.services.process.PropertyTestFormData");
      Assert.assertEquals(m_formData.getSuperclassTypeSignature(), "QAbstractFormData;");
    }
  }

  @Test
  public void testBooleanObject() throws Exception {
    String propertyname = "BoolObject";
    IType propertyType = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyType));
    Assert.assertEquals(propertyType.getSuperclassTypeSignature(), "QAbstractPropertyData<QBoolean;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "QBoolean;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "QBoolean;");
  }

  @Test
  public void testBooleanPrimitive() throws Exception {
    String propertyname = "BoolPrimitive";
    IType propertyType = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyType));
    Assert.assertEquals(propertyType.getSuperclassTypeSignature(), "QAbstractPropertyData<QBoolean;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "is" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), Signature.SIG_BOOLEAN);
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], Signature.SIG_BOOLEAN);
  }

  @Test
  public void testIntegerPrimitive() throws Exception {
    String propertyname = "IntPrimitive";
    IType simpleNrProperty = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<QInteger;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), Signature.SIG_INT);
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], Signature.SIG_INT);
  }

  @Test
  public void testByteArray() throws Exception {
    String propertyname = "ByteArray";
    IType simpleNrProperty = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<[" + Signature.SIG_BYTE + ">;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "[" + Signature.SIG_BYTE);
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "[" + Signature.SIG_BYTE);
  }

  @Test
  public void testLongProperty() throws Exception {
    String propertyname = "PropertyTestNr";
    IType simpleNrProperty = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<QLong;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "QLong;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "QLong;");
  }

  @Test
  public void testObjectProperty() throws Exception {
    String propertyname = "ObjectProperty";
    IType simpleNrProperty = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<QObject;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "QObject;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "QObject;");
  }

  @Test
  public void testHashmapProperty() throws Exception {
    String propertyname = "Wizards";
    IType simpleNrProperty = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<QHashMap<QString;QList<QIService;>;>;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "QHashMap<QString;QList<QIService;>;>;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "QHashMap<QString;QList<QIService;>;>;");
  }

  @Test
  public void testSingleArrayProperty() throws Exception {
    String propertyname = "SingleArrayProperty";
    IType simpleNrProperty = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<[QString;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "[QString;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "[QString;");
  }

  @Test
  public void testDoubleArrayProperty() throws Exception {
    String propertyname = "DoubleArrayProperty";
    IType simpleNrProperty = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<[[QString;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "[[QString;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "[[QString;");
  }

  @Test
  public void testComplexInnerArrayProperty() throws Exception {
    String propertyname = "ComplexInnerArray";
    IType simpleNrProperty = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<QArrayList<QList<[QString;>;>;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "QArrayList<QList<[QString;>;>;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "QArrayList<QList<[QString;>;>;");
  }

  @Test
  public void testComplexArrayProperty() throws Exception {
    String propertyname = "ComplexArray";
    IType simpleNrProperty = m_formData.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<[QArrayList<QList<QString;>;>;>;");
    IMethod propertyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(m_formData, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "[QArrayList<QList<QString;>;>;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(m_formData, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "[QArrayList<QList<QString;>;>;");
  }

  public void testInnerFieldProperty() throws Exception {
    IType stringField = m_formData.getType("Name");
    Assert.assertTrue(TypeUtility.exists(stringField));
    Assert.assertEquals("QAbstractValueFieldData<QString;>;", stringField.getSuperclassTypeSignature());
    IMethod stringGetter = TypeUtility.getMethod(m_formData, "getName");
    Assert.assertTrue(TypeUtility.exists(stringGetter));
    Assert.assertEquals(stringGetter.getReturnType(), "QName;");

    String propertyname = "IntProperty";
    IType simpleNrProperty = stringField.getType(propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(simpleNrProperty));
    Assert.assertEquals(simpleNrProperty.getSuperclassTypeSignature(), "QAbstractPropertyData<QInteger;>;");
    IMethod propertyGetter = TypeUtility.getMethod(stringField, "get" + propertyname + "Property");
    Assert.assertTrue(TypeUtility.exists(propertyGetter));
    Assert.assertEquals(propertyGetter.getReturnType(), "Q" + propertyname + "Property;");
    // legacy prop getter
    IMethod legacyGetter = TypeUtility.getMethod(stringField, "get" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacyGetter));
    Assert.assertEquals(legacyGetter.getReturnType(), "QInteger;");
    // legacy prop setter
    IMethod legacySetter = TypeUtility.getMethod(stringField, "set" + propertyname);
    Assert.assertTrue(TypeUtility.exists(legacySetter));
    Assert.assertEquals(legacySetter.getReturnType(), Signature.SIG_VOID);
    Assert.assertEquals(legacySetter.getParameterTypes()[0], "QInteger;");

  }
}
