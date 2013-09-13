package jdt.test.client;

import jdt.test.client.TableTestForm.MainBox.TableField;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.extension.client.ui.basic.table.AbstractExtensibleTable;

public class TableTestForm extends AbstractForm {

  public TableTestForm() throws ProcessingException {
    super();
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public TableField getTableField() {
    return getFieldByClass(TableField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class TableField extends AbstractTableField<TableField.Table> {

      @Order(10.0)
      public class Table extends AbstractExtensibleTable {

        public DateColumn getDateColumn() {
          return getColumnSet().getColumnByClass(DateColumn.class);
        }

        public IntegerColumn getIntegerColumn() {
          return getColumnSet().getColumnByClass(IntegerColumn.class);
        }

        @Order(10.0)
        public class IntegerColumn extends AbstractIntegerColumn {
        }

        @Order(20.0)
        public class DateColumn extends AbstractDateColumn {
        }
      }
    }

  }
}
