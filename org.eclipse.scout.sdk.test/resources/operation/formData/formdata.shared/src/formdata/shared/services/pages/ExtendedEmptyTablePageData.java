package formdata.shared.services.pages;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

public class ExtendedEmptyTablePageData extends EmptyTablePageData {
  private static final long serialVersionUID = 1L;

  public ExtendedEmptyTablePageData() {
  }

  @Override
  public ExtendedEmptyTablePageRowData[] getRows() {
    return (ExtendedEmptyTablePageRowData[]) super.getRows();
  }

  public void setRows(ExtendedEmptyTablePageRowData[] rows) {
    super.setRows(rows);
  }

  @Override
  public ExtendedEmptyTablePageRowData addRow() {
    return (ExtendedEmptyTablePageRowData) super.addRow();
  }

  @Override
  public ExtendedEmptyTablePageRowData addRow(int rowState) {
    return (ExtendedEmptyTablePageRowData) super.addRow(rowState);
  }

  @Override
  public ExtendedEmptyTablePageRowData rowAt(int idx) {
    return (ExtendedEmptyTablePageRowData) super.rowAt(idx);
  }

  @Override
  public ExtendedEmptyTablePageRowData createRow() {
    return new ExtendedEmptyTablePageRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return ExtendedEmptyTablePageRowData.class;
  }

  public static class ExtendedEmptyTablePageRowData extends AbstractTableRowData {
    private static final long serialVersionUID = 1L;

    public ExtendedEmptyTablePageRowData() {
    }

    public static final String name = "name";
    private String m_name;

    public String getName() {
      return m_name;
    }

    public void setName(String name) {
      m_name = name;
    }
  }
}
