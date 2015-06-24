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
package formdata.client.ui.forms.replace;

import org.eclipse.scout.commons.annotations.ColumnData;
import org.eclipse.scout.commons.annotations.ColumnData.SdkColumnCommand;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

import formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsBaseForm.MainBox.TableBaseField;
import formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData;

@FormData(value = TableFieldWithIgnoredColumnsBaseFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldWithIgnoredColumnsBaseForm extends AbstractForm {

  public TableFieldWithIgnoredColumnsBaseForm() throws ProcessingException {
    super();
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public TableBaseField getTableBaseField() {
    return getFieldByClass(TableBaseField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    @FormData(sdkCommand = SdkCommand.USE, value = AbstractTableFieldBeanData.class)
    public class TableBaseField extends AbstractTableField<TableBaseField.Table> {

      public class Table extends AbstractTable {

        public DefaultColumn getDefaultColumn() {
          return getColumnSet().getColumnByClass(DefaultColumn.class);
        }

        public IgnoreColumn getIgnoreColumn() {
          return getColumnSet().getColumnByClass(IgnoreColumn.class);
        }

        public CreateColumn getCreateColumn() {
          return getColumnSet().getColumnByClass(CreateColumn.class);
        }

        @Order(10.0)
        public class DefaultColumn extends AbstractStringColumn {
        }

        @Order(20.0)
        @ColumnData(SdkColumnCommand.CREATE)
        public class CreateColumn extends AbstractStringColumn {
        }

        @Order(30.0)
        @ColumnData(SdkColumnCommand.IGNORE)
        public class IgnoreColumn extends AbstractStringColumn {
        }
      }
    }
  }
}
