package formdata.shared.services.process;

import org.eclipse.scout.service.IService;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import java.util.Map;
import java.util.HashMap;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

public class PropertyTestFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public PropertyTestFormData() {
  }

  public BoolObjectProperty getBoolObjectProperty() {
    return getPropertyByClass(BoolObjectProperty.class);
  }

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

  public BoolPrimitiveProperty getBoolPrimitiveProperty() {
    return getPropertyByClass(BoolPrimitiveProperty.class);
  }

  /**
   * access method for property BoolPrimitive.
   */
  public boolean isBoolPrimitive() {
    return (getBoolPrimitiveProperty().getValue() == null) ? (false) : (getBoolPrimitiveProperty().getValue());
  }

  /**
   * access method for property BoolPrimitive.
   */
  public void setBoolPrimitive(boolean boolPrimitive) {
    getBoolPrimitiveProperty().setValue(boolPrimitive);
  }

  public ByteArrayProperty getByteArrayProperty() {
    return getPropertyByClass(ByteArrayProperty.class);
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

  public ComplexArrayProperty getComplexArrayProperty() {
    return getPropertyByClass(ComplexArrayProperty.class);
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

  public ComplexInnerArrayProperty getComplexInnerArrayProperty() {
    return getPropertyByClass(ComplexInnerArrayProperty.class);
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

  public DoubleArrayPropertyProperty getDoubleArrayPropertyProperty() {
    return getPropertyByClass(DoubleArrayPropertyProperty.class);
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

  public IntPrimitiveProperty getIntPrimitiveProperty() {
    return getPropertyByClass(IntPrimitiveProperty.class);
  }

  /**
   * access method for property IntPrimitive.
   */
  public int getIntPrimitive() {
    return (getIntPrimitiveProperty().getValue() == null) ? (0) : (getIntPrimitiveProperty().getValue());
  }

  /**
   * access method for property IntPrimitive.
   */
  public void setIntPrimitive(int intPrimitive) {
    getIntPrimitiveProperty().setValue(intPrimitive);
  }

  public ObjectPropertyProperty getObjectPropertyProperty() {
    return getPropertyByClass(ObjectPropertyProperty.class);
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

  public PropertyTestNrProperty getPropertyTestNrProperty() {
    return getPropertyByClass(PropertyTestNrProperty.class);
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

  public SingleArrayPropertyProperty getSingleArrayPropertyProperty() {
    return getPropertyByClass(SingleArrayPropertyProperty.class);
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

  public WizardsProperty getWizardsProperty() {
    return getPropertyByClass(WizardsProperty.class);
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

  public Name getName() {
    return getFieldByClass(Name.class);
  }

  public class BoolObjectProperty extends AbstractPropertyData<Boolean> {
    private static final long serialVersionUID = 1L;

    public BoolObjectProperty() {
    }
  }

  public class BoolPrimitiveProperty extends AbstractPropertyData<Boolean> {
    private static final long serialVersionUID = 1L;

    public BoolPrimitiveProperty() {
    }
  }

  public class ByteArrayProperty extends AbstractPropertyData<byte[]> {
    private static final long serialVersionUID = 1L;

    public ByteArrayProperty() {
    }
  }

  public class ComplexArrayProperty extends AbstractPropertyData<ArrayList<List<String>>[]> {
    private static final long serialVersionUID = 1L;

    public ComplexArrayProperty() {
    }
  }

  public class ComplexInnerArrayProperty extends AbstractPropertyData<ArrayList<List<String[]>>> {
    private static final long serialVersionUID = 1L;

    public ComplexInnerArrayProperty() {
    }
  }

  public class DoubleArrayPropertyProperty extends AbstractPropertyData<String[][]> {
    private static final long serialVersionUID = 1L;

    public DoubleArrayPropertyProperty() {
    }
  }

  public class IntPrimitiveProperty extends AbstractPropertyData<Integer> {
    private static final long serialVersionUID = 1L;

    public IntPrimitiveProperty() {
    }
  }

  public class ObjectPropertyProperty extends AbstractPropertyData<Object> {
    private static final long serialVersionUID = 1L;

    public ObjectPropertyProperty() {
    }
  }

  public class PropertyTestNrProperty extends AbstractPropertyData<Long> {
    private static final long serialVersionUID = 1L;

    public PropertyTestNrProperty() {
    }
  }

  public class SingleArrayPropertyProperty extends AbstractPropertyData<String[]> {
    private static final long serialVersionUID = 1L;

    public SingleArrayPropertyProperty() {
    }
  }

  public class WizardsProperty extends AbstractPropertyData<HashMap<String, List<IService>>> {
    private static final long serialVersionUID = 1L;

    public WizardsProperty() {
    }
  }

  public static class Name extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public Name() {
    }

    public IntPropertyProperty getIntPropertyProperty() {
      return getPropertyByClass(IntPropertyProperty.class);
    }

    /**
     * access method for property IntProperty.
     */
    public int getIntProperty() {
      return (getIntPropertyProperty().getValue() == null) ? (0) : (getIntPropertyProperty().getValue());
    }

    /**
     * access method for property IntProperty.
     */
    public void setIntProperty(int intProperty) {
      getIntPropertyProperty().setValue(intProperty);
    }

    public class IntPropertyProperty extends AbstractPropertyData<Integer> {
      private static final long serialVersionUID = 1L;

      public IntPropertyProperty() {
      }
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, 4000);
    }
  }
}
