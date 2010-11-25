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
package org.eclipse.scout.sdk.test.operation.form;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.internal.typecache.WorkingCopyManager;
import org.eclipse.scout.sdk.operation.form.FormDataUpdateOperation;
import org.eclipse.scout.sdk.workspace.member.IPropertyBean;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class FormDataUpdateOperationTest extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    setupWorkspace("codeGeneration/formData", "a.shared", "a.client");
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    deleteProjects("a", "a.client", "a.shared");
  }

  public IType createFormData(String formSimpleClassName) throws Exception {
    NullProgressMonitor monitor = new NullProgressMonitor();

    IType form = ScoutSdk.getType("a.client.form." + formSimpleClassName);
    Assert.assertNotNull(form);

    IProject sharedProject = getProject("a.shared");
    Assert.assertNotNull(sharedProject);

    FormDataUpdateOperation op = new FormDataUpdateOperation(form);
    WorkingCopyManager workingCopyManager = new WorkingCopyManager();
    op.run(monitor, workingCopyManager);
    workingCopyManager.unregisterAll(monitor);
    refreshAndBuildProject(sharedProject);

    IType formData = ScoutSdk.getType("a.shared.services.process." + formSimpleClassName + "Data");
    Assert.assertTrue(TypeUtility.exists(formData));

    return formData;
  }

  @After
  public void deleteFormData() throws Exception {
    IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getProject("a.shared").getFolder("src/a/shared/services/process");
    deleteAndWaitUntilDeleted(folder);
  }

  @Test
  public void testCreateFormData_textField() throws Exception {
    IType formData = createFormData("AForm");
    IType text = formData.getType("Text");
    Assert.assertTrue(TypeUtility.exists(text));
    Assert.assertEquals("QAbstractValueFieldData<QString;>;", text.getSuperclassTypeSignature());
  }

  @Test
  public void testCreateFormData_listBox() throws Exception {
    IType formData = createFormData("AForm");
    IType list = formData.getType("ListBox");
    Assert.assertTrue(TypeUtility.exists(list));
    Assert.assertEquals("QAbstractValueFieldData<[QLong;>;", list.getSuperclassTypeSignature());
  }

  @Test
  public void testCreateFormData_emptyForm() throws Exception {
    IType formData = createFormData("EmptyForm");
    Assert.assertEquals(0, formData.getTypes().length);
    Assert.assertEquals(1, formData.getMethods().length);
  }

  @Test
  public void testCreateFormData_emptyFormWithProperty() throws Exception {
    IType formData = createFormData("EmptyPropertyForm");
    // inner types
    Assert.assertEquals(4, formData.getTypes().length);
    // properties
    Assert.assertEquals(8, TypeUtility.getPropertyBeans(formData, null, null).length);
    verifyBeanProperty(formData, "Property", "Ljava.lang.String;", "getProperty", "()QString;", "setProperty", "(QString;)V");
    verifyBeanProperty(formData, "OneDimension", "[Ljava.lang.Object;", "getOneDimension", "()[QObject;", "setOneDimension", "([QObject;)V");
    verifyBeanProperty(formData, "TwoDimensions", "[[Ljava.lang.Object;", "getTwoDimensions", "()[[QObject;", "setTwoDimensions", "([[QObject;)V");
    verifyBeanProperty(formData, "ThreeDimensions", "[[[Ljava.lang.Object;", "getThreeDimensions", "()[[[QObject;", "setThreeDimensions", "([[[QObject;)V");
    Assert.assertEquals(13, formData.getMethods().length);
  }

  @Test
  public void testCreateFormData_textFieldForm() throws Exception {
    IType formData = createFormData("TextFieldForm");
    // inner types
    Assert.assertEquals(1, formData.getTypes().length);
    IType text = TypeUtility.findInnerType(formData, "Text");
    Assert.assertNotNull(text);
    Assert.assertEquals("QAbstractValueFieldData<QString;>;", text.getSuperclassTypeSignature());
  }

  @Test
  public void testCreateFormData_textFieldFormWithProperties() throws Exception {
    IType formData = createFormData("TextFieldPropertyForm");
    // inner types
    Assert.assertEquals(1, formData.getTypes().length);
    IType text = TypeUtility.findInnerType(formData, "Text");
    Assert.assertNotNull(text);
    Assert.assertEquals("QAbstractValueFieldData<QString;>;", text.getSuperclassTypeSignature());
    // field properties
    Assert.assertEquals(2, TypeUtility.getPropertyBeans(text, null, null).length);
    verifyBeanProperty(text, "Property", "Ljava.lang.String;", "getProperty", "()QString;", "setProperty", "(QString;)V");
  }

  @Test
  public void testCreateFormData_tableFieldForm() throws Exception {
    IType formData = createFormData("TableFieldForm");
    // inner types
    Assert.assertEquals(1, formData.getTypes().length);
    IType tableData = verifyInnerType(formData, "Table", "QAbstractTableFieldData;");
    // columns
    verifyTableDataColumn(tableData, "String", "QString;");
    verifyTableDataColumn(tableData, "Integer", "QInteger;");
    // TODO abr: the methods are not correctly parsed by Eclipse JDT?!
//    verifyTableDataColumn(tableData, "Long", "QLong;");
//    verifyTableDataColumn(tableData, "Double", "QDouble;");
//    verifyTableDataColumn(tableData, "Date", "QDate;");
    verifyTableDataColumn(tableData, "BigDecimal", "QBigDecimal;");
//    verifyTableDataColumn(tableData, "Boolean", "QBoolean;");
//    verifyTableDataColumn(tableData, "Time", "QTime;");
    verifyTableDataColumn(tableData, "Object", "QObject;");
    verifyTableDataColumn(tableData, "SmartLong", "QLong;");
    verifyTableDataColumn(tableData, "SmartCustom", "QSet<QMap<QString;QInteger;>;>;");
  }

  @Test
  public void testCreateFormData_tableFieldFormWithProperties() throws Exception {
    IType formData = createFormData("TableFieldPropertyForm");
    // inner types
    Assert.assertEquals(1, formData.getTypes().length);
    verifyInnerType(formData, "Table", "QAbstractTableFieldData;");
    IType table = TypeUtility.findInnerType(formData, "Table");
    verifyBeanProperty(table, "TableFieldProperty", "Ljava.lang.String;", "getTableFieldProperty", "()QString;", "setTableFieldProperty", "(QString;)V");
  }

  @Test
  public void testCreateFormData_emptyFormWithPrimitiveProperties() throws Exception {
    IType formData = createFormData("EmptyPrimitivePropertyForm");
    // inner types
    Assert.assertEquals(9, formData.getTypes().length);
  }

  @Test
  public void testCreateFormData_templateFileds() throws Exception {
    IType formData = createFormData("TemplateFieldForm");
    // inner types
    Assert.assertEquals(4, formData.getTypes().length);

    // check template string field data
    IType stringFieldData = verifyInnerType(formData, "TemplateString", "QAbstractTemplateStringFieldData;");
    IType stringFieldDataSuperType = ScoutSdk.getTypeBySignature(stringFieldData.getSuperclassTypeSignature());
    Assert.assertNotNull(stringFieldDataSuperType);
    Assert.assertEquals("QAbstractValueFieldData<QString;>;", stringFieldDataSuperType.getSuperclassTypeSignature());

    // check template box data
    IType templateBoxData = verifyInnerType(formData, "TemplateBox", "QAbstractTemplateBoxData;");
    IType templateBoxDataSuperType = ScoutSdk.getTypeBySignature(templateBoxData.getSuperclassTypeSignature());
    Assert.assertNotNull(templateBoxDataSuperType);
    Assert.assertEquals("QAbstractFormFieldData;", templateBoxDataSuperType.getSuperclassTypeSignature());
    // check template box data inner types
    Assert.assertEquals(1, templateBoxDataSuperType.getTypes().length);
    verifyInnerType(templateBoxDataSuperType, "MyString", "QAbstractValueFieldData<QString;>;");

    // check template table field data
    IType templateTableFieldData = verifyInnerType(formData, "TemplateTable", "QAbstractTemplateTableFieldData;");
    IType templateTableFieldSuperType = ScoutSdk.getTypeBySignature(templateTableFieldData.getSuperclassTypeSignature());
    Assert.assertNotNull(templateTableFieldSuperType);
    Assert.assertEquals("QAbstractTableFieldData;", templateTableFieldSuperType.getSuperclassTypeSignature());
    // check template table columns
    verifyTableDataColumn(templateTableFieldSuperType, "First", "QString;");
    verifyTableDataColumn(templateTableFieldSuperType, "Second", "QLong;");

    // check external table template table field data
    IType externalTableTemplateTableFieldData = verifyInnerType(formData, "ExternalTableTemplateTable", "QAbstractExternalTableTemplateTableFieldData;");
    IType externalTableTemplateTableFieldSuperType = ScoutSdk.getTypeBySignature(externalTableTemplateTableFieldData.getSuperclassTypeSignature());
    Assert.assertNotNull(externalTableTemplateTableFieldSuperType);
    Assert.assertEquals("QAbstractTableFieldData;", externalTableTemplateTableFieldSuperType.getSuperclassTypeSignature());
    // check external table template table columns
    verifyTableDataColumn(templateTableFieldSuperType, "FirstExternal", "QString;");
    verifyTableDataColumn(templateTableFieldSuperType, "SecondExternal", "QLong;");
  }

  /**
   * Verifies the availability and correctness of the given inner type within the given type.
   */
  private IType verifyInnerType(IType type, String innerTypeName, String innerTypeSuperSignature) throws JavaModelException {
    IType innerType = TypeUtility.findInnerType(type, innerTypeName);
    Assert.assertNotNull(innerType);
    Assert.assertEquals(innerTypeSuperSignature, innerType.getSuperclassTypeSignature());
    return innerType;
  }

  /**
   * Verifies the availability and correctness of the given property within the given type.
   */
  @SuppressWarnings("null")
  private void verifyBeanProperty(IType type, Object beanName, String beanSignature, String getterName, String getterSignature, String setterName, String setterSignature) throws JavaModelException {
    IPropertyBean property = null;
    for (IPropertyBean p : TypeUtility.getPropertyBeans(type, null, null)) {
      if (beanName.equals(p.getBeanName())) {
        property = p;
        break;
      }
    }
    Assert.assertNotNull(property);
    Assert.assertEquals(beanSignature, property.getBeanSignature());

    IMethod readMethod = property.getReadMethod();
    Assert.assertNotNull(readMethod);
    Assert.assertEquals(getterName, readMethod.getElementName());
    Assert.assertEquals(getterSignature, readMethod.getSignature());

    IMethod writeMethod = property.getWriteMethod();
    Assert.assertNotNull(writeMethod);
    Assert.assertEquals(setterName, writeMethod.getElementName());
    Assert.assertEquals(setterSignature, writeMethod.getSignature());
  }

  @SuppressWarnings("null")
  private void verifyTableDataColumn(IType tableType, String columnName, String columnTypeSignature) throws Exception {
    IMethod getter = null;
    IMethod setter = null;
    for (IMethod method : tableType.getMethods()) {
      if (("get" + columnName).equals(method.getElementName())) {
        getter = method;
      }
      else if (("set" + columnName).equals(method.getElementName())) {
        setter = method;
      }
      if (getter != null && setter != null) {
        break;
      }
    }
    Assert.assertNotNull("column getter is null", getter);
    Assert.assertNotNull("column setter is null", setter);

    Assert.assertEquals("unexpected getter return type", columnTypeSignature, getter.getReturnType());
    Assert.assertArrayEquals("unexpected getter parameter types", new String[]{"I"}, getter.getParameterTypes());

    Assert.assertEquals("unexpected setter return type", "V", setter.getReturnType());
    Assert.assertArrayEquals("unexpected setter parameter types", new String[]{"I", columnTypeSignature}, setter.getParameterTypes());
  }
}
