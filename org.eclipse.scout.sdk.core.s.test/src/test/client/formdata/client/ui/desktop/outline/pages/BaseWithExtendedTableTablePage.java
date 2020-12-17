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
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.table.AbstractTestTableWithOneColumn;
import formdata.client.ui.desktop.outline.pages.BaseWithExtendedTableTablePage.Table;
import formdata.shared.services.BaseWithExtendedTableTablePageData;

/**
 *
 */
@PageData(BaseWithExtendedTableTablePageData.class)
public class BaseWithExtendedTableTablePage extends AbstractPageWithTable<Table> {

  @Order(10.0)
  public class Table extends AbstractTestTableWithOneColumn {
    @Order(30.0)
    public class ColInTablePage extends AbstractStringColumn {

    }
  }
}
