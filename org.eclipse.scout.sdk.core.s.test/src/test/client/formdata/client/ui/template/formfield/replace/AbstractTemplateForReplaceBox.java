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
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.services.process.replace.AbstractTemplateForReplaceBoxData;

/**
 * @since 3.10.0-M5
 */
@FormData(value = AbstractTemplateForReplaceBoxData.class, sdkCommand = SdkCommand.CREATE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractTemplateForReplaceBox extends AbstractGroupBox {

  public TemplateStringField getTemplateStringField() {
    return getFieldByClass(TemplateStringField.class);
  }

  public TemplateBox getTemplateBox() {
    return getFieldByClass(TemplateBox.class);
  }

  public TemplateTableField getTemplateTableField() {
    return getFieldByClass(TemplateTableField.class);
  }

  @Order(10)
  public class TemplateStringField extends AbstractStringField {
  }

  @Order(20)
  public class TemplateBox extends AbstractGroupBox {

    @Order(10)
    public class TemplateBoxStringField extends AbstractStringField {
    }
  }

  @Order(30)
  public class TemplateTableField extends AbstractTableField<TemplateTableField.Table> {

    public class Table extends AbstractTable {

      public FirstColumn getFirstColumn() {
        return getColumnSet().getColumnByClass(FirstColumn.class);
      }

      public SecondColumn getSecondColumn() {
        return getColumnSet().getColumnByClass(SecondColumn.class);
      }

      public class FirstColumn extends AbstractStringColumn {
      }

      public class SecondColumn extends AbstractStringColumn {
      }
    }
  }
}
