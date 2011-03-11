package formdata.shared.services.process;

import java.util.Map;
import formdata.shared.services.process.AbstractCompanyTableFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import java.util.Set;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.commons.annotations.FormDataChecksum;

@FormDataChecksum(146962376l)
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

  public class Company extends AbstractCompanyTableFieldData {
    private static final long serialVersionUID = 1L;

    public Company() {
    }

  }

  public class PersonTable extends AbstractTableFieldData {
    private static final long serialVersionUID = 1L;

    public PersonTable() {
    }

    public void setPersonNr(int row, Long personNr) {
      setValueInternal(row, 0, personNr);
    }

    public Long getPersonNr(int row) {
      return (Long) getValueInternal(row, 0);
    }

    public void setName(int row, String name) {
      setValueInternal(row, 1, name);
    }

    public String getName(int row) {
      return (String) getValueInternal(row, 1);
    }

    public void setAnObject(int row, Object anObject) {
      setValueInternal(row, 2, anObject);
    }

    public Object getAnObject(int row) {
      return getValueInternal(row, 2);
    }

    public void setSmartLong(int row, Long smartLong) {
      setValueInternal(row, 3, smartLong);
    }

    public Long getSmartLong(int row) {
      return (Long) getValueInternal(row, 3);
    }

    public void setCustom(int row, Set<Map<String, Integer>> custom) {
      setValueInternal(row, 4, custom);
    }

    public Set<Map<String, Integer>> getCustom(int row) {
      return (Set<Map<String, Integer>>) getValueInternal(row, 4);
    }

    @Override
    public int getColumnCount() {
      return 5;
    }

    @Override
    public Object getValueAt(int row, int column) {
      switch (column) {
        case 0:
          return getPersonNr(row);
        case 1:
          return getName(row);
        case 2:
          return getAnObject(row);
        case 3:
          return getSmartLong(row);
        case 4:
          return getCustom(row);
        default:
          return null;
      }
    }

    @Override
    public void setValueAt(int row, int column, Object value) {
      switch (column) {
        case 0:
          setPersonNr(row, (Long) value);
          break;
        case 1:
          setName(row, (String) value);
          break;
        case 2:
          setAnObject(row, value);
          break;
        case 3:
          setSmartLong(row, (Long) value);
          break;
        case 4:
          setCustom(row, (Set<Map<String, Integer>>) value);
          break;
      }
    }
  }
}
