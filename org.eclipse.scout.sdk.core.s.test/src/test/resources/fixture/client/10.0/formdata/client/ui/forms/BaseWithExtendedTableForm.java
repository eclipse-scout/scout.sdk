/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.forms;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.table.AbstractTestTableWithOneColumn;
import formdata.client.ui.forms.BaseWithExtendedTableForm.MainBox.CancelButton;
import formdata.client.ui.forms.BaseWithExtendedTableForm.MainBox.OkButton;
import formdata.client.ui.forms.BaseWithExtendedTableForm.MainBox.TableInFormField;
import formdata.shared.services.BaseWithExtendedTableFormData;

/**
 *
 */
@FormData(value = BaseWithExtendedTableFormData.class, sdkCommand = SdkCommand.CREATE)
public class BaseWithExtendedTableForm extends AbstractForm {

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public TableInFormField getTableInFormField() {
    return getFieldByClass(TableInFormField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class TableInFormField extends AbstractTableField<TableInFormField.Table> {
      @Order(10.0)
      public class Table extends AbstractTestTableWithOneColumn {
        @Order(20.0)
        public class ColInDesktopForm extends AbstractStringColumn {
        }
      }
    }

    @Order(20.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(30.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }
}
