package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;

public abstract class AbstractCompanyTableFieldData extends AbstractTableFieldData {
  private static final long serialVersionUID = 1L;

  public AbstractCompanyTableFieldData() {
  }

  public static final int NAME_COLUMN_ID = 0;

  public void setName(int row, String name) {
    setValueInternal(row, NAME_COLUMN_ID, name);
  }

  public String getName(int row) {
    return (String) getValueInternal(row, NAME_COLUMN_ID);
  }

  @Override
  public int getColumnCount() {
    return 1;
  }

  @Override
  public Object getValueAt(int row, int column) {
    switch (column) {
      case NAME_COLUMN_ID:
        return getName(row);
      default:
        return null;
    }
  }

  @Override
  public void setValueAt(int row, int column, Object value) {
    switch (column) {
      case NAME_COLUMN_ID:
        setName(row, (String) value);
        break;
    }
  }
}
