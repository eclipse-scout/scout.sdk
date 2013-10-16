/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.services.pages;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class ExtendedTablePageWithoutExtendedTableData extends BaseTablePageData {

  private static final long serialVersionUID = 1L;

  public ExtendedTablePageWithoutExtendedTableData() {
  }

  @Override
  public BaseTableRowData addRow() {
    return (BaseTableRowData) super.addRow();
  }

  @Override
  public BaseTableRowData addRow(int rowState) {
    return (BaseTableRowData) super.addRow(rowState);
  }

  @Override
  public BaseTableRowData createRow() {
    return new BaseTableRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return BaseTableRowData.class;
  }

  @Override
  public BaseTableRowData[] getRows() {
    return (BaseTableRowData[]) super.getRows();
  }

  @Override
  public BaseTableRowData rowAt(int index) {
    return (BaseTableRowData) super.rowAt(index);
  }

  public void setRows(BaseTableRowData[] rows) {
    super.setRows(rows);
  }

  public static class BaseTableRowData extends formdata.shared.services.pages.BaseTablePageData.BaseTableRowData {

    private static final long serialVersionUID = 1L;

    public BaseTableRowData() {
    }
  }
}
