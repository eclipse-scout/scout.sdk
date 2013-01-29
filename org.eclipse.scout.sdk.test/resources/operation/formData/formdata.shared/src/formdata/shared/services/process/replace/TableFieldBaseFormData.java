package formdata.shared.services.process.replace;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

public class TableFieldBaseFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public TableFieldBaseFormData() {
  }

  public EmptyTable getEmptyTable() {
    return getFieldByClass(EmptyTable.class);
  }

  public NoTable getNoTable() {
    return getFieldByClass(NoTable.class);
  }

  public Table getTable() {
    return getFieldByClass(Table.class);
  }

  public static class EmptyTable extends AbstractTableFieldBeanData {
    private static final long serialVersionUID = 1L;

    public EmptyTable() {
    }

    @Override
    public EmptyTableRowData addRow(int rowState) {
      return (EmptyTableRowData) super.addRow(rowState);
    }

    @Override
    public EmptyTableRowData addRow() {
      return (EmptyTableRowData) super.addRow();
    }

    @Override
    public EmptyTableRowData createRow() {
      return new EmptyTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return EmptyTableRowData.class;
    }

    @Override
    public EmptyTableRowData[] getRows() {
      return (EmptyTableRowData[]) super.getRows();
    }

    @Override
    public EmptyTableRowData rowAt(int idx) {
      return (EmptyTableRowData) super.rowAt(idx);
    }

    public void setRows(EmptyTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class EmptyTableRowData extends AbstractTableRowData {
      private static final long serialVersionUID = 1L;

      public EmptyTableRowData() {
      }
    }
  }

  public static class NoTable extends AbstractTableFieldBeanData {
    private static final long serialVersionUID = 1L;

    public NoTable() {
    }

    @Override
    public AbstractTableRowData createRow() {
      return new AbstractTableRowData() {
        private static final long serialVersionUID = 1L;
      };
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return AbstractTableRowData.class;
    }
  }

  public static class Table extends AbstractTableFieldBeanData {
    private static final long serialVersionUID = 1L;

    public Table() {
    }

    @Override
    public TableRowData addRow() {
      return (TableRowData) super.addRow();
    }

    @Override
    public TableRowData addRow(int rowState) {
      return (TableRowData) super.addRow(rowState);
    }

    @Override
    public TableRowData createRow() {
      return new TableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableRowData.class;
    }

    @Override
    public TableRowData[] getRows() {
      return (TableRowData[]) super.getRows();
    }

    @Override
    public TableRowData rowAt(int idx) {
      return (TableRowData) super.rowAt(idx);
    }

    public void setRows(TableRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableRowData extends AbstractTableRowData {
      private static final long serialVersionUID = 1L;

      public TableRowData() {
      }

      private String m_first;
      private String m_second;

      public String getFirst() {
        return m_first;
      }

      public void setFirst(String first) {
        m_first = first;
      }

      public String getSecond() {
        return m_second;
      }

      public void setSecond(String second) {
        m_second = second;
      }
    }
  }
}
