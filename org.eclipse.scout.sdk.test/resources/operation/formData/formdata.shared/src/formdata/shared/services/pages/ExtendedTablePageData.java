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

import java.math.BigDecimal;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class ExtendedTablePageData extends BaseTablePageData {

  private static final long serialVersionUID = 1L;

  public ExtendedTablePageData() {
  }

  @Override
  public ExtendedTableRowData addRow() {
    return (ExtendedTableRowData) super.addRow();
  }

  @Override
  public ExtendedTableRowData addRow(int rowState) {
    return (ExtendedTableRowData) super.addRow(rowState);
  }

  @Override
  public ExtendedTableRowData createRow() {
    return new ExtendedTableRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return ExtendedTableRowData.class;
  }

  @Override
  public ExtendedTableRowData[] getRows() {
    return (ExtendedTableRowData[]) super.getRows();
  }

  @Override
  public ExtendedTableRowData rowAt(int index) {
    return (ExtendedTableRowData) super.rowAt(index);
  }

  public void setRows(ExtendedTableRowData[] rows) {
    super.setRows(rows);
  }

  public static class ExtendedTableRowData extends BaseTableRowData {

    private static final long serialVersionUID = 1L;
    public static final String intermediate = "intermediate";
    private BigDecimal m_intermediate;

    public ExtendedTableRowData() {
    }

    public BigDecimal getIntermediate() {
      return m_intermediate;
    }

    public void setIntermediate(BigDecimal intermediate) {
      m_intermediate = intermediate;
    }
  }
}
