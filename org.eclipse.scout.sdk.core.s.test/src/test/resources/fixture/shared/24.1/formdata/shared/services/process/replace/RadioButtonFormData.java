/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.services.process.replace;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;

import formdata.shared.services.process.replace.AbstractRadioButtonGroupWithFieldsData.InputString;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.template.formfield.replace.RadioButtonForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class RadioButtonFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public InputExString getInputExString() {
    return getFieldByClass(InputExString.class);
  }

  public UsageOneUsualString getUsageOneUsualString() {
    return getFieldByClass(UsageOneUsualString.class);
  }

  public UsedRadioButtonGroup getUsedRadioButtonGroup() {
    return getFieldByClass(UsedRadioButtonGroup.class);
  }

  public UsualRadioButtonGroup getUsualRadioButtonGroup() {
    return getFieldByClass(UsualRadioButtonGroup.class);
  }

  public UsualString getUsualString() {
    return getFieldByClass(UsualString.class);
  }

  @Replace
  public static class InputExString extends InputString {

    private static final long serialVersionUID = 1L;
  }

  public static class UsageOneUsualString extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class UsedRadioButtonGroup extends AbstractRadioButtonGroupWithFieldsData {

    private static final long serialVersionUID = 1L;
  }

  public static class UsualRadioButtonGroup extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }

  public static class UsualString extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;
  }
}
