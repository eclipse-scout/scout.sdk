/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.ui.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.service.IService;

import formdata.client.ui.forms.PropertyTestForm.MainBox.NameField;
import formdata.shared.services.process.PropertyTestFormData;

@FormData(value = PropertyTestFormData.class, sdkCommand = SdkCommand.CREATE)
public class PropertyTestForm extends AbstractForm {

  private Boolean m_boolObject;
  private boolean m_boolPrimitive;
  private int m_intPrimitive;
  private byte[] m_byteArray;
  private Long m_propertyTestNr;
  private Object m_objectProperty;
  private HashMap<String, List<IService>> m_wizards;
  private String[] m_singleArrayProperty;
  private String[][] m_doubleArrayProperty;
  private ArrayList<List<String[]>> m_complexInnerArray;
  private ArrayList<List<String>>[] m_complexArray;

  @FormData
  public void setBoolObject(Boolean boolObject) {
    m_boolObject = boolObject;
  }

  @FormData
  public Boolean getBoolObject() {
    return m_boolObject;
  }

  @FormData
  public void setBoolPrimitive(boolean boolPrimitive) {
    m_boolPrimitive = boolPrimitive;
  }

  @FormData
  public boolean isBoolPrimitive() {
    return m_boolPrimitive;
  }

  @FormData
  public void setIntPrimitive(int intPrimitive) {
    m_intPrimitive = intPrimitive;
  }

  @FormData
  public int getIntPrimitive() {
    return m_intPrimitive;
  }

  @FormData
  public byte[] getByteArray() {
    return m_byteArray;
  }

  @FormData
  public void setByteArray(byte[] byteArray) {
    m_byteArray = byteArray;
  }

  @FormData
  public Long getPropertyTestNr() {
    return m_propertyTestNr;
  }

  @FormData
  public void setPropertyTestNr(Long propertyTestNr) {
    this.m_propertyTestNr = propertyTestNr;
  }

  @FormData
  public Object getObjectProperty() {
    return m_objectProperty;
  }

  @FormData
  public void setObjectProperty(Object objectProperty) {
    this.m_objectProperty = objectProperty;
  }

  @FormData
  public HashMap<String, List<IService>> getWizards() {
    return m_wizards;
  }

  @FormData
  public void setWizards(HashMap<String, List<IService>> wizards) {
    m_wizards = wizards;
  }

  @FormData
  public String[] getSingleArrayProperty() {
    return m_singleArrayProperty;
  }

  @FormData
  public void setSingleArrayProperty(String[] singleArrayProperty) {
    m_singleArrayProperty = singleArrayProperty;
  }

  @FormData
  public String[][] getDoubleArrayProperty() {
    return m_doubleArrayProperty;
  }

  @FormData
  public void setDoubleArrayProperty(String[][] doubleArrayProperty) {
    m_doubleArrayProperty = doubleArrayProperty;
  }

  @FormData
  public ArrayList<List<String[]>> getComplexInnerArray() {
    return m_complexInnerArray;
  }

  @FormData
  public void setComplexInnerArray(ArrayList<List<String[]>> complexInnerArray) {
    m_complexInnerArray = complexInnerArray;
  }

  @FormData
  public ArrayList<List<String>>[] getComplexArray() {
    return m_complexArray;
  }

  @FormData
  public void setComplexArray(ArrayList<List<String>>[] complexArray) {
    m_complexArray = complexArray;
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public NameField getNameField() {
    return getFieldByClass(NameField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class NameField extends AbstractStringField {
      private int m_intProperty;

      @FormData
      public int getIntProperty() {
        return m_intProperty;
      }

      @FormData
      public void setIntProperty(int intProperty) {
        m_intProperty = intProperty;
      }
    }
  }
}
