package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;
import org.eclipse.scout.commons.annotations.FormDataChecksum;

@FormDataChecksum(1711980225l)
public abstract class AbstractCompanyTableFieldData extends AbstractTableFieldData {
  private static final long serialVersionUID = 1L;

  public AbstractCompanyTableFieldData() {
  }

  public void setName(int row, String name) {
    setValueInternal(row, 0, name);
  }

  public String getName(int row) {
    return (String) getValueInternal(row, 1);
  }

  @Override
  public int getColumnCount() {
    return 1;
  }

  @Override
  public Object getValueAt(int row, int column) {
    switch (column) {
      case 0:
        return getName(row);
      default:
        return null;
    }
  }

  @Override
  public void setValueAt(int row, int column, Object value) {
    switch (column) {
      case 0:
        setName(row, (String) value);
        break;
    }
  }
}
