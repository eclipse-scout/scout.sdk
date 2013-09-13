package sample.shared.person;

import java.util.Date;

import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

public class PersonFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public PersonFormData() {
  }

  public AnyPropertyProperty getAnyPropertyProperty() {
    return getPropertyByClass(AnyPropertyProperty.class);
  }

  /**
   * access method for property AnyProperty.
   */
  public String getAnyProperty() {
    return getAnyPropertyProperty().getValue();
  }

  /**
   * access method for property AnyProperty.
   */
  public void setAnyProperty(String anyProperty) {
    getAnyPropertyProperty().setValue(anyProperty);
  }

  public PersonIdProperty getPersonIdProperty() {
    return getPropertyByClass(PersonIdProperty.class);
  }

  /**
   * access method for property PersonId.
   */
  public Long getPersonId() {
    return getPersonIdProperty().getValue();
  }

  /**
   * access method for property PersonId.
   */
  public void setPersonId(Long personId) {
    getPersonIdProperty().setValue(personId);
  }

  public Calendar getCalendar() {
    return getFieldByClass(Calendar.class);
  }

  public Composer getComposer() {
    return getFieldByClass(Composer.class);
  }

  public EmptyTable getEmptyTable() {
    return getFieldByClass(EmptyTable.class);
  }

  public Table getTable() {
    return getFieldByClass(Table.class);
  }

  public class AnyPropertyProperty extends AbstractPropertyData<String> {
    private static final long serialVersionUID = 1L;

    public AnyPropertyProperty() {
    }
  }

  public class PersonIdProperty extends AbstractPropertyData<Long> {
    private static final long serialVersionUID = 1L;

    public PersonIdProperty() {
    }
  }

  public static class Calendar extends AbstractValueFieldData<Date> {
    private static final long serialVersionUID = 1L;

    public Calendar() {
    }
  }

  public static class Composer extends AbstractComposerData {
    private static final long serialVersionUID = 1L;

    public Composer() {
    }
  }

  public static class EmptyTable extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public EmptyTable() {
    }
  }

  public static class Table extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public Table() {
    }

    public static final int DOUBLE_COLUMN_ID = 0;
    public static final int LONG_COLUMN_ID = 1;
    public static final int STRING_COLUMN_ID = 2;

    public void setDouble(int row, Double doubleValue) {
      setValueInternal(row, DOUBLE_COLUMN_ID, doubleValue);
    }

    public Double getDouble(int row) {
      return (Double) getValueInternal(row, DOUBLE_COLUMN_ID);
    }

    public void setLong(int row, Long longValue) {
      setValueInternal(row, LONG_COLUMN_ID, longValue);
    }

    public Long getLong(int row) {
      return (Long) getValueInternal(row, LONG_COLUMN_ID);
    }

    public void setString(int row, String string) {
      setValueInternal(row, STRING_COLUMN_ID, string);
    }

    public String getString(int row) {
      return (String) getValueInternal(row, STRING_COLUMN_ID);
    }

    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case DOUBLE_COLUMN_ID:
          return getDouble(row);
        case LONG_COLUMN_ID:
          return getLong(row);
        case STRING_COLUMN_ID:
          return getString(row);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(int row, int column, Object value) {
      switch (column) {
        case DOUBLE_COLUMN_ID:
          setDouble(row, (Double) value);
          break;
        case LONG_COLUMN_ID:
          setLong(row, (Long) value);
          break;
        case STRING_COLUMN_ID:
          setString(row, (String) value);
          break;
      }
    }
  }
}
