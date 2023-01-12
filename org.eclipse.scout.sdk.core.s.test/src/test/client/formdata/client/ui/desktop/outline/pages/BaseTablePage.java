/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.dto.ColumnData;
import org.eclipse.scout.rt.client.dto.ColumnData.SdkColumnCommand;
import org.eclipse.scout.rt.client.dto.PageData;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

import formdata.shared.services.pages.BaseTablePageData;

@PageData(formdata.shared.services.pages.BaseTablePageData.class)
public class BaseTablePage extends AbstractPageWithTable<BaseTablePage.Table> {

  @Override
  protected void execLoadData(SearchFilter filter) {
    var data = new BaseTablePageData();
    getTable().importFromTableBeanData(data);
  }

  public class Table extends AbstractTable {

    public FirstColumn getFirstColumn() {
      return getColumnSet().getColumnByClass(FirstColumn.class);
    }

    public SecondColumn getSecondColumn() {
      return getColumnSet().getColumnByClass(SecondColumn.class);
    }

    public IgnoredColumn getIgnoredColumn() {
      return getColumnSet().getColumnByClass(IgnoredColumn.class);
    }

    @Order(10)
    public class FirstColumn extends AbstractStringColumn {
    }

    @Order(20)
    public class SecondColumn extends AbstractDateColumn {

    }

    @Order(30)
    @ColumnData(SdkColumnCommand.IGNORE)
    public class IgnoredColumn extends AbstractDateColumn {

    }
  }
}
