/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.forms.replace;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

import formdata.client.ui.template.formfield.AbstractAddressTableField;
import formdata.client.ui.template.formfield.AbstractPersonTableField;
import formdata.shared.services.process.replace.TableFieldBaseFormData;

@FormData(value = TableFieldBaseFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldBaseForm extends AbstractForm {

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    @FormData(sdkCommand = SdkCommand.USE, value = AbstractTableFieldBeanData.class)
    public class TableField extends AbstractTableField<TableField.Table> {

      public class Table extends AbstractTable {

        @Order(10.0)
        public class FirstColumn extends AbstractStringColumn {
        }

        @Order(20.0)
        public class SecondColumn extends AbstractStringColumn {

        }
      }
    }

    @Order(20.0)
    @FormData(sdkCommand = SdkCommand.USE, value = AbstractTableFieldBeanData.class)
    public class EmptyTableField extends AbstractTableField<EmptyTableField.Table> {

      public class Table extends AbstractTable {
      }
    }

    @Order(30.0)
    @FormData(sdkCommand = SdkCommand.USE, value = AbstractTableFieldBeanData.class)
    public class NoTableField extends AbstractTableField<ITable> {
    }

    @Order(40.0)
    public class AddressTableField extends AbstractAddressTableField {

      public class Table extends AbstractAddressTableField.Table {

        public CityColumn getCityColumn() {
          return getColumnSet().getColumnByClass(CityColumn.class);
        }

        @Order(50)
        public class CityColumn extends AbstractStringColumn {

        }
      }
    }

    @Order(40.0)
    public class PersonTableField extends AbstractPersonTableField {
    }
  }
}
