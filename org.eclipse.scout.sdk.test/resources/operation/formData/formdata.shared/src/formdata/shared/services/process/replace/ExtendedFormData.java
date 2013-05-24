/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.services.process.replace;

import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

public class ExtendedFormData extends BaseFormData {
  private static final long serialVersionUID = 1L;

  public ExtendedFormData() {
  }

  public FirstName getFirstName() {
    return getFieldByClass(FirstName.class);
  }

  public IgnoringGroupBoxExCreate getIgnoringGroupBoxExCreate() {
    return getFieldByClass(IgnoringGroupBoxExCreate.class);
  }

  public IgnoringGroupBoxExUse getIgnoringGroupBoxExUse() {
    return getFieldByClass(IgnoringGroupBoxExUse.class);
  }

  public NameEx getNameEx() {
    return getFieldByClass(NameEx.class);
  }

  public SdkCommandCreateCreate getSdkCommandCreateCreate() {
    return getFieldByClass(SdkCommandCreateCreate.class);
  }

  public SdkCommandCreateIgnore getSdkCommandCreateIgnore() {
    return getFieldByClass(SdkCommandCreateIgnore.class);
  }

  public SdkCommandCreateNone getSdkCommandCreateNone() {
    return getFieldByClass(SdkCommandCreateNone.class);
  }

  public SdkCommandCreateUse getSdkCommandCreateUse() {
    return getFieldByClass(SdkCommandCreateUse.class);
  }

  public SdkCommandIgnoreCreate getSdkCommandIgnoreCreate() {
    return getFieldByClass(SdkCommandIgnoreCreate.class);
  }

  public SdkCommandIgnoreUse getSdkCommandIgnoreUse() {
    return getFieldByClass(SdkCommandIgnoreUse.class);
  }

  public SdkCommandNoneCreate getSdkCommandNoneCreate() {
    return getFieldByClass(SdkCommandNoneCreate.class);
  }

  public SdkCommandNoneIgnore getSdkCommandNoneIgnore() {
    return getFieldByClass(SdkCommandNoneIgnore.class);
  }

  public SdkCommandNoneNone getSdkCommandNoneNone() {
    return getFieldByClass(SdkCommandNoneNone.class);
  }

  public SdkCommandNoneUse getSdkCommandNoneUse() {
    return getFieldByClass(SdkCommandNoneUse.class);
  }

  public SdkCommandUseCreate getSdkCommandUseCreate() {
    return getFieldByClass(SdkCommandUseCreate.class);
  }

  public SdkCommandUseIgnore getSdkCommandUseIgnore() {
    return getFieldByClass(SdkCommandUseIgnore.class);
  }

  public SdkCommandUseNone getSdkCommandUseNone() {
    return getFieldByClass(SdkCommandUseNone.class);
  }

  public SdkCommandUseUse getSdkCommandUseUse() {
    return getFieldByClass(SdkCommandUseUse.class);
  }

  public SmartEx getSmartEx() {
    return getFieldByClass(SmartEx.class);
  }

  public static class FirstName extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public FirstName() {
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

  @Replace
  public static class IgnoringGroupBoxExCreate extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public IgnoringGroupBoxExCreate() {
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

  @Replace
  public static class IgnoringGroupBoxExUse extends UsingFormFieldData {
    private static final long serialVersionUID = 1L;

    public IgnoringGroupBoxExUse() {
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

  @Replace
  public static class NameEx extends BaseFormData.Name {
    private static final long serialVersionUID = 1L;

    public NameEx() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.remove(ValidationRule.MANDATORY);
      ruleMap.put(ValidationRule.MAX_LENGTH, 100);
    }
  }

  @Replace
  public static class SdkCommandCreateCreate extends BaseFormData.SdkCommandCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateCreate() {
    }
  }

  @Replace
  public static class SdkCommandCreateIgnore extends BaseFormData.SdkCommandCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateIgnore() {
    }
  }

  @Replace
  public static class SdkCommandCreateNone extends BaseFormData.SdkCommandCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateNone() {
    }
  }

  @Replace
  public static class SdkCommandCreateUse extends BaseFormData.SdkCommandCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateUse() {
    }
  }

  @Replace
  public static class SdkCommandIgnoreCreate extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreCreate() {
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

  @Replace
  public static class SdkCommandIgnoreUse extends UsingFormFieldData {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreUse() {
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

  @Replace
  public static class SdkCommandNoneCreate extends BaseFormData.SdkCommandNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneCreate() {
    }
  }

  @Replace
  public static class SdkCommandNoneIgnore extends BaseFormData.SdkCommandNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneIgnore() {
    }
  }

  @Replace
  public static class SdkCommandNoneNone extends BaseFormData.SdkCommandNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneNone() {
    }
  }

  @Replace
  public static class SdkCommandNoneUse extends BaseFormData.SdkCommandNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneUse() {
    }
  }

  @Replace
  public static class SdkCommandUseCreate extends BaseFormData.SdkCommandUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseCreate() {
    }
  }

  @Replace
  public static class SdkCommandUseIgnore extends BaseFormData.SdkCommandUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseIgnore() {
    }
  }

  @Replace
  public static class SdkCommandUseNone extends BaseFormData.SdkCommandUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseNone() {
    }
  }

  @Replace
  public static class SdkCommandUseUse extends BaseFormData.SdkCommandUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseUse() {
    }
  }

  @Replace
  public static class SmartEx extends BaseFormData.Smart {
    private static final long serialVersionUID = 1L;

    public SmartEx() {
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.remove(ValidationRule.CODE_TYPE);
    }
  }
}
