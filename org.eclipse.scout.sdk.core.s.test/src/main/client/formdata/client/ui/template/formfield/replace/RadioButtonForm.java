/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.client.ui.template.formfield.replace;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

import formdata.client.ui.template.formfield.replace.RadioButtonForm.MainBox.UsedRadioButtonGroup;
import formdata.client.ui.template.formfield.replace.RadioButtonForm.MainBox.UsedRadioButtonGroup.InputExStringField;
import formdata.client.ui.template.formfield.replace.RadioButtonForm.MainBox.UsedRadioButtonGroup.UsageOneUsualStringField;
import formdata.client.ui.template.formfield.replace.RadioButtonForm.MainBox.UsualRadioButtonGroup;
import formdata.client.ui.template.formfield.replace.RadioButtonForm.MainBox.UsualRadioButtonGroup.UsualOneButton;
import formdata.client.ui.template.formfield.replace.RadioButtonForm.MainBox.UsualRadioButtonGroup.UsualStringField;
import formdata.client.ui.template.formfield.replace.RadioButtonForm.MainBox.UsualRadioButtonGroup.UsualTwoButton;
import formdata.shared.services.process.replace.RadioButtonFormData;

/**
 * @since 3.10.0-M5
 */
@FormData(value = RadioButtonFormData.class, sdkCommand = SdkCommand.CREATE)
public class RadioButtonForm extends AbstractForm {

  public RadioButtonForm() throws ProcessingException {
    super();
  }

  @Override
  public MainBox getRootGroupBox() {
    return (MainBox) super.getRootGroupBox();
  }

  public UsualRadioButtonGroup getUsualRadioButtonGroup() {
    return getFieldByClass(UsualRadioButtonGroup.class);
  }

  public UsualOneButton getUsualOneButton() {
    return getFieldByClass(UsualOneButton.class);
  }

  public UsualTwoButton getUsualTwoButton() {
    return getFieldByClass(UsualTwoButton.class);
  }

  public UsualStringField getUsualStringField() {
    return getFieldByClass(UsualStringField.class);
  }

  public UsedRadioButtonGroup getUsedRadioButtonGroup() {
    return getFieldByClass(UsedRadioButtonGroup.class);
  }

  public InputExStringField getInputExStringField() {
    return getFieldByClass(InputExStringField.class);
  }

  public UsageOneUsualStringField getUsageOneUsualStringField() {
    return getFieldByClass(UsageOneUsualStringField.class);
  }

  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class UsualRadioButtonGroup extends AbstractRadioButtonGroup<String> {

      @Order(10)
      public class UsualOneButton extends AbstractRadioButton<String> {
        @Override
        protected String getConfiguredRadioValue() {
          return "one";
        }
      }

      @Order(20)
      public class UsualTwoButton extends AbstractRadioButton<String> {
        @Override
        protected String getConfiguredRadioValue() {
          return "tow";
        }
      }

      @Order(30)
      public class UsualStringField extends AbstractStringField {
      }
    }

    @Order(20)
    public class UsedRadioButtonGroup extends AbstractRadioButtonGroupWithFields {

      @Replace
      public class InputExStringField extends InputStringField {
      }

      @Order(50)
      public class UsageOneUsualStringField extends AbstractStringField {
      }
    }
  }
}
