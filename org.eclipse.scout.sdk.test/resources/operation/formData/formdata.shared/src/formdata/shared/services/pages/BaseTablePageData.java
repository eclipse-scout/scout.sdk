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

import java.util.Date;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class BaseTablePageData extends AbstractTablePageData {

  private static final long serialVersionUID = 1L;

  public BaseTablePageData() {
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

  public static class BaseTableRowData extends AbstractTableRowData {

    private static final long serialVersionUID = 1L;
    public static final String first = "first";
    public static final String second = "second";
    private String m_first;
    private Date m_second;

    public BaseTableRowData() {
    }

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
