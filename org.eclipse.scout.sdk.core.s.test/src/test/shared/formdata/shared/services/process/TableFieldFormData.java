/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.shared.services.process;

import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.forms.TableFieldForm", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public class TableFieldFormData extends AbstractFormData {

  private static final long serialVersionUID = 1L;

  public CompanyTable getCompanyTable() {
    return getFieldByClass(CompanyTable.class);
  }

  public ConcreteTable getConcreteTable() {
    return getFieldByClass(ConcreteTable.class);
  }

  public PersonTable getPersonTable() {
    return getFieldByClass(PersonTable.class);
  }

  public TableFieldWithExternalTable getTableFieldWithExternalTable() {
    return getFieldByClass(TableFieldWithExternalTable.class);
  }

  public static class CompanyTable extends AbstractCompanyTableFieldData {

    private static final long serialVersionUID = 1L;

    @Override
    public CompanyTableRowData addRow() {
      return (CompanyTableRowData) super.addRow();
    }

    @Override
    public CompanyTableRowData addRow(int rowState) {
      return (CompanyTableRowData) super.addRow(rowState);
    }

    @Override
    public CompanyTableRowData createRow() {
      return new CompanyTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return CompanyTableRowData.class;
    }

    @Override
    public CompanyTableRowData[] getRows() {
      return (CompanyTableRowData[]) super.getRows();
    }

    @Override
    public CompanyTableRowData rowAt(int index) {
      return (CompanyTableRowData) super.rowAt(index);
    }

    public void setRows(CompanyTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class CompanyTableRowData extends AbstractCompanyTableRowData {

      private static final long serialVersionUID = 1L;
    }
  }

  public static class ConcreteTable extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    @Override
    public ConcreteTableRowData addRow() {
      return (ConcreteTableRowData) super.addRow();
    }

    @Override
    public ConcreteTableRowData addRow(int rowState) {
      return (ConcreteTableRowData) super.addRow(rowState);
    }

    @Override
    public ConcreteTableRowData createRow() {
      return new ConcreteTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return ConcreteTableRowData.class;
    }

    @Override
    public ConcreteTableRowData[] getRows() {
      return (ConcreteTableRowData[]) super.getRows();
    }

    @Override
    public ConcreteTableRowData rowAt(int index) {
      return (ConcreteTableRowData) super.rowAt(index);
    }

    public void setRows(ConcreteTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class ConcreteTableRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String name = "name";
      public static final String extKey = "extKey";
      private String m_name;
      private Integer m_extKey;

      public String getName() {
        return m_name;
      }

      public void setName(String newName) {
        m_name = newName;
      }

      public Integer getExtKey() {
        return m_extKey;
      }

      public void setExtKey(Integer newExtKey) {
        m_extKey = newExtKey;
      }
    }
  }

  public static class PersonTable extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    @Override
    public PersonTableRowData addRow() {
      return (PersonTableRowData) super.addRow();
    }

    @Override
    public PersonTableRowData addRow(int rowState) {
      return (PersonTableRowData) super.addRow(rowState);
    }

    @Override
    public PersonTableRowData createRow() {
      return new PersonTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return PersonTableRowData.class;
    }

    @Override
    public PersonTableRowData[] getRows() {
      return (PersonTableRowData[]) super.getRows();
    }

    @Override
    public PersonTableRowData rowAt(int index) {
      return (PersonTableRowData) super.rowAt(index);
    }

    public void setRows(PersonTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class PersonTableRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String personNr = "personNr";
      public static final String name = "name";
      public static final String anObject = "anObject";
      public static final String smartLong = "smartLong";
      public static final String custom = "custom";
      private Long m_personNr;
      private String m_name;
      private Object m_anObject;
      private Long m_smartLong;
      private Set<Map<String, Integer>> m_custom;

      public Long getPersonNr() {
        return m_personNr;
      }

      public void setPersonNr(Long newPersonNr) {
        m_personNr = newPersonNr;
      }

      public String getName() {
        return m_name;
      }

      public void setName(String newName) {
        m_name = newName;
      }

      public Object getAnObject() {
        return m_anObject;
      }

      public void setAnObject(Object newAnObject) {
        m_anObject = newAnObject;
      }

      public Long getSmartLong() {
        return m_smartLong;
      }

      public void setSmartLong(Long newSmartLong) {
        m_smartLong = newSmartLong;
      }

      public Set<Map<String, Integer>> getCustom() {
        return m_custom;
      }

      public void setCustom(Set<Map<String, Integer>> newCustom) {
        m_custom = newCustom;
      }
    }
  }

  public static class TableFieldWithExternalTable extends AbstractTableFieldBeanData {

    private static final long serialVersionUID = 1L;

    @Override
    public TableFieldWithExternalTableRowData addRow() {
      return (TableFieldWithExternalTableRowData) super.addRow();
    }

    @Override
    public TableFieldWithExternalTableRowData addRow(int rowState) {
      return (TableFieldWithExternalTableRowData) super.addRow(rowState);
    }

    @Override
    public TableFieldWithExternalTableRowData createRow() {
      return new TableFieldWithExternalTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TableFieldWithExternalTableRowData.class;
    }

    @Override
    public TableFieldWithExternalTableRowData[] getRows() {
      return (TableFieldWithExternalTableRowData[]) super.getRows();
    }

    @Override
    public TableFieldWithExternalTableRowData rowAt(int index) {
      return (TableFieldWithExternalTableRowData) super.rowAt(index);
    }

    public void setRows(TableFieldWithExternalTableRowData[] rows) {
      super.setRows(rows);
    }

    public static class TableFieldWithExternalTableRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String aa = "aa";
      public static final String xx = "xx";
      public static final String bb = "bb";
      private String m_aa;
      private String m_xx;
      private String m_bb;

      public String getAa() {
        return m_aa;
      }

      public void setAa(String newAa) {
        m_aa = newAa;
      }

      public String getXx() {
        return m_xx;
      }

      public void setXx(String newXx) {
        m_xx = newXx;
      }

      public String getBb() {
        return m_bb;
      }

      public void setBb(String newBb) {
        m_bb = newBb;
      }
    }
  }
}
