package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

public class FormWithGroupBoxesFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public FormWithGroupBoxesFormData() {
  }

  public FlatString getFlatString() {
    return getFieldByClass(FlatString.class);
  }

  public InnerInteger getInnerInteger() {
    return getFieldByClass(InnerInteger.class);
  }

  public static class FlatString extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public FlatString() {
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

  public static class InnerInteger extends AbstractValueFieldData<Integer> {
    private static final long serialVersionUID = 1L;

    public InnerInteger() {
    }
  }
}
