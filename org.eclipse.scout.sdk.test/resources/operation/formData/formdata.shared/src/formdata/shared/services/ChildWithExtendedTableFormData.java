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
package formdata.shared.services;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

import formdata.shared.services.BaseWithExtendedTableFormData.TableInForm.TableInFormRowData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class ChildWithExtendedTableFormData extends BaseWithExtendedTableFormData {

  private static final long serialVersionUID = 1L;

  public ChildWithExtendedTableFormData() {
  }

  public ChildTable getChildTable() {
    return getFieldByClass(ChildTable.class);
  }

  public static class ChildTable extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    public ChildTable() {
    }

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

    public static class ChildTableRowData extends TableInFormRowData {

      private static final long serialVersionUID = 1L;
      public static final String col1InChildForm = "col1InChildForm";
      private String m_col1InChildForm;

      public ChildTableRowData() {
      }

      public String getCol1InChildForm() {
        return m_col1InChildForm;
      }

      public void setCol1InChildForm(String col1InChildForm) {
        m_col1InChildForm = col1InChildForm;
      }
    }
  }
}
