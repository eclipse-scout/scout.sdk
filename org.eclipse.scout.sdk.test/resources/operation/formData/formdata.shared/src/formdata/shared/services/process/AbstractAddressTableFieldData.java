package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

public abstract class AbstractAddressTableFieldData extends AbstractTableFieldBeanData {
  private static final long serialVersionUID = 1L;

  public AbstractAddressTableFieldData() {
  }

  @Override
  public AbstractAddressTableRowData[] getRows() {
    return (AbstractAddressTableRowData[]) super.getRows();
  }

  public void setRows(AbstractAddressTableRowData[] rows) {
    super.setRows(rows);
  }

  @Override
  public AbstractAddressTableRowData addRow() {
    return (AbstractAddressTableRowData) super.addRow();
  }

  @Override
  public AbstractAddressTableRowData addRow(int rowState) {
    return (AbstractAddressTableRowData) super.addRow(rowState);
  }

  @Override
  public AbstractAddressTableRowData rowAt(int idx) {
    return (AbstractAddressTableRowData) super.rowAt(idx);
  }

  @Override
  public AbstractAddressTableRowData createRow() {
    return new AbstractAddressTableRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return AbstractAddressTableRowData.class;
  }

  public static class AbstractAddressTableRowData extends AbstractTableRowData {
    private static final long serialVersionUID = 1L;

    public AbstractAddressTableRowData() {
    }

    public static final String addressId = "addressId";
    public static final String street = "street";
    public static final String poBoxAddress = "poBoxAddress";
    private String m_addressId;
    private String m_street;
    private Boolean m_poBoxAddress;

    public String getAddressId() {
      return m_addressId;
    }

    public void setAddressId(String addressId) {
      m_addressId = addressId;
    }

    public String getStreet() {
      return m_street;
    }

    public void setStreet(String street) {
      m_street = street;
    }

    public Boolean getPoBoxAddress() {
      return m_poBoxAddress;
    }

    public void setPoBoxAddress(Boolean poBoxAddress) {
      m_poBoxAddress = poBoxAddress;
    }
  }
}
