package presenter.test.shared.services.process;

import presenter.test.shared.services.code.TestCodeType;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import presenter.test.shared.services.lookup.TestLookupCall;

public class DesktopFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public DesktopFormData() {
  }

  public BooleanPresenterTest getBooleanPresenterTest() {
    return getFieldByClass(BooleanPresenterTest.class);
  }

  public CodeTypePresenterTest getCodeTypePresenterTest() {
    return getFieldByClass(CodeTypePresenterTest.class);
  }

  public IconPresenter getIconPresenter() {
    return getFieldByClass(IconPresenter.class);
  }

  public LabelHorizontalAlignmentTest getLabelHorizontalAlignmentTest() {
    return getFieldByClass(LabelHorizontalAlignmentTest.class);
  }

  public LabelPositionPresenterTest getLabelPositionPresenterTest() {
    return getFieldByClass(LabelPositionPresenterTest.class);
  }

  public LongPresenterTest getLongPresenterTest() {
    return getFieldByClass(LongPresenterTest.class);
  }

  public LookupCallProposalPresenterTest getLookupCallProposalPresenterTest() {
    return getFieldByClass(LookupCallProposalPresenterTest.class);
  }

  public MasterFieldPresenterTest getMasterFieldPresenterTest() {
    return getFieldByClass(MasterFieldPresenterTest.class);
  }

  public NlsTextProposalPresenterTest getNlsTextProposalPresenterTest() {
    return getFieldByClass(NlsTextProposalPresenterTest.class);
  }

  public StringPresenterTest getStringPresenterTest() {
    return getFieldByClass(StringPresenterTest.class);
  }

  public static class BooleanPresenterTest extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public BooleanPresenterTest() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MANDATORY, true);
      ruleMap.put(ValidationRule.MAX_LENGTH, 4000);
    }
  }

  public static class CodeTypePresenterTest extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public CodeTypePresenterTest() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.CODE_TYPE, TestCodeType.class);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }

  public static class IconPresenter extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public IconPresenter() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }

  public static class LabelHorizontalAlignmentTest extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public LabelHorizontalAlignmentTest() {
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

  public static class LabelPositionPresenterTest extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public LabelPositionPresenterTest() {
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

  public static class LongPresenterTest extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public LongPresenterTest() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MAX_VALUE, 100L);
    }
  }

  public static class LookupCallProposalPresenterTest extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public LookupCallProposalPresenterTest() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.LOOKUP_CALL, TestLookupCall.class);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }

  public static class MasterFieldPresenterTest extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public MasterFieldPresenterTest() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MASTER_VALUE_FIELD, CodeTypePresenterTest.class);
      ruleMap.put(ValidationRule.ZERO_NULL_EQUALITY, true);
    }
  }

  public static class NlsTextProposalPresenterTest extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public NlsTextProposalPresenterTest() {
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

  public static class StringPresenterTest extends AbstractValueFieldData<Double> {
    private static final long serialVersionUID = 1L;

    public StringPresenterTest() {
    }
  }
}
