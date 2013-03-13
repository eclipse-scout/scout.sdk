package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

public class IgnoredFieldsFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public IgnoredFieldsFormData() {
  }

  public NotIgnored getNotIgnored() {
    return getFieldByClass(NotIgnored.class);
  }

  public static class NotIgnored extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public NotIgnored() {
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
