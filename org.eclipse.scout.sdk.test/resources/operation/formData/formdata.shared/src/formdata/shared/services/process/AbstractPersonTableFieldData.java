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

    public static final String personId = "personId";
    public static final String name = "name";
    public static final String female = "female";
    public static final String boolean_ = "boolean";
    public static final String assert_ = "assert";
    public static final String switch_ = "switch";
    private String m_personId;
    private String m_name;
    private Boolean m_female;
    private Boolean m_booleanValue;
    private Boolean m_assertValue;
    private Boolean m_switchValue;

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

    public Boolean getBoolean() {
      return m_booleanValue;
    }

    public void setBoolean(Boolean booleanValue) {
      m_booleanValue = booleanValue;
    }

    public Boolean getAssert() {
      return m_assertValue;
    }

    public void setAssert(Boolean assertValue) {
      m_assertValue = assertValue;
    }

    public Boolean getSwitch() {
      return m_switchValue;
    }

    public void setSwitch(Boolean switchValue) {
      m_switchValue = switchValue;
    }
  }
}
