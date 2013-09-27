package formdata.shared.services.process;

import java.util.Map;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class SimpleFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public SimpleFormData() {
  }

  public Date getDate() {
    return getFieldByClass(Date.class);
  }

  public Double getDouble() {
    return getFieldByClass(Double.class);
  }

  public SampleComposer getSampleComposer() {
    return getFieldByClass(SampleComposer.class);
  }

  public SampleDate getSampleDate() {
    return getFieldByClass(SampleDate.class);
  }

  public SampleSmart getSampleSmart() {
    return getFieldByClass(SampleSmart.class);
  }

  public SampleString getSampleString() {
    return getFieldByClass(SampleString.class);
  }

  /**
   * access method for property SimpleNr.
   */
  public Long getSimpleNr() {
    return getSimpleNrProperty().getValue();
  }

  /**
   * access method for property SimpleNr.
   */
  public void setSimpleNr(Long simpleNr) {
    getSimpleNrProperty().setValue(simpleNr);
  }

  public SimpleNrProperty getSimpleNrProperty() {
    return getPropertyByClass(SimpleNrProperty.class);
  }

  public static class Date extends AbstractValueFieldData<Integer> {

    private static final long serialVersionUID = 1L;

    public Date() {
    }
  }

  public static class Double extends AbstractValueFieldData<java.lang.Double> {

    private static final long serialVersionUID = 1L;

    public Double() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_VALUE, -Double.MAX_VALUE);
      ruleMap.put(ValidationRule.MIN_VALUE, 0.0);
    }
  }

  public static class SampleComposer extends AbstractComposerData {

    private static final long serialVersionUID = 1L;

    public SampleComposer() {
    }
  }

  public static class SampleDate extends AbstractValueFieldData<java.util.Date> {

    private static final long serialVersionUID = 1L;

    public SampleDate() {
    }
  }

  public static class SampleSmart extends AbstractValueFieldData<Long> {

    private static final long serialVersionUID = 1L;

    public SampleSmart() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }

  public static class SampleString extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    public SampleString() {
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

  public static class SimpleNrProperty extends AbstractPropertyData<Long> {

    private static final long serialVersionUID = 1L;

    public SimpleNrProperty() {
    }
  }
}
