package formdata.shared.services.pages;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

public class ExtendedExtendedTablePageWithExtendedTableData extends ExtendedTablePageWithoutExtendedTableData {
  private static final long serialVersionUID = 1L;

  public ExtendedExtendedTablePageWithExtendedTableData() {
  }

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData[] getRows() {
    return (ExtendedExtendedTablePageWithExtendedTableRowData[]) super.getRows();
  }

  public void setRows(ExtendedExtendedTablePageWithExtendedTableRowData[] rows) {
    super.setRows(rows);
  }

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData addRow() {
    return (ExtendedExtendedTablePageWithExtendedTableRowData) super.addRow();
  }

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData addRow(int rowState) {
    return (ExtendedExtendedTablePageWithExtendedTableRowData) super.addRow(rowState);
  }

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData rowAt(int idx) {
    return (ExtendedExtendedTablePageWithExtendedTableRowData) super.rowAt(idx);
  }

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData createRow() {
    return new ExtendedExtendedTablePageWithExtendedTableRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return ExtendedExtendedTablePageWithExtendedTableRowData.class;
  }

  public static class ExtendedExtendedTablePageWithExtendedTableRowData extends BaseTablePageData.BaseTablePageRowData {
    private static final long serialVersionUID = 1L;

    public ExtendedExtendedTablePageWithExtendedTableRowData() {
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
