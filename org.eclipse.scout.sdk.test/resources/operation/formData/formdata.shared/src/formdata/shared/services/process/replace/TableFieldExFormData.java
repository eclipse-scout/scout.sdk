package formdata.shared.services.process.replace;

import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

import formdata.shared.services.process.AbstractPersonTableFieldData;

public class TableFieldExFormData extends TableFieldBaseFormData {
  private static final long serialVersionUID = 1L;

  public TableFieldExFormData() {
  }

  public EmptyTableExtended getEmptyTableExtended() {
    return getFieldByClass(EmptyTableExtended.class);
  }

  public ExtendedAddress getExtendedAddress() {
    return getFieldByClass(ExtendedAddress.class);
  }

  public ExtendedPersonTable getExtendedPersonTable() {
    return getFieldByClass(ExtendedPersonTable.class);
  }

  public NoTableExtended getNoTableExtended() {
    return getFieldByClass(NoTableExtended.class);
  }

  public TableExtended getTableExtended() {
    return getFieldByClass(TableExtended.class);
  }

  @Replace
  public static class EmptyTableExtended extends TableFieldBaseFormData.EmptyTable {
    private static final long serialVersionUID = 1L;

    public EmptyTableExtended() {
    }

    @Override
    public TableExRowData[] getRows() {
      return (TableExRowData[]) super.getRows();
    }

    public void setRows(TableExRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public TableExRowData addRow() {
      return (TableExRowData) super.addRow();
    }

    @Override
    public TableExRowData addRow(int rowState) {
      return (TableExRowData) super.addRow(rowState);
    }

    @Override
    public TableExRowData rowAt(int idx) {
      return (TableExRowData) super.rowAt(idx);
    }

    @Override
    public TableExRowData createRow() {
      return new TableExRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableExRowData.class;
    }

    public static class TableExRowData extends TableFieldBaseFormData.EmptyTable.EmptyTableRowData {
      private static final long serialVersionUID = 1L;

      public TableExRowData() {
      }

      public static final String single = "single";
      private String m_single;

      public String getSingle() {
        return m_single;
      }

      public void setSingle(String single) {
        m_single = single;
      }
    }
  }

  @Replace
  public static class ExtendedAddress extends TableFieldBaseFormData.AddressTable {
    private static final long serialVersionUID = 1L;

    public ExtendedAddress() {
    }

    @Override
    public ExtendedAddressRowData[] getRows() {
      return (ExtendedAddressRowData[]) super.getRows();
    }

    public void setRows(ExtendedAddressRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public ExtendedAddressRowData addRow() {
      return (ExtendedAddressRowData) super.addRow();
    }

    @Override
    public ExtendedAddressRowData addRow(int rowState) {
      return (ExtendedAddressRowData) super.addRow(rowState);
    }

    @Override
    public ExtendedAddressRowData rowAt(int idx) {
      return (ExtendedAddressRowData) super.rowAt(idx);
    }

    @Override
    public ExtendedAddressRowData createRow() {
      return new ExtendedAddressRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return ExtendedAddressRowData.class;
    }

    public static class ExtendedAddressRowData extends TableFieldBaseFormData.AddressTable.AddressTableRowData {
      private static final long serialVersionUID = 1L;

      public ExtendedAddressRowData() {
      }

      public static final String state = "state";
      private String m_state;

      public String getState() {
        return m_state;
      }

      public void setState(String state) {
        m_state = state;
      }
    }
  }

  @Replace
  public static class ExtendedPersonTable extends TableFieldBaseFormData.PersonTable {
    private static final long serialVersionUID = 1L;

    public ExtendedPersonTable() {
    }

    @Override
    public ExtendedPersonTableRowData[] getRows() {
      return (ExtendedPersonTableRowData[]) super.getRows();
    }

    public void setRows(ExtendedPersonTableRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public ExtendedPersonTableRowData addRow() {
      return (ExtendedPersonTableRowData) super.addRow();
    }

    @Override
    public ExtendedPersonTableRowData addRow(int rowState) {
      return (ExtendedPersonTableRowData) super.addRow(rowState);
    }

    @Override
    public ExtendedPersonTableRowData rowAt(int idx) {
      return (ExtendedPersonTableRowData) super.rowAt(idx);
    }

    @Override
    public ExtendedPersonTableRowData createRow() {
      return new ExtendedPersonTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return ExtendedPersonTableRowData.class;
    }

    public static class ExtendedPersonTableRowData extends AbstractPersonTableFieldData.AbstractPersonTableRowData {
      private static final long serialVersionUID = 1L;

      public ExtendedPersonTableRowData() {
      }

      public static final String lastName = "lastName";
      private String m_lastName;

      public String getLastName() {
        return m_lastName;
      }

      public void setLastName(String lastName) {
        m_lastName = lastName;
      }
    }
  }

  @Replace
  public static class NoTableExtended extends TableFieldBaseFormData.NoTable {
    private static final long serialVersionUID = 1L;

    public NoTableExtended() {
    }

    @Override
    public NoTableExtendedRowData[] getRows() {
      return (NoTableExtendedRowData[]) super.getRows();
    }

    public void setRows(NoTableExtendedRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public NoTableExtendedRowData addRow() {
      return (NoTableExtendedRowData) super.addRow();
    }

    @Override
    public NoTableExtendedRowData addRow(int rowState) {
      return (NoTableExtendedRowData) super.addRow(rowState);
    }

    @Override
    public NoTableExtendedRowData rowAt(int idx) {
      return (NoTableExtendedRowData) super.rowAt(idx);
    }

    @Override
    public NoTableExtendedRowData createRow() {
      return new NoTableExtendedRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return NoTableExtendedRowData.class;
    }

    public static class NoTableExtendedRowData extends AbstractTableRowData {
      private static final long serialVersionUID = 1L;

      public NoTableExtendedRowData() {
      }

      public static final String new_ = "new";
      private String m_newValue;

      public String getNew() {
        return m_newValue;
      }

      public void setNew(String newValue) {
        m_newValue = newValue;
      }
    }
  }

  @Replace
  public static class TableExtended extends TableFieldBaseFormData.Table {
    private static final long serialVersionUID = 1L;

    public TableExtended() {
    }

    @Override
    public TableExRowData[] getRows() {
      return (TableExRowData[]) super.getRows();
    }

    public void setRows(TableExRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public TableExRowData addRow() {
      return (TableExRowData) super.addRow();
    }

    @Override
    public TableExRowData addRow(int rowState) {
      return (TableExRowData) super.addRow(rowState);
    }

    @Override
    public TableExRowData rowAt(int idx) {
      return (TableExRowData) super.rowAt(idx);
    }

    @Override
    public TableExRowData createRow() {
      return new TableExRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableExRowData.class;
    }

    public static class TableExRowData extends TableFieldBaseFormData.Table.TableRowData {
      private static final long serialVersionUID = 1L;

      public TableExRowData() {
      }

      public static final String boolean_ = "boolean";
      private Boolean m_booleanValue;

      public Boolean getBoolean() {
        return m_booleanValue;
      }

      public void setBoolean(Boolean booleanValue) {
        m_booleanValue = booleanValue;
      }
    }
  }
}
