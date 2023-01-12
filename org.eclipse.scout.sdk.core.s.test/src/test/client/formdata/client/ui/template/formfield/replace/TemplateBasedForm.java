/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.template.formfield.replace;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

import formdata.client.ui.template.formfield.replace.TemplateBasedForm.MainBox.UsageOneBox;
import formdata.client.ui.template.formfield.replace.TemplateBasedForm.MainBox.UsageOneBox.UsageOneStringField;
import formdata.client.ui.template.formfield.replace.TemplateBasedForm.MainBox.UsageTwoBox;
import formdata.client.ui.template.formfield.replace.TemplateBasedForm.MainBox.UsualStringField;
import formdata.shared.services.process.replace.TemplateBasedFormData;

/**
 * @since 3.10.0-M5
 */
@FormData(value = TemplateBasedFormData.class, sdkCommand = SdkCommand.CREATE)
public class TemplateBasedForm extends AbstractForm {

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public UsualStringField getUsualStringField() {
    return getFieldByClass(UsualStringField.class);
  }

  public UsageOneBox getUsageOneBox() {
    return getFieldByClass(UsageOneBox.class);
  }

  public UsageTwoBox getUsageTwoBox() {
    return getFieldByClass(UsageTwoBox.class);
  }

  public UsageOneStringField getUsageOneStringField() {
    return getFieldByClass(UsageOneStringField.class);
  }

  public class MainBox extends AbstractGroupBox {

    public class UsualStringField extends AbstractStringField {

    }

    @Order(20)
    public class UsageOneBox extends AbstractTemplateForReplaceBox {

      @Order(20)
      public class UsageOneStringField extends AbstractStringField {
      }
    }

    @Order(30)
    public class UsageTwoBox extends AbstractTemplateForReplaceBox {

      @Replace
      public class UsageTwoTemplateTableField extends TemplateTableField {

        public class UsageTwoTable extends TemplateTableField.Table {

          public UsageTwoFirstColumn getUsageTwoFirstColumn() {
            return getColumnSet().getColumnByClass(UsageTwoFirstColumn.class);
          }

          public ThirdColumn getThirdColumn() {
            return getColumnSet().getColumnByClass(ThirdColumn.class);
          }

          @Replace
          public class UsageTwoFirstColumn extends TemplateTableField.Table.FirstColumn {
          }

          @Order(30)
          public class ThirdColumn extends AbstractStringColumn {
          }
        }
      }
    }
  }
}
