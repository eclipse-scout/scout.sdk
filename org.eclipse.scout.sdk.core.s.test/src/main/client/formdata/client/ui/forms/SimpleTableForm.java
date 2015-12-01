package formdata.client.ui.forms;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

import formdata.client.ui.forms.SimpleTableForm.MainBox.TestTableField.Table;
import formdata.shared.services.process.SimpleTableFormData;

@FormData(value = SimpleTableFormData.class, sdkCommand = SdkCommand.CREATE)
public class SimpleTableForm extends AbstractForm {

  public SimpleTableForm() throws ProcessingException {
    super();
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class TestTableField extends AbstractTableField<Table> {
      public class Table extends AbstractTable {
        @Order(10.0)
        public class NameColumn extends AbstractStringColumn {
        }
      }
    }
  }
}
