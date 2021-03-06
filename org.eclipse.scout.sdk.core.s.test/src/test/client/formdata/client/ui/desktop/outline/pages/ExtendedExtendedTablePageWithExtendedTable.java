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
package formdata.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.dto.PageData;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.services.pages.ExtendedExtendedTablePageWithExtendedTableData;

@PageData(ExtendedExtendedTablePageWithExtendedTableData.class)
public class ExtendedExtendedTablePageWithExtendedTable extends ExtendedTablePageWithoutExtendedTable {

  public class Table extends BaseTablePage.Table {

    public BooleanColumn getBooleanColumn() {
      return getColumnSet().getColumnByClass(BooleanColumn.class);
    }

    @Order(30)
    public class BooleanColumn extends AbstractBooleanColumn {

    }
  }
}
