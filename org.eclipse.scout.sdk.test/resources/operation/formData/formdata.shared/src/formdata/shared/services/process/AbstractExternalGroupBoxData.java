package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

public abstract class AbstractExternalGroupBoxData extends AbstractFormFieldData {
  private static final long serialVersionUID = 1L;

  public AbstractExternalGroupBoxData() {
  }

  public ExternalString getExternalString() {
    return getFieldByClass(ExternalString.class);
  }

  public static class ExternalString extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public ExternalString() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, 4000);
    }
  }
}
