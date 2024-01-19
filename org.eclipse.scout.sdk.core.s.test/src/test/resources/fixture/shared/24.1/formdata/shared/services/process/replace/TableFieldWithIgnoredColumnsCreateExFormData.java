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
@Generated(value = "formdata.client.ui.forms.replace.TableFieldWithIgnoredColumnsCreateExForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class TableFieldWithIgnoredColumnsCreateExFormData extends TableFieldWithIgnoredColumnsBaseFormData {

  private static final long serialVersionUID = 1L;

  public TableCreateEx getTableCreateEx() {
    return getFieldByClass(TableCreateEx.class);
  }

  @Replace
  public static class TableCreateEx extends TableBase {

    private static final long serialVersionUID = 1L;

    @Override
    public TableCreateExRowData addRow() {
      return (TableCreateExRowData) super.addRow();
    }

    @Override
    public TableCreateExRowData addRow(int rowState) {
      return (TableCreateExRowData) super.addRow(rowState);
    }

    @Override
    public TableCreateExRowData createRow() {
      return new TableCreateExRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableCreateExRowData.class;
    }

    @Override
    public TableCreateExRowData[] getRows() {
      return (TableCreateExRowData[]) super.getRows();
    }

    @Override
    public TableCreateExRowData rowAt(int index) {
      return (TableCreateExRowData) super.rowAt(index);
    }

    public void setRows(TableCreateExRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableCreateExRowData extends formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsBaseFormData.TableBase.TableBaseRowData {

      private static final long serialVersionUID = 1L;
      public static final String ignoreCreate = "ignoreCreate";
      private String m_ignoreCreate;

      public String getIgnoreCreate() {
        return m_ignoreCreate;
      }

      public void setIgnoreCreate(String newIgnoreCreate) {
        m_ignoreCreate = newIgnoreCreate;
      }
    }
  }
}
