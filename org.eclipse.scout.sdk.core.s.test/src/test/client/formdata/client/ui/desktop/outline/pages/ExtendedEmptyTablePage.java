/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.dto.PageData;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.services.pages.ExtendedEmptyTablePageData;

@PageData(ExtendedEmptyTablePageData.class)
public class ExtendedEmptyTablePage extends EmptyTablePage<ExtendedEmptyTablePage.Table> {

  public class Table extends AbstractTable {

    public NameColumn getNameColumn() {
      return getColumnSet().getColumnByClass(NameColumn.class);
    }

    @Order(10)
    public class NameColumn extends AbstractStringColumn {

    }
  }
}
