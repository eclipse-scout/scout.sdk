package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import java.util.Map;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;

public class UsingTemplateFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public UsingTemplateFormData() {
  }

  public ExternalGroupBox getExternalGroupBox() {
    return getFieldByClass(ExternalGroupBox.class);
  }

  public InternalHtml getInternalHtml() {
    return getFieldByClass(InternalHtml.class);
  }

  public TestCheckbox getTestCheckbox() {
    return getFieldByClass(TestCheckbox.class);
  }

  public static class ExternalGroupBox extends AbstractExternalGroupBoxData {
    private static final long serialVersionUID = 1L;

    public ExternalGroupBox() {
    }
  }

  public static class InternalHtml extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public InternalHtml() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_LENGTH, Integer.MAX_VALUE);
    }
  }

  public static class TestCheckbox extends AbstractTestCheckboxFieldData {
    private static final long serialVersionUID = 1L;

    public TestCheckbox() {
    }
  }
}
