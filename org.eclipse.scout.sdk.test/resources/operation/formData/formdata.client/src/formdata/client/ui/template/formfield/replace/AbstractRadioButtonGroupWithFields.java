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
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.AbstractRadioButtonGroup;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

import formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData;

/**
 * @since 3.10.0-M5
 */
@FormData(value = AbstractRadioButtonGroupWithFieldsData.class, sdkCommand = SdkCommand.CREATE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractRadioButtonGroupWithFields extends AbstractRadioButtonGroup<Long> {

  public OptionOneButton getOptionOneButton() {
    return getFieldByClass(OptionOneButton.class);
  }

  public OptionTwoButton getOptionTwoButton() {
    return getFieldByClass(OptionTwoButton.class);
  }

  public InputStringField getInputStringField() {
    return getFieldByClass(InputStringField.class);
  }

  @Order(10)
  public class OptionOneButton extends AbstractRadioButton {

    @Override
    protected Long getConfiguredRadioValue() {
      return 1L;
    }
  }

  @Order(20)
  public class OptionTwoButton extends AbstractRadioButton {

    @Override
    protected Long getConfiguredRadioValue() {
      return 2L;
    }
  }

  @Order(30)
  public class InputStringField extends AbstractStringField {
  }
}
