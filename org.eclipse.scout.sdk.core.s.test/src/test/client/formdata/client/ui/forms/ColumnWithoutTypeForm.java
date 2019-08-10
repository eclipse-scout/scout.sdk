/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.ui.forms;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.ui.forms.ColumnWithoutTypeFormData;

/**
 *
 */
@FormData(value = ColumnWithoutTypeFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ColumnWithoutTypeForm extends AbstractForm {

  @Order(1000)
  public class MainBox extends AbstractGroupBox {

    @Order(2000)
    public class MyTableField extends AbstractTableField<MyTableField.Table> {
      public class Table extends AbstractTable {

        public MyColumn getMyColumn() {
          return getColumnSet().getColumnByClass(MyColumn.class);
        }

        @Order(1000)
        @SuppressWarnings("rawtypes")
        public class MyColumn extends AbstractColumn {
        }
      }
    }

    @Order(3000)
    public class OkButton extends AbstractOkButton {
    }

    @Order(4000)
    public class CancelButton extends AbstractCancelButton {
    }
  }
}
