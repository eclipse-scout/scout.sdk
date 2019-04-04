/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.client.ui.forms.replace;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

import formdata.shared.services.process.replace.ExtendedFormData;
import formdata.shared.services.process.replace.UsingFormFieldData;

@FormData(value = ExtendedFormData.class, sdkCommand = SdkCommand.CREATE)
public class ExtendedForm extends BaseForm {

  /* ##########################################################################
   * injecting additional field
   * ##########################################################################
   */
  @Order(20)
  @InjectFieldTo(BaseForm.MainBox.class)
  public class FirstNameField extends AbstractStringField {
  }

  /* ##########################################################################
   * replacing fields that are creating a form data (without explicitly
   * defining a @FormData annotation)
   * expectation: replacing form field data must be created and it must be a
   *              subclass of the parent field's form field data. Hence any
   *              @FormData annotation available on the field replacements are
   *              ignored.
   * ##########################################################################
   */
  @Replace
  public class SdkCommandNoneNoneField extends BaseForm.MainBox.NoneFormDataFieldsGroupBox.SdkCommandNoneField {
    public SdkCommandNoneNoneField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandNoneCreateField extends BaseForm.MainBox.NoneFormDataFieldsGroupBox.SdkCommandNoneField {
    public SdkCommandNoneCreateField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandNoneUseField extends BaseForm.MainBox.NoneFormDataFieldsGroupBox.SdkCommandNoneField {
    public SdkCommandNoneUseField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandNoneIgnoreField extends BaseForm.MainBox.NoneFormDataFieldsGroupBox.SdkCommandNoneField {
    public SdkCommandNoneIgnoreField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  /* ##########################################################################
   * replacing fields that are creating a form data (SdkCommand CREATE)
   * expectation: replacing form field data must be created and it must be a
   *              subclass of the parent field's form field data. Hence any
   *              @FormData annotation available on the field replacements are
   *              ignored.
   * ##########################################################################
   */
  @Replace
  public class SdkCommandCreateNoneField extends BaseForm.MainBox.CreatingFormDataFieldsGroupBox.SdkCommandCreateField {
    public SdkCommandCreateNoneField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandCreateCreateField extends BaseForm.MainBox.CreatingFormDataFieldsGroupBox.SdkCommandCreateField {
    public SdkCommandCreateCreateField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandCreateUseField extends BaseForm.MainBox.CreatingFormDataFieldsGroupBox.SdkCommandCreateField {
    public SdkCommandCreateUseField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandCreateIgnoreField extends BaseForm.MainBox.CreatingFormDataFieldsGroupBox.SdkCommandCreateField {
    public SdkCommandCreateIgnoreField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  /* ##########################################################################
   * replacing fields that are using a form data (SdkCommand USE)
   * expectation: replacing form field data must be created and it must be a
   *              subclass of the parent field's form field data. Hence any
   *              @FormData annotation available on the field replacements are
   *              ignored.
   * ##########################################################################
   */
  @Replace
  public class SdkCommandUseNoneField extends BaseForm.MainBox.UsingFormDataFieldsGroupBox.SdkCommandUseField {
    public SdkCommandUseNoneField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandUseCreateField extends BaseForm.MainBox.UsingFormDataFieldsGroupBox.SdkCommandUseField {
    public SdkCommandUseCreateField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandUseUseField extends BaseForm.MainBox.UsingFormDataFieldsGroupBox.SdkCommandUseField {
    public SdkCommandUseUseField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandUseIgnoreField extends BaseForm.MainBox.UsingFormDataFieldsGroupBox.SdkCommandUseField {
    public SdkCommandUseIgnoreField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  /* ########################################################################
   * replacing fields that are ignoring a form data (SdkCommand IGNORE)
   * expectation: replacing form field data must be created if the replacement
   *              field is not annotated with SdkCommand.IGNORE. There is no
   *              special parent field data handling required.
   * ########################################################################
   */
  @Replace
  public class SdkCommandIgnoreNoneField extends BaseForm.MainBox.IgnoringFormDataFieldsGroupBox.SdkCommandIgnoreField {
    public SdkCommandIgnoreNoneField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandIgnoreCreateField extends BaseForm.MainBox.IgnoringFormDataFieldsGroupBox.SdkCommandIgnoreField {
    public SdkCommandIgnoreCreateField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandIgnoreUseField extends BaseForm.MainBox.IgnoringFormDataFieldsGroupBox.SdkCommandIgnoreField {
    public SdkCommandIgnoreUseField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandIgnoreIgnoreField extends BaseForm.MainBox.IgnoringFormDataFieldsGroupBox.SdkCommandIgnoreField {
    public SdkCommandIgnoreIgnoreField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      container.super();
    }
  }

  /* ########################################################################
   * additional tests
   * ########################################################################
   */
  @Replace
  public class NameExField extends BaseForm.MainBox.GroupBox.NameField {

    public NameExField(BaseForm.MainBox.GroupBox container) {
      container.super();
    }

    @Override
    protected boolean getConfiguredMandatory() {
      return false;
    }

    @Override
    protected int getConfiguredMaxLength() {
      return 100;
    }
  }

  @Replace
  public class SmartExField extends BaseForm.MainBox.GroupBox.SmartField {

    public SmartExField(BaseForm.MainBox.GroupBox container) {
      container.super();
    }

    @Override
    protected Class<? extends ICodeType<?, Long>> getConfiguredCodeType() {
      return super.getConfiguredCodeType();
    }
  }

  @Replace
  public class IgnoringGroupBoxExNoneField extends BaseForm.MainBox.IgnoringGroupBox.IgnoringGroupBoxField {

    public IgnoringGroupBoxExNoneField(BaseForm.MainBox.IgnoringGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class IgnoringGroupBoxExCreateField extends BaseForm.MainBox.IgnoringGroupBox.IgnoringGroupBoxField {

    public IgnoringGroupBoxExCreateField(BaseForm.MainBox.IgnoringGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class IgnoringGroupBoxExUseField extends BaseForm.MainBox.IgnoringGroupBox.IgnoringGroupBoxField {

    public IgnoringGroupBoxExUseField(BaseForm.MainBox.IgnoringGroupBox container) {
      container.super();
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class IgnoringGroupBoxExIgnoreField extends BaseForm.MainBox.IgnoringGroupBox.IgnoringGroupBoxField {

    public IgnoringGroupBoxExIgnoreField(BaseForm.MainBox.IgnoringGroupBox container) {
      container.super();
    }
  }

  @Replace
  public class CloseExButton extends BaseForm.MainBox.CloseButton {

    public CloseExButton(BaseForm.MainBox container) {
      container.super();
    }
  }
}
