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
package formdata.client.ui.forms.replace;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.exception.ProcessingException;

import formdata.client.ui.forms.replace.BaseForm.MainBox.GroupBox;
import formdata.shared.IFormDataInterface03;
import formdata.shared.services.process.replace.ExtendedExtendedFormData;
import formdata.shared.services.process.replace.UsingFormFieldData;

@FormData(value = ExtendedExtendedFormData.class, sdkCommand = SdkCommand.CREATE)
public class ExtendedExtendedForm extends ExtendedForm {

  public ExtendedExtendedForm() throws ProcessingException {
    super();
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
  public class SdkCommandNoneNoneNoneField extends ExtendedForm.SdkCommandNoneNoneField {
    public SdkCommandNoneNoneNoneField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE, interfaces = IFormDataInterface03.class)
  public class SdkCommandNoneNoneCreateField extends ExtendedForm.SdkCommandNoneNoneField {
    public SdkCommandNoneNoneCreateField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandNoneNoneUseField extends ExtendedForm.SdkCommandNoneNoneField {
    public SdkCommandNoneNoneUseField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandNoneNoneIgnoreField extends ExtendedForm.SdkCommandNoneNoneField {
    public SdkCommandNoneNoneIgnoreField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandNoneCreateNoneField extends ExtendedForm.SdkCommandNoneCreateField {
    public SdkCommandNoneCreateNoneField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandNoneCreateCreateField extends ExtendedForm.SdkCommandNoneCreateField {
    public SdkCommandNoneCreateCreateField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandNoneCreateUseField extends ExtendedForm.SdkCommandNoneCreateField {
    public SdkCommandNoneCreateUseField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandNoneCreateIgnoreField extends ExtendedForm.SdkCommandNoneCreateField {
    public SdkCommandNoneCreateIgnoreField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandNoneUseNoneField extends ExtendedForm.SdkCommandNoneUseField {
    public SdkCommandNoneUseNoneField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandNoneUseCreateField extends ExtendedForm.SdkCommandNoneUseField {
    public SdkCommandNoneUseCreateField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandNoneUseUseField extends ExtendedForm.SdkCommandNoneUseField {
    public SdkCommandNoneUseUseField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandNoneUseIgnoreField extends ExtendedForm.SdkCommandNoneUseField {
    public SdkCommandNoneUseIgnoreField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandNoneIgnoreNoneField extends ExtendedForm.SdkCommandNoneIgnoreField {
    public SdkCommandNoneIgnoreNoneField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandNoneIgnoreCreateField extends ExtendedForm.SdkCommandNoneIgnoreField {
    public SdkCommandNoneIgnoreCreateField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandNoneIgnoreUseField extends ExtendedForm.SdkCommandNoneIgnoreField {
    public SdkCommandNoneIgnoreUseField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandNoneIgnoreIgnoreField extends ExtendedForm.SdkCommandNoneIgnoreField {
    public SdkCommandNoneIgnoreIgnoreField(BaseForm.MainBox.NoneFormDataFieldsGroupBox container) {
      super(container);
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
  public class SdkCommandCreateNoneNoneField extends ExtendedForm.SdkCommandCreateNoneField {
    public SdkCommandCreateNoneNoneField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandCreateNoneCreateField extends ExtendedForm.SdkCommandCreateNoneField {
    public SdkCommandCreateNoneCreateField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandCreateNoneUseField extends ExtendedForm.SdkCommandCreateNoneField {
    public SdkCommandCreateNoneUseField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandCreateNoneIgnoreField extends ExtendedForm.SdkCommandCreateNoneField {
    public SdkCommandCreateNoneIgnoreField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandCreateCreateNoneField extends ExtendedForm.SdkCommandCreateCreateField {
    public SdkCommandCreateCreateNoneField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandCreateCreateCreateField extends ExtendedForm.SdkCommandCreateCreateField {
    public SdkCommandCreateCreateCreateField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandCreateCreateUseField extends ExtendedForm.SdkCommandCreateCreateField {
    public SdkCommandCreateCreateUseField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandCreateCreateIgnoreField extends ExtendedForm.SdkCommandCreateCreateField {
    public SdkCommandCreateCreateIgnoreField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandCreateUseNoneField extends ExtendedForm.SdkCommandCreateUseField {
    public SdkCommandCreateUseNoneField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandCreateUseCreateField extends ExtendedForm.SdkCommandCreateUseField {
    public SdkCommandCreateUseCreateField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandCreateUseUseField extends ExtendedForm.SdkCommandCreateUseField {
    public SdkCommandCreateUseUseField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandCreateUseIgnoreField extends ExtendedForm.SdkCommandCreateUseField {
    public SdkCommandCreateUseIgnoreField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandCreateIgnoreNoneField extends ExtendedForm.SdkCommandCreateIgnoreField {
    public SdkCommandCreateIgnoreNoneField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandCreateIgnoreCreateField extends ExtendedForm.SdkCommandCreateIgnoreField {
    public SdkCommandCreateIgnoreCreateField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandCreateIgnoreUseField extends ExtendedForm.SdkCommandCreateIgnoreField {
    public SdkCommandCreateIgnoreUseField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandCreateIgnoreIgnoreField extends ExtendedForm.SdkCommandCreateIgnoreField {
    public SdkCommandCreateIgnoreIgnoreField(BaseForm.MainBox.CreatingFormDataFieldsGroupBox container) {
      super(container);
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
  public class SdkCommandUseNoneNoneField extends ExtendedForm.SdkCommandUseNoneField {
    public SdkCommandUseNoneNoneField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandUseNoneCreateField extends ExtendedForm.SdkCommandUseNoneField {
    public SdkCommandUseNoneCreateField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandUseNoneUseField extends ExtendedForm.SdkCommandUseNoneField {
    public SdkCommandUseNoneUseField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandUseNoneIgnoreField extends ExtendedForm.SdkCommandUseNoneField {
    public SdkCommandUseNoneIgnoreField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandUseCreateNoneField extends ExtendedForm.SdkCommandUseCreateField {
    public SdkCommandUseCreateNoneField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandUseCreateCreateField extends ExtendedForm.SdkCommandUseCreateField {
    public SdkCommandUseCreateCreateField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandUseCreateUseField extends ExtendedForm.SdkCommandUseCreateField {
    public SdkCommandUseCreateUseField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandUseCreateIgnoreField extends ExtendedForm.SdkCommandUseCreateField {
    public SdkCommandUseCreateIgnoreField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandUseUseNoneField extends ExtendedForm.SdkCommandUseUseField {
    public SdkCommandUseUseNoneField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandUseUseCreateField extends ExtendedForm.SdkCommandUseUseField {
    public SdkCommandUseUseCreateField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandUseUseUseField extends ExtendedForm.SdkCommandUseUseField {
    public SdkCommandUseUseUseField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandUseUseIgnoreField extends ExtendedForm.SdkCommandUseUseField {
    public SdkCommandUseUseIgnoreField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandUseIgnoreNoneField extends ExtendedForm.SdkCommandUseIgnoreField {
    public SdkCommandUseIgnoreNoneField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandUseIgnoreCreateField extends ExtendedForm.SdkCommandUseIgnoreField {
    public SdkCommandUseIgnoreCreateField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandUseIgnoreUseField extends ExtendedForm.SdkCommandUseIgnoreField {
    public SdkCommandUseIgnoreUseField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandUseIgnoreIgnoreField extends ExtendedForm.SdkCommandUseIgnoreField {
    public SdkCommandUseIgnoreIgnoreField(BaseForm.MainBox.UsingFormDataFieldsGroupBox container) {
      super(container);
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
  public class SdkCommandIgnoreNoneNoneField extends ExtendedForm.SdkCommandIgnoreNoneField {
    public SdkCommandIgnoreNoneNoneField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandIgnoreNoneCreateField extends ExtendedForm.SdkCommandIgnoreNoneField {
    public SdkCommandIgnoreNoneCreateField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandIgnoreNoneUseField extends ExtendedForm.SdkCommandIgnoreNoneField {
    public SdkCommandIgnoreNoneUseField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandIgnoreNoneIgnoreField extends ExtendedForm.SdkCommandIgnoreNoneField {
    public SdkCommandIgnoreNoneIgnoreField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandIgnoreCreateNoneField extends ExtendedForm.SdkCommandIgnoreCreateField {
    public SdkCommandIgnoreCreateNoneField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandIgnoreCreateCreateField extends ExtendedForm.SdkCommandIgnoreCreateField {
    public SdkCommandIgnoreCreateCreateField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandIgnoreCreateUseField extends ExtendedForm.SdkCommandIgnoreCreateField {
    public SdkCommandIgnoreCreateUseField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandIgnoreCreateIgnoreField extends ExtendedForm.SdkCommandIgnoreCreateField {
    public SdkCommandIgnoreCreateIgnoreField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandIgnoreUseNoneField extends ExtendedForm.SdkCommandIgnoreUseField {
    public SdkCommandIgnoreUseNoneField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandIgnoreUseCreateField extends ExtendedForm.SdkCommandIgnoreUseField {
    public SdkCommandIgnoreUseCreateField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandIgnoreUseUseField extends ExtendedForm.SdkCommandIgnoreUseField {
    public SdkCommandIgnoreUseUseField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandIgnoreUseIgnoreField extends ExtendedForm.SdkCommandIgnoreUseField {
    public SdkCommandIgnoreUseIgnoreField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class SdkCommandIgnoreIgnoreNoneField extends ExtendedForm.SdkCommandIgnoreIgnoreField {
    public SdkCommandIgnoreIgnoreNoneField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class SdkCommandIgnoreIgnoreCreateField extends ExtendedForm.SdkCommandIgnoreIgnoreField {
    public SdkCommandIgnoreIgnoreCreateField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.USE, value = UsingFormFieldData.class)
  public class SdkCommandIgnoreIgnoreUseField extends ExtendedForm.SdkCommandIgnoreIgnoreField {
    public SdkCommandIgnoreIgnoreUseField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.IGNORE)
  public class SdkCommandIgnoreIgnoreIgnoreField extends ExtendedForm.SdkCommandIgnoreIgnoreField {
    public SdkCommandIgnoreIgnoreIgnoreField(BaseForm.MainBox.IgnoringFormDataFieldsGroupBox container) {
      super(container);
    }
  }

  /* ########################################################################
   * additional tests
   * ########################################################################
   */
  @Replace
  public class NameExExField extends NameExField {

    private String m_stringProperty;

    public NameExExField(GroupBox container) {
      super(container);
    }

    @FormData
    public String getStringProperty() {
      return m_stringProperty;
    }

    @FormData
    public void setStringProperty(String stringProperty) {
      m_stringProperty = stringProperty;
    }

    @Override
    protected boolean getConfiguredMandatory() {
      return true;
    }

    @Override
    protected int getConfiguredMaxLength() {
      return 15;
    }
  }

  @Replace
  public class IgnoringGroupBoxExNoneNoneField extends ExtendedForm.IgnoringGroupBoxExNoneField {

    public IgnoringGroupBoxExNoneNoneField(BaseForm.MainBox.IgnoringGroupBox container) {
      super(container);
    }
  }

  @Replace
  @FormData(sdkCommand = SdkCommand.CREATE)
  public class IgnoringGroupBoxExNoneCreateField extends ExtendedForm.IgnoringGroupBoxExNoneField {

    public IgnoringGroupBoxExNoneCreateField(BaseForm.MainBox.IgnoringGroupBox container) {
      super(container);
    }
  }

  @Replace
  public class IgnoringGroupBoxExCreateNoneField extends ExtendedForm.IgnoringGroupBoxExCreateField {

    public IgnoringGroupBoxExCreateNoneField(BaseForm.MainBox.IgnoringGroupBox container) {
      super(container);
    }
  }
}
