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

import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public abstract class AbstractCompanyTableFieldData extends AbstractTableFieldData {

  private static final long serialVersionUID = 1L;
  public static final int NAME_COLUMN_ID = 0;

  public AbstractCompanyTableFieldData() {
  }

  public String getName(int row) {
    return (String) getValueInternal(row, NAME_COLUMN_ID);
  }

  public void setName(int row, String name) {
    setValueInternal(row, NAME_COLUMN_ID, name);
  }

  @Override
  public int getColumnCount() {
    return 1;
  }

  @Override
  public Object getValueAt(int row, int column) {
    switch (column) {
      case NAME_COLUMN_ID:
        return getName(row);
      default:
        return null;
    }
  }

  @Override
  public void setValueAt(int row, int column, Object value) {
    switch (column) {
      case NAME_COLUMN_ID:
        setName(row, (String) value);
        break;
    }
  }
}
