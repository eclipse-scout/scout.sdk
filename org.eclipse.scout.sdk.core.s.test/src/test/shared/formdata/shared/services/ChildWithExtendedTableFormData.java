/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.services;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.forms.ChildWithExtendedTableForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class ChildWithExtendedTableFormData extends BaseWithExtendedTableFormData {

  private static final long serialVersionUID = 1L;

  public ChildTable getChildTable() {
    return getFieldByClass(ChildTable.class);
  }

  public static class ChildTable extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    @Override
    public ChildTableRowData addRow() {
      return (ChildTableRowData) super.addRow();
    }

    @Override
    public ChildTableRowData addRow(int rowState) {
      return (ChildTableRowData) super.addRow(rowState);
    }

    @Override
    public ChildTableRowData createRow() {
      return new ChildTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return ChildTableRowData.class;
    }

    @Override
    public ChildTableRowData[] getRows() {
      return (ChildTableRowData[]) super.getRows();
    }

    @Override
    public ChildTableRowData rowAt(int index) {
      return (ChildTableRowData) super.rowAt(index);
    }

    public void setRows(ChildTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class ChildTableRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String col1InChildForm = "col1InChildForm";
      public static final String colInAbstractTable = "colInAbstractTable";
      public static final String colInDesktopForm = "colInDesktopForm";
      private String m_col1InChildForm;
      private String m_colInAbstractTable;
      private String m_colInDesktopForm;

      public String getCol1InChildForm() {
        return m_col1InChildForm;
      }

      public void setCol1InChildForm(String newCol1InChildForm) {
        m_col1InChildForm = newCol1InChildForm;
      }

      public String getColInAbstractTable() {
        return m_colInAbstractTable;
      }

      public void setColInAbstractTable(String newColInAbstractTable) {
        m_colInAbstractTable = newColInAbstractTable;
      }

      public String getColInDesktopForm() {
        return m_colInDesktopForm;
      }

      public void setColInDesktopForm(String newColInDesktopForm) {
        m_colInDesktopForm = newColInDesktopForm;
      }
    }
  }
}
