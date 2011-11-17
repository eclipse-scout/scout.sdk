package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import java.util.Map;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

public class SimpleFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public SimpleFormData() {
  }

  public SimpleNrProperty getSimpleNrProperty() {
    return getPropertyByClass(SimpleNrProperty.class);
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

  public SampleComposer getSampleComposer() {
    return getFieldByClass(SampleComposer.class);
  }

  public SampleDouble getSampleDouble() {
    return getFieldByClass(SampleDouble.class);
  }

  public SampleSmart getSampleSmart() {
    return getFieldByClass(SampleSmart.class);
  }

  public SampleString getSampleString() {
    return getFieldByClass(SampleString.class);
  }

  public class SimpleNrProperty extends AbstractPropertyData<Long> {
    private static final long serialVersionUID = 1L;

    public SimpleNrProperty() {
    }
  }

  public static class SampleComposer extends AbstractComposerData {
    private static final long serialVersionUID = 1L;

    public SampleComposer() {
    }
  }

  public static class SampleDouble extends AbstractValueFieldData<Double> {
    private static final long serialVersionUID = 1L;

    public SampleDouble() {
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
}
