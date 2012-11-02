package formdata.client.ui.forms;

import java.util.Map;
import java.util.Set;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractObjectColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;

import formdata.client.ui.forms.TableFieldForm.MainBox.CompanyField;
import formdata.client.ui.forms.TableFieldForm.MainBox.PersonTableField;
import formdata.client.ui.template.formfield.AbstractCompanyTableField;
import formdata.shared.services.process.TableFieldFormData;

@FormData(value = TableFieldFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldForm extends AbstractForm {

  public TableFieldForm() throws ProcessingException {
    super();
  }

  public CompanyField getCompanyField() {
    return getFieldByClass(CompanyField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public PersonTableField getPersonTableField() {
    return getFieldByClass(PersonTableField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class PersonTableField extends AbstractTableField<PersonTableField.Table> {

      @Order(10.0)
      public class Table extends AbstractTable {

        public AnObjectColumn getAnObjectColumn() {
          return getColumnSet().getColumnByClass(AnObjectColumn.class);
        }

        public NameColumn getNameColumn() {
          return getColumnSet().getColumnByClass(NameColumn.class);
        }

        public PersonNrColumn getPersonNrColumn() {
          return getColumnSet().getColumnByClass(PersonNrColumn.class);
        }

        public SmartLongColumn getSmartLongColumn() {
          return getColumnSet().getColumnByClass(SmartLongColumn.class);
        }

        public CustomColumn getCustomColumn() {
          return getColumnSet().getColumnByClass(CustomColumn.class);
        }

        @Order(10.0)
        public class PersonNrColumn extends AbstractLongColumn {
        }

        @Order(20.0)
        public class NameColumn extends AbstractStringColumn {
        }

        @Order(30.0)
        public class AnObjectColumn extends AbstractObjectColumn {
        }

        @Order(40.0)
        public class SmartLongColumn extends AbstractSmartColumn<Long> {
        }

        @Order(50.0)
        public class CustomColumn extends AbstractColumn<Set<Map<String, Integer>>> {
        }
      }
    }

    @Order(20.0)
    public class CompanyField extends AbstractCompanyTableField {

    }

  }
}
