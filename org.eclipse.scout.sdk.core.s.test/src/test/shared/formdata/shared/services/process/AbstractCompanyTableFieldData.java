/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.shared.services.process;

import javax.annotation.Generated;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated by the Scout SDK. No manual modifications recommended.
 */
@Generated(value = "formdata.client.ui.template.formfield.AbstractCompanyTableField", comments = "This class is auto generated by the Scout SDK. No manual modifications recommended.")
public abstract class AbstractCompanyTableFieldData extends AbstractTableFieldBeanData {

  private static final long serialVersionUID = 1L;

  @Override
  public AbstractCompanyTableRowData addRow() {
    return (AbstractCompanyTableRowData) super.addRow();
  }

  @Override
  public AbstractCompanyTableRowData addRow(int rowState) {
    return (AbstractCompanyTableRowData) super.addRow(rowState);
  }

  @Override
  public abstract AbstractCompanyTableRowData createRow();

  @Override
  public Class<? extends AbstractTableRowData> getRowType() {
    return AbstractCompanyTableRowData.class;
  }

  @Override
  public AbstractCompanyTableRowData[] getRows() {
    return (AbstractCompanyTableRowData[]) super.getRows();
  }

  @Override
  public AbstractCompanyTableRowData rowAt(int index) {
    return (AbstractCompanyTableRowData) super.rowAt(index);
  }

  public void setRows(AbstractCompanyTableRowData[] rows) {
    super.setRows(rows);
  }

  public abstract static class AbstractCompanyTableRowData extends AbstractTableRowData {

    private static final long serialVersionUID = 1L;
    public static final String name = "name";
    private String m_name;

    public String getName() {
      return m_name;
    }

    public void setName(String newName) {
      m_name = newName;
    }
  }
}