package formdata.shared.services.process.replace;

import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

public class TableFieldExFormData extends TableFieldBaseFormData {
  private static final long serialVersionUID = 1L;

  public TableFieldExFormData() {
  }

  public EmptyTableExtended getEmptyTableExtended() {
    return getFieldByClass(EmptyTableExtended.class);
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
    public TableExRowData addRow() {
      return (TableExRowData) super.addRow();
    }

    @Override
    public TableExRowData addRow(int rowState) {
      return (TableExRowData) super.addRow(rowState);
    }

    @Override
    public TableExRowData createRow() {
      return new TableExRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableExRowData.class;
    }

    @Override
    public TableExRowData[] getRows() {
      return (TableExRowData[]) super.getRows();
    }

    @Override
    public TableExRowData rowAt(int idx) {
      return (TableExRowData) super.rowAt(idx);
    }

    public void setRows(TableExRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableExRowData extends TableFieldBaseFormData.EmptyTable.EmptyTableRowData {
      private static final long serialVersionUID = 1L;

      public TableExRowData() {
      }

      private String m_single;

      public void setSingle(String single) {
        m_single = single;
      }

      public String getSingle() {
        return m_single;
      }
    }
  }

  @Replace
  public static class NoTableExtended extends TableFieldBaseFormData.NoTable {
    private static final long serialVersionUID = 1L;

    public NoTableExtended() {
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
    public NoTableExtendedRowData createRow() {
      return new NoTableExtendedRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return NoTableExtendedRowData.class;
    }

    @Override
    public NoTableExtendedRowData[] getRows() {
      return (NoTableExtendedRowData[]) super.getRows();
    }

    @Override
    public NoTableExtendedRowData rowAt(int idx) {
      return (NoTableExtendedRowData) super.rowAt(idx);
    }

    public void setRows(NoTableExtendedRowData[] rows) {
      super.setRows(rows);
    }

    public static class NoTableExtendedRowData extends AbstractTableRowData {
      private static final long serialVersionUID = 1L;

      public NoTableExtendedRowData() {
      }

      private String m_newValue;

      public void setNew(String newValue) {
        m_newValue = newValue;
      }

      public String getNew() {
        return m_newValue;
      }
    }
  }

  @Replace
  public static class TableExtended extends TableFieldBaseFormData.Table {
    private static final long serialVersionUID = 1L;

    public TableExtended() {
    }

    @Override
    public TableExRowData addRow(int rowState) {
      return (TableExRowData) super.addRow(rowState);
    }

    @Override
    public TableExRowData addRow() {
      return (TableExRowData) super.addRow();
    }

    @Override
    public TableExRowData createRow() {
      return new TableExRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableExRowData.class;
    }

    @Override
    public TableExRowData[] getRows() {
      return (TableExRowData[]) super.getRows();
    }

    @Override
    public TableExRowData rowAt(int idx) {
      return (TableExRowData) super.rowAt(idx);
    }

    public void setRows(TableExRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableExRowData extends TableFieldBaseFormData.Table.TableRowData {
      private static final long serialVersionUID = 1L;

      public TableExRowData() {
      }

      private Boolean m_booleanValue;

      public void setBoolean(Boolean booleanValue) {
        m_booleanValue = booleanValue;
      }

      public Boolean getBoolean() {
        return m_booleanValue;
      }
    }
  }
}
