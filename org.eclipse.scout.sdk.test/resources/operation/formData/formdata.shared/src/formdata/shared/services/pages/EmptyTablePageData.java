package formdata.shared.services.pages;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

public class EmptyTablePageData extends AbstractTableFieldBeanData {
  private static final long serialVersionUID = 1L;

  public EmptyTablePageData() {
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
