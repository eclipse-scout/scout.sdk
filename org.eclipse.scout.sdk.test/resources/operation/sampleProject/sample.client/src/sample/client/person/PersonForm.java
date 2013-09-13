package sample.client.person;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDoubleColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.extension.client.ui.basic.calendar.AbstractExtensibleCalendar;
import org.eclipse.scout.rt.extension.client.ui.basic.calendar.provider.AbstractExtensibleCalendarItemProvider;
import org.eclipse.scout.rt.extension.client.ui.basic.table.AbstractExtensibleTable;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.AbstractDataModelEntity;
import org.eclipse.scout.service.SERVICES;

import sample.client.person.PersonForm.MainBox.CalendarField;
import sample.client.person.PersonForm.MainBox.CancelButton;
import sample.client.person.PersonForm.MainBox.ComposerField;
import sample.client.person.PersonForm.MainBox.EmptyTableField;
import sample.client.person.PersonForm.MainBox.OkButton;
import sample.client.person.PersonForm.MainBox.TableField;
import sample.shared.person.IPersonService;
import sample.shared.person.PersonFormData;
import sample.shared.person.UpdatePersonPermission;

@FormData(value = PersonFormData.class, sdkCommand = SdkCommand.CREATE)
public class PersonForm extends AbstractForm {

  private Long m_personId;
  private String m_anyProperty;

  public PersonForm() throws ProcessingException {
    super();
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Person");
  }

  @FormData
  public String getAnyProperty() {
    return m_anyProperty;
  }

  @FormData
  public void setAnyProperty(String anyProperty) {
    m_anyProperty = anyProperty;
  }

  @FormData
  public Long getPersonId() {
    return m_personId;
  }

  @FormData
  public void setPersonId(Long personId) {
    m_personId = personId;
  }

  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  public void startNew() throws ProcessingException {
    startInternal(new NewHandler());
  }

  public CalendarField getCalendarField() {
    return getFieldByClass(CalendarField.class);
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public ComposerField getComposerField() {
    return getFieldByClass(ComposerField.class);
  }

  public EmptyTableField getEmptyTableField() {
    return getFieldByClass(EmptyTableField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public TableField getTableField() {
    return getFieldByClass(TableField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class CalendarField extends AbstractCalendarField<CalendarField.Calendar> {

      @Override
      protected String getConfiguredLabel() {
        return TEXTS.get("Calendar");
      }

      @Order(10.0)
      public class Calendar extends AbstractExtensibleCalendar {

        @Order(10.0)
        public class FirstItemProvider extends AbstractExtensibleCalendarItemProvider {
        }
      }
    }

    @Order(20.0)
    public class ComposerField extends AbstractComposerField {

      public class FirstEntry extends AbstractDataModelEntity {

        private static final long serialVersionUID = 1L;

        @Override
        protected String getConfiguredText() {
          return TEXTS.get("First");
        }
      }

      public class FirstAttribute extends AbstractDataModelAttribute {

        private static final long serialVersionUID = 1L;
      }
    }

    @Order(30.0)
    public class EmptyTableField extends AbstractTableField<EmptyTableField.Table> {

      @Order(10.0)
      public class Table extends AbstractExtensibleTable {
      }
    }

    @Order(40.0)
    public class TableField extends AbstractTableField<TableField.Table> {

      @Order(10.0)
      public class Table extends AbstractExtensibleTable {

        public LongColumn getLongColumn() {
          return getColumnSet().getColumnByClass(LongColumn.class);
        }

        public StringColumn getStringColumn() {
          return getColumnSet().getColumnByClass(StringColumn.class);
        }

        public DoubleColumn getDoubleColumn() {
          return getColumnSet().getColumnByClass(DoubleColumn.class);
        }

        @Order(10.0)
        public class DoubleColumn extends AbstractDoubleColumn {
        }

        @Order(20.0)
        public class LongColumn extends AbstractLongColumn {
        }

        @Order(30.0)
        public class StringColumn extends AbstractStringColumn {
        }
      }
    }

    @Order(50.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(60.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class ModifyHandler extends AbstractFormHandler {

    @Override
    public void execLoad() throws ProcessingException {
      IPersonService service = SERVICES.getService(IPersonService.class);
      PersonFormData formData = new PersonFormData();
      exportFormData(formData);
      formData = service.load(formData);
      importFormData(formData);
      setEnabledPermission(new UpdatePersonPermission());
    }

    @Override
    public void execStore() throws ProcessingException {
      IPersonService service = SERVICES.getService(IPersonService.class);
      PersonFormData formData = new PersonFormData();
      exportFormData(formData);
      formData = service.store(formData);
    }
  }

  public class NewHandler extends AbstractFormHandler {

    @Override
    public void execLoad() throws ProcessingException {
      IPersonService service = SERVICES.getService(IPersonService.class);
      PersonFormData formData = new PersonFormData();
      exportFormData(formData);
      formData = service.prepareCreate(formData);
      importFormData(formData);
    }

    @Override
    public void execStore() throws ProcessingException {
      IPersonService service = SERVICES.getService(IPersonService.class);
      PersonFormData formData = new PersonFormData();
      exportFormData(formData);
      formData = service.create(formData);
    }
  }
}
