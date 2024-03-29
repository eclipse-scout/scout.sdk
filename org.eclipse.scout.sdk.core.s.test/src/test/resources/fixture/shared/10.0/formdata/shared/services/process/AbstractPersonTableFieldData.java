/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.shared.services.process;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.template.formfield.AbstractPersonTableField", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public abstract class AbstractPersonTableFieldData extends AbstractTableFieldBeanData {

  private static final long serialVersionUID = 1L;

  @Override
  public AbstractPersonTableRowData addRow() {
    return (AbstractPersonTableRowData) super.addRow();
  }

  @Override
  public AbstractPersonTableRowData addRow(int rowState) {
    return (AbstractPersonTableRowData) super.addRow(rowState);
  }

  @Override
  public abstract AbstractPersonTableRowData createRow();

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

  public abstract static class AbstractPersonTableRowData extends AbstractTableRowData {

    private static final long serialVersionUID = 1L;
    public static final String personId = "personId";
    public static final String name = "name";
    public static final String female = "female";
    public static final String boolean_ = "boolean";
    public static final String assert_ = "assert";
    public static final String switch_ = "switch";
    private String m_personId;
    private String m_name;
    private Boolean m_female;
    private Boolean m_boolean;
    private Boolean m_assert;
    private Boolean m_switch;

    public String getPersonId() {
      return m_personId;
    }

    public void setPersonId(String newPersonId) {
      m_personId = newPersonId;
    }

    public String getName() {
      return m_name;
    }

    public void setName(String newName) {
      m_name = newName;
    }

    public Boolean getFemale() {
      return m_female;
    }

    public void setFemale(Boolean newFemale) {
      m_female = newFemale;
    }

    public Boolean getBoolean() {
      return m_boolean;
    }

    public void setBoolean(Boolean newBoolean) {
      m_boolean = newBoolean;
    }

    public Boolean getAssert() {
      return m_assert;
    }

    public void setAssert(Boolean newAssert) {
      m_assert = newAssert;
    }

    public Boolean getSwitch() {
      return m_switch;
    }

    public void setSwitch(Boolean newSwitch) {
      m_switch = newSwitch;
    }
  }
}
