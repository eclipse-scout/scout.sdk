package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

public abstract class AbstractPersonTableFieldData extends AbstractTableFieldBeanData {
  private static final long serialVersionUID = 1L;

  public AbstractPersonTableFieldData() {
  }

  @Override
  public AbstractPersonTableRowData[] getRows() {
    return (AbstractPersonTableRowData[]) super.getRows();
  }

  public void setRows(AbstractPersonTableRowData[] rows) {
    super.setRows(rows);
  }

  @Override
  public AbstractPersonTableRowData addRow() {
    return (AbstractPersonTableRowData) super.addRow();
  }

  @Override
  public AbstractPersonTableRowData addRow(int rowState) {
    return (AbstractPersonTableRowData) super.addRow(rowState);
  }

  @Override
  public AbstractPersonTableRowData rowAt(int idx) {
    return (AbstractPersonTableRowData) super.rowAt(idx);
  }

  @Override
  public AbstractPersonTableRowData createRow() {
    return new AbstractPersonTableRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return AbstractPersonTableRowData.class;
  }

  public static class AbstractPersonTableRowData extends AbstractTableRowData {
    private static final long serialVersionUID = 1L;

    public AbstractPersonTableRowData() {
    }

    public static final String PROP_PERSON_ID = "personId";
    public static final String PROP_NAME = "name";
    public static final String PROP_FEMALE = "female";
    private String m_personId;
    private String m_name;
    private Boolean m_female;

    public String getPersonId() {
      return m_personId;
    }

    public void setPersonId(String personId) {
      m_personId = personId;
    }

    public String getName() {
      return m_name;
    }

    public void setName(String name) {
      m_name = name;
    }

    public Boolean getFemale() {
      return m_female;
    }

    public void setFemale(Boolean female) {
      m_female = female;
    }
  }
}
