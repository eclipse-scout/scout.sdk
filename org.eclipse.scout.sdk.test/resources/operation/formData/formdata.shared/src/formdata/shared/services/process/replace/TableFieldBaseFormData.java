package formdata.shared.services.process.replace;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import formdata.shared.services.process.AbstractAddressTableFieldData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

public class TableFieldBaseFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public TableFieldBaseFormData() {
  }

  public AddressTable getAddressTable() {
    return getFieldByClass(AddressTable.class);
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

  public static class AddressTable extends AbstractAddressTableFieldData {
    private static final long serialVersionUID = 1L;

    public AddressTable() {
    }

    @Override
    public AddressTableRowData[] getRows() {
      return (AddressTableRowData[]) super.getRows();
    }

    public void setRows(AddressTableRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public AddressTableRowData rowAt(int idx) {
      return (AddressTableRowData) super.rowAt(idx);
    }

    @Override
    public AddressTableRowData addRow() {
      return (AddressTableRowData) super.addRow();
    }

    @Override
    public AddressTableRowData addRow(int rowState) {
      return (AddressTableRowData) super.addRow(rowState);
    }

    @Override
    public AddressTableRowData createRow() {
      return new AddressTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return AddressTableRowData.class;
    }

    public static class AddressTableRowData extends AbstractAddressTableFieldData.AbstractAddressTableRowData {
      private static final long serialVersionUID = 1L;

      public AddressTableRowData() {
      }

      public static final String PROP_CITY = "city";
      private String m_city;

      public String getCity() {
        return m_city;
      }

      public void setCity(String city) {
        m_city = city;
      }
    }
  }

  public static class EmptyTable extends AbstractTableFieldBeanData {
    private static final long serialVersionUID = 1L;

    public EmptyTable() {
    }

    @Override
    public EmptyTableRowData[] getRows() {
      return (EmptyTableRowData[]) super.getRows();
    }

    public void setRows(EmptyTableRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public EmptyTableRowData rowAt(int idx) {
      return (EmptyTableRowData) super.rowAt(idx);
    }

    @Override
    public EmptyTableRowData addRow() {
      return (EmptyTableRowData) super.addRow();
    }

    @Override
    public EmptyTableRowData addRow(int rowState) {
      return (EmptyTableRowData) super.addRow(rowState);
    }

    @Override
    public EmptyTableRowData createRow() {
      return new EmptyTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return EmptyTableRowData.class;
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
    public TableRowData[] getRows() {
      return (TableRowData[]) super.getRows();
    }

    public void setRows(TableRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public TableRowData rowAt(int idx) {
      return (TableRowData) super.rowAt(idx);
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

    public static class TableRowData extends AbstractTableRowData {
      private static final long serialVersionUID = 1L;

      public TableRowData() {
      }

      public static final String PROP_FIRST = "first";
      public static final String PROP_SECOND = "second";
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
