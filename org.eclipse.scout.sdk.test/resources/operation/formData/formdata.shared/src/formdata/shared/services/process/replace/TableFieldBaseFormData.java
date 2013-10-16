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
package formdata.shared.services.process.replace;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

import formdata.shared.services.process.AbstractAddressTableFieldData;
import formdata.shared.services.process.AbstractPersonTableFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class TableFieldBaseFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public TableFieldBaseFormData() {
  }

  public AddressTable getAddressTable() {
    return getFieldByClass(AddressTable.class);
  }

  public EmptyTable getEmptyTable() {
    return getFieldByClass(EmptyTable.class);
  }

  public NoTable getNoTable() {
    return getFieldByClass(NoTable.class);
  }

  public PersonTable getPersonTable() {
    return getFieldByClass(PersonTable.class);
  }

  public Table getTable() {
    return getFieldByClass(Table.class);
  }

  public static class AddressTable extends AbstractAddressTableFieldData {

    private static final long serialVersionUID = 1L;

    public AddressTable() {
    }

    @Override
    public AddressTableRowData addRow() {
      return (AddressTableRowData) super.addRow();
    }

    @Override
    public AddressTableRowData addRow(int rowState) {
      return (AddressTableRowData) super.addRow(rowState);
    }

    @Override
    public AddressTableRowData createRow() {
      return new AddressTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return AddressTableRowData.class;
    }

    @Override
    public AddressTableRowData[] getRows() {
      return (AddressTableRowData[]) super.getRows();
    }

    @Override
    public AddressTableRowData rowAt(int index) {
      return (AddressTableRowData) super.rowAt(index);
    }

    public void setRows(AddressTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class AddressTableRowData extends AbstractAddressTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String city = "city";
      private String m_city;

      public AddressTableRowData() {
      }

      public String getCity() {
        return m_city;
      }

      public void setCity(String city) {
        m_city = city;
      }
    }
  }

  public static class EmptyTable extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    public EmptyTable() {
    }

    @Override
    public EmptyTableRowData addRow() {
      return (EmptyTableRowData) super.addRow();
    }

    @Override
    public EmptyTableRowData addRow(int rowState) {
      return (EmptyTableRowData) super.addRow(rowState);
    }

    @Override
    public EmptyTableRowData createRow() {
      return new EmptyTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return EmptyTableRowData.class;
    }

    @Override
    public EmptyTableRowData[] getRows() {
      return (EmptyTableRowData[]) super.getRows();
    }

    @Override
    public EmptyTableRowData rowAt(int index) {
      return (EmptyTableRowData) super.rowAt(index);
    }

    public void setRows(EmptyTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class EmptyTableRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;

      public EmptyTableRowData() {
      }
    }
  }

  public static class NoTable extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    public NoTable() {
    }

    @Override
    public AbstractTableRowData createRow() {
      return new AbstractTableRowData() {
        private static final long serialVersionUID = 1L;
      };
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return AbstractTableRowData.class;
    }
  }

  public static class PersonTable extends AbstractPersonTableFieldData {

    private static final long serialVersionUID = 1L;

    public PersonTable() {
    }

    @Override
    public AbstractPersonTableRowData addRow() {
      return (AbstractPersonTableRowData) super.addRow();
    }

    @Override
    public AbstractPersonTableRowData addRow(int rowState) {
      return (AbstractPersonTableRowData) super.addRow(rowState);
    }

    @Override
    public AbstractPersonTableRowData createRow() {
      return new AbstractPersonTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return AbstractPersonTableRowData.class;
    }

    @Override
    public AbstractPersonTableRowData[] getRows() {
      return (AbstractPersonTableRowData[]) super.getRows();
    }

    @Override
    public AbstractPersonTableRowData rowAt(int index) {
      return (AbstractPersonTableRowData) super.rowAt(index);
    }

    public void setRows(AbstractPersonTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class AbstractPersonTableRowData extends formdata.shared.services.process.AbstractPersonTableFieldData.AbstractPersonTableRowData {

      private static final long serialVersionUID = 1L;

      public AbstractPersonTableRowData() {
      }
    }
  }

  public static class Table extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    public Table() {
    }

    @Override
    public TableRowData addRow() {
      return (TableRowData) super.addRow();
    }

    @Override
    public TableRowData addRow(int rowState) {
      return (TableRowData) super.addRow(rowState);
    }

    @Override
    public TableRowData createRow() {
      return new TableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableRowData.class;
    }

    @Override
    public TableRowData[] getRows() {
      return (TableRowData[]) super.getRows();
    }

    @Override
    public TableRowData rowAt(int index) {
      return (TableRowData) super.rowAt(index);
    }

    public void setRows(TableRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String first = "first";
      public static final String second = "second";
      private String m_first;
      private String m_second;

      public TableRowData() {
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
