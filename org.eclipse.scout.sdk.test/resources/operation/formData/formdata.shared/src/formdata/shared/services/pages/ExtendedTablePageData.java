package formdata.shared.services.pages;

import java.math.BigDecimal;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

public class ExtendedTablePageData extends BaseTablePageData {
  private static final long serialVersionUID = 1L;

  public ExtendedTablePageData() {
  }

  @Override
  public ExtendedTablePageRowData[] getRows() {
    return (ExtendedTablePageRowData[]) super.getRows();
  }

  public void setRows(ExtendedTablePageRowData[] rows) {
    super.setRows(rows);
  }

  @Override
  public ExtendedTablePageRowData addRow() {
    return (ExtendedTablePageRowData) super.addRow();
  }

  @Override
  public ExtendedTablePageRowData addRow(int rowState) {
    return (ExtendedTablePageRowData) super.addRow(rowState);
  }

  @Override
  public ExtendedTablePageRowData rowAt(int idx) {
    return (ExtendedTablePageRowData) super.rowAt(idx);
  }

  @Override
  public ExtendedTablePageRowData createRow() {
    return new ExtendedTablePageRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return ExtendedTablePageRowData.class;
  }

  public static class ExtendedTablePageRowData extends BaseTablePageData.BaseTablePageRowData {
    private static final long serialVersionUID = 1L;

    public ExtendedTablePageRowData() {
    }

    public static final String intermediate = "intermediate";
    private BigDecimal m_intermediate;

    public BigDecimal getIntermediate() {
      return m_intermediate;
    }

    public void setIntermediate(BigDecimal intermediate) {
      m_intermediate = intermediate;
    }
  }
}
