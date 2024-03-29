/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.services.pages;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.desktop.outline.pages.ExtendedExtendedTablePageWithExtendedTable", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class ExtendedExtendedTablePageWithExtendedTableData extends ExtendedTablePageWithoutExtendedTableData {

  private static final long serialVersionUID = 1L;

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData addRow() {
    return (ExtendedExtendedTablePageWithExtendedTableRowData) super.addRow();
  }

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData addRow(int rowState) {
    return (ExtendedExtendedTablePageWithExtendedTableRowData) super.addRow(rowState);
  }

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData createRow() {
    return new ExtendedExtendedTablePageWithExtendedTableRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return ExtendedExtendedTablePageWithExtendedTableRowData.class;
  }

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData[] getRows() {
    return (ExtendedExtendedTablePageWithExtendedTableRowData[]) super.getRows();
  }

  @Override
  public ExtendedExtendedTablePageWithExtendedTableRowData rowAt(int index) {
    return (ExtendedExtendedTablePageWithExtendedTableRowData) super.rowAt(index);
  }

  public void setRows(ExtendedExtendedTablePageWithExtendedTableRowData[] rows) {
    super.setRows(rows);
  }

  public static class ExtendedExtendedTablePageWithExtendedTableRowData extends ExtendedTablePageWithoutExtendedTableRowData {

    private static final long serialVersionUID = 1L;
    public static final String boolean_ = "boolean";
    private Boolean m_boolean;

    public Boolean getBoolean() {
      return m_boolean;
    }

    public void setBoolean(Boolean newBoolean) {
      m_boolean = newBoolean;
    }
  }
}
