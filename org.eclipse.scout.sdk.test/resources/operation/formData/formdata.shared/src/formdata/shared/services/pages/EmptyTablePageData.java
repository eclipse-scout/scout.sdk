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
package formdata.shared.services.pages;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;

/**
 * <b>NOTE:</b><br>
 * This class is auto generated, no manual modifications recommended.
 * 
 * @generated
 */
public class EmptyTablePageData extends AbstractTablePageData {

  private static final long serialVersionUID = 1L;

  public EmptyTablePageData() {
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
