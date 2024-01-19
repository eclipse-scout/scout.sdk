/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.services.process.replace;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsIgnoreExForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class TableFieldWithIgnoredColumnsIgnoreExFormData extends TableFieldWithIgnoredColumnsBaseFormData {

  private static final long serialVersionUID = 1L;

  public TableIgnoreEx getTableIgnoreEx() {
    return getFieldByClass(TableIgnoreEx.class);
  }

  @Replace
  public static class TableIgnoreEx extends TableBase {

    private static final long serialVersionUID = 1L;

    @Override
    public TableIgnoreExRowData addRow() {
      return (TableIgnoreExRowData) super.addRow();
    }

    @Override
    public TableIgnoreExRowData addRow(int rowState) {
      return (TableIgnoreExRowData) super.addRow(rowState);
    }

    @Override
    public TableIgnoreExRowData createRow() {
      return new TableIgnoreExRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableIgnoreExRowData.class;
    }

    @Override
    public TableIgnoreExRowData[] getRows() {
      return (TableIgnoreExRowData[]) super.getRows();
    }

    @Override
    public TableIgnoreExRowData rowAt(int index) {
      return (TableIgnoreExRowData) super.rowAt(index);
    }

    public void setRows(TableIgnoreExRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableIgnoreExRowData extends formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData.TableBase.TableBaseRowData {

      private static final long serialVersionUID = 1L;
    }
  }
}
