/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.services.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.forms.PropertyTestForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class PropertyTestFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  /**
   * access method for property BoolObject.
   */
  public Boolean getBoolObject() {
    return getBoolObjectProperty().getValue();
  }

  /**
   * access method for property BoolObject.
   */
  public void setBoolObject(Boolean boolObject) {
    getBoolObjectProperty().setValue(boolObject);
  }

  public BoolObjectProperty getBoolObjectProperty() {
    return getPropertyByClass(BoolObjectProperty.class);
  }

  /**
   * access method for property BoolPrimitive.
   */
  public boolean isBoolPrimitive() {
    return getBoolPrimitiveProperty().getValue() == null ? false : getBoolPrimitiveProperty().getValue();
  }

  /**
   * access method for property BoolPrimitive.
   */
  public void setBoolPrimitive(boolean boolPrimitive) {
    getBoolPrimitiveProperty().setValue(boolPrimitive);
  }

  public BoolPrimitiveProperty getBoolPrimitiveProperty() {
    return getPropertyByClass(BoolPrimitiveProperty.class);
  }

  /**
   * access method for property ByteArray.
   */
  public byte[] getByteArray() {
    return getByteArrayProperty().getValue();
  }

  /**
   * access method for property ByteArray.
   */
  public void setByteArray(byte[] byteArray) {
    getByteArrayProperty().setValue(byteArray);
  }

  public ByteArrayProperty getByteArrayProperty() {
    return getPropertyByClass(ByteArrayProperty.class);
  }

  /**
   * access method for property ComplexArray.
   */
  public ArrayList<List<String>>[] getComplexArray() {
    return getComplexArrayProperty().getValue();
  }

  /**
   * access method for property ComplexArray.
   */
  public void setComplexArray(ArrayList<List<String>>[] complexArray) {
    getComplexArrayProperty().setValue(complexArray);
  }

  public ComplexArrayProperty getComplexArrayProperty() {
    return getPropertyByClass(ComplexArrayProperty.class);
  }

  /**
   * access method for property ComplexInnerArray.
   */
  public ArrayList<List<String[]>> getComplexInnerArray() {
    return getComplexInnerArrayProperty().getValue();
  }

  /**
   * access method for property ComplexInnerArray.
   */
  public void setComplexInnerArray(ArrayList<List<String[]>> complexInnerArray) {
    getComplexInnerArrayProperty().setValue(complexInnerArray);
  }

  public ComplexInnerArrayProperty getComplexInnerArrayProperty() {
    return getPropertyByClass(ComplexInnerArrayProperty.class);
  }

  /**
   * access method for property DoubleArrayProperty.
   */
  public String[][] getDoubleArrayProperty() {
    return getDoubleArrayPropertyProperty().getValue();
  }

  /**
   * access method for property DoubleArrayProperty.
   */
  public void setDoubleArrayProperty(String[][] doubleArrayProperty) {
    getDoubleArrayPropertyProperty().setValue(doubleArrayProperty);
  }

  public DoubleArrayPropertyProperty getDoubleArrayPropertyProperty() {
    return getPropertyByClass(DoubleArrayPropertyProperty.class);
  }

  /**
   * access method for property IntPrimitive.
   */
  public int getIntPrimitive() {
    return getIntPrimitiveProperty().getValue() == null ? 0 : getIntPrimitiveProperty().getValue();
  }

  /**
   * access method for property IntPrimitive.
   */
  public void setIntPrimitive(int intPrimitive) {
    getIntPrimitiveProperty().setValue(intPrimitive);
  }

  public IntPrimitiveProperty getIntPrimitiveProperty() {
    return getPropertyByClass(IntPrimitiveProperty.class);
  }

  public Name getName() {
    return getFieldByClass(Name.class);
  }

  /**
   * access method for property ObjectProperty.
   */
  public Object getObjectProperty() {
    return getObjectPropertyProperty().getValue();
  }

  /**
   * access method for property ObjectProperty.
   */
  public void setObjectProperty(Object objectProperty) {
    getObjectPropertyProperty().setValue(objectProperty);
  }

  public ObjectPropertyProperty getObjectPropertyProperty() {
    return getPropertyByClass(ObjectPropertyProperty.class);
  }

  /**
   * access method for property PropertyTestNr.
   */
  public Long getPropertyTestNr() {
    return getPropertyTestNrProperty().getValue();
  }

  /**
   * access method for property PropertyTestNr.
   */
  public void setPropertyTestNr(Long propertyTestNr) {
    getPropertyTestNrProperty().setValue(propertyTestNr);
  }

  public PropertyTestNrProperty getPropertyTestNrProperty() {
    return getPropertyByClass(PropertyTestNrProperty.class);
  }

  /**
   * access method for property SingleArrayProperty.
   */
  public String[] getSingleArrayProperty() {
    return getSingleArrayPropertyProperty().getValue();
  }

  /**
   * access method for property SingleArrayProperty.
   */
  public void setSingleArrayProperty(String[] singleArrayProperty) {
    getSingleArrayPropertyProperty().setValue(singleArrayProperty);
  }

  public SingleArrayPropertyProperty getSingleArrayPropertyProperty() {
    return getPropertyByClass(SingleArrayPropertyProperty.class);
  }

  /**
   * access method for property Wizards.
   */
  public HashMap<String, List<IService>> getWizards() {
    return getWizardsProperty().getValue();
  }

  /**
   * access method for property Wizards.
   */
  public void setWizards(HashMap<String, List<IService>> wizards) {
    getWizardsProperty().setValue(wizards);
  }

  public WizardsProperty getWizardsProperty() {
    return getPropertyByClass(WizardsProperty.class);
  }

  public static class BoolObjectProperty extends AbstractPropertyData<Boolean> {

    private static final long serialVersionUID = 1L;
  }

  public static class BoolPrimitiveProperty extends AbstractPropertyData<Boolean> {

    private static final long serialVersionUID = 1L;
  }

  public static class ByteArrayProperty extends AbstractPropertyData<byte[]> {

    private static final long serialVersionUID = 1L;
  }

  public static class ComplexArrayProperty extends AbstractPropertyData<ArrayList<List<String>>[]> {

    private static final long serialVersionUID = 1L;
  }

  public static class ComplexInnerArrayProperty extends AbstractPropertyData<ArrayList<List<String[]>>> {

    private static final long serialVersionUID = 1L;
  }

  public static class DoubleArrayPropertyProperty extends AbstractPropertyData<String[][]> {

    private static final long serialVersionUID = 1L;
  }

  public static class IntPrimitiveProperty extends AbstractPropertyData<Integer> {

    private static final long serialVersionUID = 1L;
  }

  public static class Name extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    /**
     * access method for property IntProperty.
     */
    public int getIntProperty() {
      return getIntPropertyProperty().getValue() == null ? 0 : getIntPropertyProperty().getValue();
    }

    /**
     * access method for property IntProperty.
     */
    public void setIntProperty(int intProperty) {
      getIntPropertyProperty().setValue(intProperty);
    }

    public IntPropertyProperty getIntPropertyProperty() {
      return getPropertyByClass(IntPropertyProperty.class);
    }

    public static class IntPropertyProperty extends AbstractPropertyData<Integer> {

      private static final long serialVersionUID = 1L;
    }
  }

  public static class ObjectPropertyProperty extends AbstractPropertyData<Object> {

    private static final long serialVersionUID = 1L;
  }

  public static class PropertyTestNrProperty extends AbstractPropertyData<Long> {

    private static final long serialVersionUID = 1L;
  }

  public static class SingleArrayPropertyProperty extends AbstractPropertyData<String[]> {

    private static final long serialVersionUID = 1L;
  }

  public static class WizardsProperty extends AbstractPropertyData<HashMap<String, List<IService>>> {

    private static final long serialVersionUID = 1L;
  }
}