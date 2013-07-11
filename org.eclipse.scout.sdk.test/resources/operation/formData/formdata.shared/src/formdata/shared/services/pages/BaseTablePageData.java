package formdata.shared.services.pages;

import java.util.Date;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;

public class BaseTablePageData extends AbstractTablePageData {
  private static final long serialVersionUID = 1L;

  public BaseTablePageData() {
  }

  @Override
  public BaseTablePageRowData[] getRows() {
    return (BaseTablePageRowData[]) super.getRows();
  }

  public void setRows(BaseTablePageRowData[] rows) {
    super.setRows(rows);
  }

  @Override
  public BaseTablePageRowData addRow() {
    return (BaseTablePageRowData) super.addRow();
  }

  @Override
  public BaseTablePageRowData addRow(int rowState) {
    return (BaseTablePageRowData) super.addRow(rowState);
  }

  @Override
  public BaseTablePageRowData rowAt(int idx) {
    return (BaseTablePageRowData) super.rowAt(idx);
  }

  @Override
  public BaseTablePageRowData createRow() {
    return new BaseTablePageRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return BaseTablePageRowData.class;
  }

  public static class BaseTablePageRowData extends AbstractTableRowData {
    private static final long serialVersionUID = 1L;

    public BaseTablePageRowData() {
    }

    public static final String first = "first";
    public static final String second = "second";
    private String m_first;
    private Date m_second;

    public String getFirst() {
      return m_first;
    }

    public void setFirst(String first) {
      m_first = first;
    }

    public Date getSecond() {
      return m_second;
    }

    public void setSecond(Date second) {
      m_second = second;
    }
  }
}
