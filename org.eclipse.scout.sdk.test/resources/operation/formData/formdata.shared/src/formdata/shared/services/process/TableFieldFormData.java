package formdata.shared.services.process;

import java.util.Map;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import java.util.Set;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;

public class TableFieldFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public TableFieldFormData() {
  }

  public Company getCompany() {
    return getFieldByClass(Company.class);
  }

  public PersonTable getPersonTable() {
    return getFieldByClass(PersonTable.class);
  }

  public static class Company extends AbstractCompanyTableFieldData {
    private static final long serialVersionUID = 1L;

    public Company() {
    }
  }

  public static class PersonTable extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public PersonTable() {
    }

    public static final int PERSON_NR_COLUMN_ID = 0;
    public static final int NAME_COLUMN_ID = 1;
    public static final int AN_OBJECT_COLUMN_ID = 2;
    public static final int SMART_LONG_COLUMN_ID = 3;
    public static final int CUSTOM_COLUMN_ID = 4;

    public void setPersonNr(int row, Long personNr) {
      setValueInternal(row, PERSON_NR_COLUMN_ID, personNr);
    }

    public Long getPersonNr(int row) {
      return (Long) getValueInternal(row, PERSON_NR_COLUMN_ID);
    }

    public void setName(int row, String name) {
      setValueInternal(row, NAME_COLUMN_ID, name);
    }

    public String getName(int row) {
      return (String) getValueInternal(row, NAME_COLUMN_ID);
    }

    public void setAnObject(int row, Object anObject) {
      setValueInternal(row, AN_OBJECT_COLUMN_ID, anObject);
    }

    public Object getAnObject(int row) {
      return getValueInternal(row, AN_OBJECT_COLUMN_ID);
    }

    public void setSmartLong(int row, Long smartLong) {
      setValueInternal(row, SMART_LONG_COLUMN_ID, smartLong);
    }

    public Long getSmartLong(int row) {
      return (Long) getValueInternal(row, SMART_LONG_COLUMN_ID);
    }

    public void setCustom(int row, Set<Map<String, Integer>> custom) {
      setValueInternal(row, CUSTOM_COLUMN_ID, custom);
    }

    public Set<Map<String, Integer>> getCustom(int row) {
      return (Set<Map<String, Integer>>) getValueInternal(row, CUSTOM_COLUMN_ID);
    }

    @Override
    public int getColumnCount() {
      return 5;
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case PERSON_NR_COLUMN_ID:
          return getPersonNr(row);
        case NAME_COLUMN_ID:
          return getName(row);
        case AN_OBJECT_COLUMN_ID:
          return getAnObject(row);
        case SMART_LONG_COLUMN_ID:
          return getSmartLong(row);
        case CUSTOM_COLUMN_ID:
          return getCustom(row);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(int row, int column, Object value) {
      switch (column) {
        case PERSON_NR_COLUMN_ID:
          setPersonNr(row, (Long) value);
          break;
        case NAME_COLUMN_ID:
          setName(row, (String) value);
          break;
        case AN_OBJECT_COLUMN_ID:
          setAnObject(row, value);
          break;
        case SMART_LONG_COLUMN_ID:
          setSmartLong(row, (Long) value);
          break;
        case CUSTOM_COLUMN_ID:
          setCustom(row, (Set<Map<String, Integer>>) value);
          break;
      }
    }
  }
}
