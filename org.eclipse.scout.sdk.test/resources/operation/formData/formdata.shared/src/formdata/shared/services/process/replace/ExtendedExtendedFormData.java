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
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

public class ExtendedExtendedFormData extends ExtendedFormData {
  private static final long serialVersionUID = 1L;

  public ExtendedExtendedFormData() {
  }

  public IgnoringGroupBoxExCreateNone getIgnoringGroupBoxExCreateNone() {
    return getFieldByClass(IgnoringGroupBoxExCreateNone.class);
  }

  public IgnoringGroupBoxExNoneCreate getIgnoringGroupBoxExNoneCreate() {
    return getFieldByClass(IgnoringGroupBoxExNoneCreate.class);
  }

  public NameExEx getNameExEx() {
    return getFieldByClass(NameExEx.class);
  }

  public SdkCommandCreateCreateCreate getSdkCommandCreateCreateCreate() {
    return getFieldByClass(SdkCommandCreateCreateCreate.class);
  }

  public SdkCommandCreateCreateIgnore getSdkCommandCreateCreateIgnore() {
    return getFieldByClass(SdkCommandCreateCreateIgnore.class);
  }

  public SdkCommandCreateCreateNone getSdkCommandCreateCreateNone() {
    return getFieldByClass(SdkCommandCreateCreateNone.class);
  }

  public SdkCommandCreateCreateUse getSdkCommandCreateCreateUse() {
    return getFieldByClass(SdkCommandCreateCreateUse.class);
  }

  public SdkCommandCreateIgnoreCreate getSdkCommandCreateIgnoreCreate() {
    return getFieldByClass(SdkCommandCreateIgnoreCreate.class);
  }

  public SdkCommandCreateIgnoreIgnore getSdkCommandCreateIgnoreIgnore() {
    return getFieldByClass(SdkCommandCreateIgnoreIgnore.class);
  }

  public SdkCommandCreateIgnoreNone getSdkCommandCreateIgnoreNone() {
    return getFieldByClass(SdkCommandCreateIgnoreNone.class);
  }

  public SdkCommandCreateIgnoreUse getSdkCommandCreateIgnoreUse() {
    return getFieldByClass(SdkCommandCreateIgnoreUse.class);
  }

  public SdkCommandCreateNoneCreate getSdkCommandCreateNoneCreate() {
    return getFieldByClass(SdkCommandCreateNoneCreate.class);
  }

  public SdkCommandCreateNoneIgnore getSdkCommandCreateNoneIgnore() {
    return getFieldByClass(SdkCommandCreateNoneIgnore.class);
  }

  public SdkCommandCreateNoneNone getSdkCommandCreateNoneNone() {
    return getFieldByClass(SdkCommandCreateNoneNone.class);
  }

  public SdkCommandCreateNoneUse getSdkCommandCreateNoneUse() {
    return getFieldByClass(SdkCommandCreateNoneUse.class);
  }

  public SdkCommandCreateUseCreate getSdkCommandCreateUseCreate() {
    return getFieldByClass(SdkCommandCreateUseCreate.class);
  }

  public SdkCommandCreateUseIgnore getSdkCommandCreateUseIgnore() {
    return getFieldByClass(SdkCommandCreateUseIgnore.class);
  }

  public SdkCommandCreateUseNone getSdkCommandCreateUseNone() {
    return getFieldByClass(SdkCommandCreateUseNone.class);
  }

  public SdkCommandCreateUseUse getSdkCommandCreateUseUse() {
    return getFieldByClass(SdkCommandCreateUseUse.class);
  }

  public SdkCommandIgnoreCreateCreate getSdkCommandIgnoreCreateCreate() {
    return getFieldByClass(SdkCommandIgnoreCreateCreate.class);
  }

  public SdkCommandIgnoreCreateIgnore getSdkCommandIgnoreCreateIgnore() {
    return getFieldByClass(SdkCommandIgnoreCreateIgnore.class);
  }

  public SdkCommandIgnoreCreateNone getSdkCommandIgnoreCreateNone() {
    return getFieldByClass(SdkCommandIgnoreCreateNone.class);
  }

  public SdkCommandIgnoreCreateUse getSdkCommandIgnoreCreateUse() {
    return getFieldByClass(SdkCommandIgnoreCreateUse.class);
  }

  public SdkCommandIgnoreIgnoreCreate getSdkCommandIgnoreIgnoreCreate() {
    return getFieldByClass(SdkCommandIgnoreIgnoreCreate.class);
  }

  public SdkCommandIgnoreIgnoreUse getSdkCommandIgnoreIgnoreUse() {
    return getFieldByClass(SdkCommandIgnoreIgnoreUse.class);
  }

  public SdkCommandIgnoreNoneCreate getSdkCommandIgnoreNoneCreate() {
    return getFieldByClass(SdkCommandIgnoreNoneCreate.class);
  }

  public SdkCommandIgnoreNoneUse getSdkCommandIgnoreNoneUse() {
    return getFieldByClass(SdkCommandIgnoreNoneUse.class);
  }

  public SdkCommandIgnoreUseCreate getSdkCommandIgnoreUseCreate() {
    return getFieldByClass(SdkCommandIgnoreUseCreate.class);
  }

  public SdkCommandIgnoreUseIgnore getSdkCommandIgnoreUseIgnore() {
    return getFieldByClass(SdkCommandIgnoreUseIgnore.class);
  }

  public SdkCommandIgnoreUseNone getSdkCommandIgnoreUseNone() {
    return getFieldByClass(SdkCommandIgnoreUseNone.class);
  }

  public SdkCommandIgnoreUseUse getSdkCommandIgnoreUseUse() {
    return getFieldByClass(SdkCommandIgnoreUseUse.class);
  }

  public SdkCommandNoneCreateCreate getSdkCommandNoneCreateCreate() {
    return getFieldByClass(SdkCommandNoneCreateCreate.class);
  }

  public SdkCommandNoneCreateIgnore getSdkCommandNoneCreateIgnore() {
    return getFieldByClass(SdkCommandNoneCreateIgnore.class);
  }

  public SdkCommandNoneCreateNone getSdkCommandNoneCreateNone() {
    return getFieldByClass(SdkCommandNoneCreateNone.class);
  }

  public SdkCommandNoneCreateUse getSdkCommandNoneCreateUse() {
    return getFieldByClass(SdkCommandNoneCreateUse.class);
  }

  public SdkCommandNoneIgnoreCreate getSdkCommandNoneIgnoreCreate() {
    return getFieldByClass(SdkCommandNoneIgnoreCreate.class);
  }

  public SdkCommandNoneIgnoreIgnore getSdkCommandNoneIgnoreIgnore() {
    return getFieldByClass(SdkCommandNoneIgnoreIgnore.class);
  }

  public SdkCommandNoneIgnoreNone getSdkCommandNoneIgnoreNone() {
    return getFieldByClass(SdkCommandNoneIgnoreNone.class);
  }

  public SdkCommandNoneIgnoreUse getSdkCommandNoneIgnoreUse() {
    return getFieldByClass(SdkCommandNoneIgnoreUse.class);
  }

  public SdkCommandNoneNoneCreate getSdkCommandNoneNoneCreate() {
    return getFieldByClass(SdkCommandNoneNoneCreate.class);
  }

  public SdkCommandNoneNoneIgnore getSdkCommandNoneNoneIgnore() {
    return getFieldByClass(SdkCommandNoneNoneIgnore.class);
  }

  public SdkCommandNoneNoneNone getSdkCommandNoneNoneNone() {
    return getFieldByClass(SdkCommandNoneNoneNone.class);
  }

  public SdkCommandNoneNoneUse getSdkCommandNoneNoneUse() {
    return getFieldByClass(SdkCommandNoneNoneUse.class);
  }

  public SdkCommandNoneUseCreate getSdkCommandNoneUseCreate() {
    return getFieldByClass(SdkCommandNoneUseCreate.class);
  }

  public SdkCommandNoneUseIgnore getSdkCommandNoneUseIgnore() {
    return getFieldByClass(SdkCommandNoneUseIgnore.class);
  }

  public SdkCommandNoneUseNone getSdkCommandNoneUseNone() {
    return getFieldByClass(SdkCommandNoneUseNone.class);
  }

  public SdkCommandNoneUseUse getSdkCommandNoneUseUse() {
    return getFieldByClass(SdkCommandNoneUseUse.class);
  }

  public SdkCommandUseCreateCreate getSdkCommandUseCreateCreate() {
    return getFieldByClass(SdkCommandUseCreateCreate.class);
  }

  public SdkCommandUseCreateIgnore getSdkCommandUseCreateIgnore() {
    return getFieldByClass(SdkCommandUseCreateIgnore.class);
  }

  public SdkCommandUseCreateNone getSdkCommandUseCreateNone() {
    return getFieldByClass(SdkCommandUseCreateNone.class);
  }

  public SdkCommandUseCreateUse getSdkCommandUseCreateUse() {
    return getFieldByClass(SdkCommandUseCreateUse.class);
  }

  public SdkCommandUseIgnoreCreate getSdkCommandUseIgnoreCreate() {
    return getFieldByClass(SdkCommandUseIgnoreCreate.class);
  }

  public SdkCommandUseIgnoreIgnore getSdkCommandUseIgnoreIgnore() {
    return getFieldByClass(SdkCommandUseIgnoreIgnore.class);
  }

  public SdkCommandUseIgnoreNone getSdkCommandUseIgnoreNone() {
    return getFieldByClass(SdkCommandUseIgnoreNone.class);
  }

  public SdkCommandUseIgnoreUse getSdkCommandUseIgnoreUse() {
    return getFieldByClass(SdkCommandUseIgnoreUse.class);
  }

  public SdkCommandUseNoneCreate getSdkCommandUseNoneCreate() {
    return getFieldByClass(SdkCommandUseNoneCreate.class);
  }

  public SdkCommandUseNoneIgnore getSdkCommandUseNoneIgnore() {
    return getFieldByClass(SdkCommandUseNoneIgnore.class);
  }

  public SdkCommandUseNoneNone getSdkCommandUseNoneNone() {
    return getFieldByClass(SdkCommandUseNoneNone.class);
  }

  public SdkCommandUseNoneUse getSdkCommandUseNoneUse() {
    return getFieldByClass(SdkCommandUseNoneUse.class);
  }

  public SdkCommandUseUseCreate getSdkCommandUseUseCreate() {
    return getFieldByClass(SdkCommandUseUseCreate.class);
  }

  public SdkCommandUseUseIgnore getSdkCommandUseUseIgnore() {
    return getFieldByClass(SdkCommandUseUseIgnore.class);
  }

  public SdkCommandUseUseNone getSdkCommandUseUseNone() {
    return getFieldByClass(SdkCommandUseUseNone.class);
  }

  public SdkCommandUseUseUse getSdkCommandUseUseUse() {
    return getFieldByClass(SdkCommandUseUseUse.class);
  }

  @Replace
  public static class IgnoringGroupBoxExCreateNone extends ExtendedFormData.IgnoringGroupBoxExCreate {
    private static final long serialVersionUID = 1L;

    public IgnoringGroupBoxExCreateNone() {
    }
  }

  @Replace
  public static class IgnoringGroupBoxExNoneCreate extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public IgnoringGroupBoxExNoneCreate() {
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
  public static class NameExEx extends ExtendedFormData.NameEx {
    private static final long serialVersionUID = 1L;

    public NameExEx() {
    }

    public StringPropertyProperty getStringPropertyProperty() {
      return getPropertyByClass(StringPropertyProperty.class);
    }

    /**
     * access method for property StringProperty.
     */
    public String getStringProperty() {
      return getStringPropertyProperty().getValue();
    }

    /**
     * access method for property StringProperty.
     */
    public void setStringProperty(String stringProperty) {
      getStringPropertyProperty().setValue(stringProperty);
    }

    public class StringPropertyProperty extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;

      public StringPropertyProperty() {
      }
    }

    /**
     * list of derived validation rules.
     */
    @Override
    protected void initValidationRules(java.util.Map<String, Object> ruleMap) {
      super.initValidationRules(ruleMap);
      ruleMap.put(ValidationRule.MANDATORY, true);
      ruleMap.put(ValidationRule.MAX_LENGTH, 15);
    }
  }

  @Replace
  public static class SdkCommandCreateCreateCreate extends ExtendedFormData.SdkCommandCreateCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateCreateCreate() {
    }
  }

  @Replace
  public static class SdkCommandCreateCreateIgnore extends ExtendedFormData.SdkCommandCreateCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateCreateIgnore() {
    }
  }

  @Replace
  public static class SdkCommandCreateCreateNone extends ExtendedFormData.SdkCommandCreateCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateCreateNone() {
    }
  }

  @Replace
  public static class SdkCommandCreateCreateUse extends ExtendedFormData.SdkCommandCreateCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateCreateUse() {
    }
  }

  @Replace
  public static class SdkCommandCreateIgnoreCreate extends ExtendedFormData.SdkCommandCreateIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateIgnoreCreate() {
    }
  }

  @Replace
  public static class SdkCommandCreateIgnoreIgnore extends ExtendedFormData.SdkCommandCreateIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateIgnoreIgnore() {
    }
  }

  @Replace
  public static class SdkCommandCreateIgnoreNone extends ExtendedFormData.SdkCommandCreateIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateIgnoreNone() {
    }
  }

  @Replace
  public static class SdkCommandCreateIgnoreUse extends ExtendedFormData.SdkCommandCreateIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateIgnoreUse() {
    }
  }

  @Replace
  public static class SdkCommandCreateNoneCreate extends ExtendedFormData.SdkCommandCreateNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateNoneCreate() {
    }
  }

  @Replace
  public static class SdkCommandCreateNoneIgnore extends ExtendedFormData.SdkCommandCreateNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateNoneIgnore() {
    }
  }

  @Replace
  public static class SdkCommandCreateNoneNone extends ExtendedFormData.SdkCommandCreateNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateNoneNone() {
    }
  }

  @Replace
  public static class SdkCommandCreateNoneUse extends ExtendedFormData.SdkCommandCreateNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateNoneUse() {
    }
  }

  @Replace
  public static class SdkCommandCreateUseCreate extends ExtendedFormData.SdkCommandCreateUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateUseCreate() {
    }
  }

  @Replace
  public static class SdkCommandCreateUseIgnore extends ExtendedFormData.SdkCommandCreateUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateUseIgnore() {
    }
  }

  @Replace
  public static class SdkCommandCreateUseNone extends ExtendedFormData.SdkCommandCreateUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateUseNone() {
    }
  }

  @Replace
  public static class SdkCommandCreateUseUse extends ExtendedFormData.SdkCommandCreateUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandCreateUseUse() {
    }
  }

  @Replace
  public static class SdkCommandIgnoreCreateCreate extends ExtendedFormData.SdkCommandIgnoreCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreCreateCreate() {
    }
  }

  @Replace
  public static class SdkCommandIgnoreCreateIgnore extends ExtendedFormData.SdkCommandIgnoreCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreCreateIgnore() {
    }
  }

  @Replace
  public static class SdkCommandIgnoreCreateNone extends ExtendedFormData.SdkCommandIgnoreCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreCreateNone() {
    }
  }

  @Replace
  public static class SdkCommandIgnoreCreateUse extends ExtendedFormData.SdkCommandIgnoreCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreCreateUse() {
    }
  }

  @Replace
  public static class SdkCommandIgnoreIgnoreCreate extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreIgnoreCreate() {
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
  public static class SdkCommandIgnoreIgnoreUse extends UsingFormFieldData {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreIgnoreUse() {
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
  public static class SdkCommandIgnoreNoneCreate extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreNoneCreate() {
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
  public static class SdkCommandIgnoreNoneUse extends UsingFormFieldData {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreNoneUse() {
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
  public static class SdkCommandIgnoreUseCreate extends ExtendedFormData.SdkCommandIgnoreUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreUseCreate() {
    }
  }

  @Replace
  public static class SdkCommandIgnoreUseIgnore extends ExtendedFormData.SdkCommandIgnoreUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreUseIgnore() {
    }
  }

  @Replace
  public static class SdkCommandIgnoreUseNone extends ExtendedFormData.SdkCommandIgnoreUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreUseNone() {
    }
  }

  @Replace
  public static class SdkCommandIgnoreUseUse extends ExtendedFormData.SdkCommandIgnoreUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandIgnoreUseUse() {
    }
  }

  @Replace
  public static class SdkCommandNoneCreateCreate extends ExtendedFormData.SdkCommandNoneCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneCreateCreate() {
    }
  }

  @Replace
  public static class SdkCommandNoneCreateIgnore extends ExtendedFormData.SdkCommandNoneCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneCreateIgnore() {
    }
  }

  @Replace
  public static class SdkCommandNoneCreateNone extends ExtendedFormData.SdkCommandNoneCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneCreateNone() {
    }
  }

  @Replace
  public static class SdkCommandNoneCreateUse extends ExtendedFormData.SdkCommandNoneCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneCreateUse() {
    }
  }

  @Replace
  public static class SdkCommandNoneIgnoreCreate extends ExtendedFormData.SdkCommandNoneIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneIgnoreCreate() {
    }
  }

  @Replace
  public static class SdkCommandNoneIgnoreIgnore extends ExtendedFormData.SdkCommandNoneIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneIgnoreIgnore() {
    }
  }

  @Replace
  public static class SdkCommandNoneIgnoreNone extends ExtendedFormData.SdkCommandNoneIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneIgnoreNone() {
    }
  }

  @Replace
  public static class SdkCommandNoneIgnoreUse extends ExtendedFormData.SdkCommandNoneIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneIgnoreUse() {
    }
  }

  @Replace
  public static class SdkCommandNoneNoneCreate extends ExtendedFormData.SdkCommandNoneNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneNoneCreate() {
    }
  }

  @Replace
  public static class SdkCommandNoneNoneIgnore extends ExtendedFormData.SdkCommandNoneNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneNoneIgnore() {
    }
  }

  @Replace
  public static class SdkCommandNoneNoneNone extends ExtendedFormData.SdkCommandNoneNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneNoneNone() {
    }
  }

  @Replace
  public static class SdkCommandNoneNoneUse extends ExtendedFormData.SdkCommandNoneNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneNoneUse() {
    }
  }

  @Replace
  public static class SdkCommandNoneUseCreate extends ExtendedFormData.SdkCommandNoneUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneUseCreate() {
    }
  }

  @Replace
  public static class SdkCommandNoneUseIgnore extends ExtendedFormData.SdkCommandNoneUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneUseIgnore() {
    }
  }

  @Replace
  public static class SdkCommandNoneUseNone extends ExtendedFormData.SdkCommandNoneUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneUseNone() {
    }
  }

  @Replace
  public static class SdkCommandNoneUseUse extends ExtendedFormData.SdkCommandNoneUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandNoneUseUse() {
    }
  }

  @Replace
  public static class SdkCommandUseCreateCreate extends ExtendedFormData.SdkCommandUseCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseCreateCreate() {
    }
  }

  @Replace
  public static class SdkCommandUseCreateIgnore extends ExtendedFormData.SdkCommandUseCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseCreateIgnore() {
    }
  }

  @Replace
  public static class SdkCommandUseCreateNone extends ExtendedFormData.SdkCommandUseCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseCreateNone() {
    }
  }

  @Replace
  public static class SdkCommandUseCreateUse extends ExtendedFormData.SdkCommandUseCreate {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseCreateUse() {
    }
  }

  @Replace
  public static class SdkCommandUseIgnoreCreate extends ExtendedFormData.SdkCommandUseIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseIgnoreCreate() {
    }
  }

  @Replace
  public static class SdkCommandUseIgnoreIgnore extends ExtendedFormData.SdkCommandUseIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseIgnoreIgnore() {
    }
  }

  @Replace
  public static class SdkCommandUseIgnoreNone extends ExtendedFormData.SdkCommandUseIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseIgnoreNone() {
    }
  }

  @Replace
  public static class SdkCommandUseIgnoreUse extends ExtendedFormData.SdkCommandUseIgnore {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseIgnoreUse() {
    }
  }

  @Replace
  public static class SdkCommandUseNoneCreate extends ExtendedFormData.SdkCommandUseNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseNoneCreate() {
    }
  }

  @Replace
  public static class SdkCommandUseNoneIgnore extends ExtendedFormData.SdkCommandUseNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseNoneIgnore() {
    }
  }

  @Replace
  public static class SdkCommandUseNoneNone extends ExtendedFormData.SdkCommandUseNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseNoneNone() {
    }
  }

  @Replace
  public static class SdkCommandUseNoneUse extends ExtendedFormData.SdkCommandUseNone {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseNoneUse() {
    }
  }

  @Replace
  public static class SdkCommandUseUseCreate extends ExtendedFormData.SdkCommandUseUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseUseCreate() {
    }
  }

  @Replace
  public static class SdkCommandUseUseIgnore extends ExtendedFormData.SdkCommandUseUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseUseIgnore() {
    }
  }

  @Replace
  public static class SdkCommandUseUseNone extends ExtendedFormData.SdkCommandUseUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseUseNone() {
    }
  }

  @Replace
  public static class SdkCommandUseUseUse extends ExtendedFormData.SdkCommandUseUse {
    private static final long serialVersionUID = 1L;

    public SdkCommandUseUseUse() {
    }
  }
}
