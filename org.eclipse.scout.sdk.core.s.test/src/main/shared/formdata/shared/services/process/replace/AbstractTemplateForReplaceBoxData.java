/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.services.process.replace;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 * 
 * @generated
 */
@Generated(value = "FormDataUpdateOperation", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public abstract class AbstractTemplateForReplaceBoxData extends AbstractFormFieldData {

  private static final long serialVersionUID = 1L;

  public AbstractTemplateForReplaceBoxData() {
  }

  public TemplateBoxString getTemplateBoxString() {
    return getFieldByClass(TemplateBoxString.class);
  }

  public TemplateString getTemplateString() {
    return getFieldByClass(TemplateString.class);
  }

  public TemplateTable getTemplateTable() {
    return getFieldByClass(TemplateTable.class);
  }

  public static class TemplateBoxString extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    public TemplateBoxString() {
    }
  }

  public static class TemplateString extends AbstractValueFieldData<String> {

    private static final long serialVersionUID = 1L;

    public TemplateString() {
    }
  }

  public static class TemplateTable extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    public TemplateTable() {
    }

    @Override
    public TemplateTableRowData addRow() {
      return (TemplateTableRowData) super.addRow();
    }

    @Override
    public TemplateTableRowData addRow(int rowState) {
      return (TemplateTableRowData) super.addRow(rowState);
    }

    @Override
    public TemplateTableRowData createRow() {
      return new TemplateTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TemplateTableRowData.class;
    }

    @Override
    public TemplateTableRowData[] getRows() {
      return (TemplateTableRowData[]) super.getRows();
    }

    @Override
    public TemplateTableRowData rowAt(int index) {
      return (TemplateTableRowData) super.rowAt(index);
    }

    public void setRows(TemplateTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class TemplateTableRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String first = "first";
      public static final String second = "second";
      private String m_first;
      private String m_second;

      public TemplateTableRowData() {
      }

      public String getFirst() {
        return m_first;
      }

      public void setFirst(String first) {
        m_first = first;
      }

      public String getSecond() {
        return m_second;
      }

      public void setSecond(String second) {
        m_second = second;
      }
    }
  }
}