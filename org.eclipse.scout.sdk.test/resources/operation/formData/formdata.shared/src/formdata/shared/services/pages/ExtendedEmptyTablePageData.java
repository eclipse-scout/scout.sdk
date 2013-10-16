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
public class ExtendedEmptyTablePageData extends EmptyTablePageData {

  private static final long serialVersionUID = 1L;

  public ExtendedEmptyTablePageData() {
  }

  @Override
  public ExtendedEmptyTableRowData addRow() {
    return (ExtendedEmptyTableRowData) super.addRow();
  }

  @Override
  public ExtendedEmptyTableRowData addRow(int rowState) {
    return (ExtendedEmptyTableRowData) super.addRow(rowState);
  }

  @Override
  public ExtendedEmptyTableRowData createRow() {
    return new ExtendedEmptyTableRowData();
  }

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return ExtendedEmptyTableRowData.class;
  }

  @Override
  public ExtendedEmptyTableRowData[] getRows() {
    return (ExtendedEmptyTableRowData[]) super.getRows();
  }

  @Override
  public ExtendedEmptyTableRowData rowAt(int index) {
    return (ExtendedEmptyTableRowData) super.rowAt(index);
  }

  public void setRows(ExtendedEmptyTableRowData[] rows) {
    super.setRows(rows);
  }

  public static class ExtendedEmptyTableRowData extends AbstractTableRowData {

    private static final long serialVersionUID = 1L;
    public static final String name = "name";
    private String m_name;

    public ExtendedEmptyTableRowData() {
    }

    public String getName() {
      return m_name;
    }

    public void setName(String name) {
      m_name = name;
    }
  }
}
