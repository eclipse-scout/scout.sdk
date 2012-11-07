package formfield.shared.services.process;

import formfield.shared.services.code.TestCodeType;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import java.util.Map;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

public class DesktopFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public DesktopFormData() {
  }

  public TestSmart getTestSmart() {
    return getFieldByClass(TestSmart.class);
  }

  public static class TestSmart extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public TestSmart() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.CODE_TYPE, TestCodeType.class);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }
}
