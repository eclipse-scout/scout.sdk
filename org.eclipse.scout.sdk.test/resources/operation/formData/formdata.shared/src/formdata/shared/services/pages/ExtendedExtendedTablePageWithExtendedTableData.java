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
public class ExtendedExtendedTablePageWithExtendedTableData extends ExtendedTablePageWithoutExtendedTableData {

  private static final long serialVersionUID = 1L;

  public ExtendedExtendedTablePageWithExtendedTableData() {
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

  public static class ExtendedExtendedTablePageWithExtendedTableRowData extends BaseTableRowData {

    private static final long serialVersionUID = 1L;
    public static final String boolean_ = "boolean";
    private Boolean m_boolean;

    public ExtendedExtendedTablePageWithExtendedTableRowData() {
    }

    public Boolean getBoolean() {
      return m_boolean;
    }

    public void setBoolean(Boolean booleanValue) {
      m_boolean = booleanValue;
    }
  }
}
