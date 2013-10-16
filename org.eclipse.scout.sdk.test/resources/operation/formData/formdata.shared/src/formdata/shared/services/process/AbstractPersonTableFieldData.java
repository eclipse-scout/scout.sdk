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
package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public abstract class AbstractPersonTableFieldData extends AbstractTableFieldBeanData {

  private static final long serialVersionUID = 1L;

  public AbstractPersonTableFieldData() {
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

  public static abstract class AbstractPersonTableRowData extends AbstractTableRowData {

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

    public AbstractPersonTableRowData() {
    }

    public String getPersonId() {
      return m_personId;
    }

    public void setPersonId(String personId) {
      m_personId = personId;
    }

    public String getName() {
      return m_name;
    }

    public void setName(String name) {
      m_name = name;
    }

    public Boolean getFemale() {
      return m_female;
    }

    public void setFemale(Boolean female) {
      m_female = female;
    }

    public Boolean getBoolean() {
      return m_boolean;
    }

    public void setBoolean(Boolean booleanValue) {
      m_boolean = booleanValue;
    }

    public Boolean getAssert() {
      return m_assert;
    }

    public void setAssert(Boolean assertValue) {
      m_assert = assertValue;
    }

    public Boolean getSwitch() {
      return m_switch;
    }

    public void setSwitch(Boolean switchValue) {
      m_switch = switchValue;
    }
  }
}
