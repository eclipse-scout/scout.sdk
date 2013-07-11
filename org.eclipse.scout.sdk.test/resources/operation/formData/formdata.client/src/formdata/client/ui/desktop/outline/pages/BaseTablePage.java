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
package formdata.client.ui.desktop.outline.pages;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.PageData;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractDateColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

import formdata.shared.services.pages.BaseTablePageData;

@PageData(formdata.shared.services.pages.BaseTablePageData.class)
public class BaseTablePage extends AbstractPageWithTable<BaseTablePage.Table> {

  @Override
  protected void execLoadData(SearchFilter filter) throws ProcessingException {
    BaseTablePageData data = new BaseTablePageData();
    getTable().importFromTableBeanData(data);
  }

  public class Table extends AbstractTable {

    public FirstColumn getFirstColumn() {
      return getColumnSet().getColumnByClass(FirstColumn.class);
    }

    public SecondColumn getSecondColumn() {
      return getColumnSet().getColumnByClass(SecondColumn.class);
    }

    @Order(10)
    public class FirstColumn extends AbstractStringColumn {

    }

    @Order(20)
    public class SecondColumn extends AbstractDateColumn {

    }
  }
}
