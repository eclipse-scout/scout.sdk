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

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.ui.desktop.outline.pages.BaseTablePage.Table;
import formdata.shared.services.pages.PageWithTableExtensionData;

@Data(PageWithTableExtensionData.class)
public class PageWithTableExtension extends AbstractPageWithTableExtension<BaseTablePage.Table, BaseTablePage> {

  public PageWithTableExtension(BaseTablePage owner) {
    super(owner);
  }

  public class TableExtension extends AbstractTableExtension<BaseTablePage.Table> {

    public TableExtension(Table owner) {
      super(owner);
    }

    @Order(1000.0)
    public class BigDecimalTestColumn extends AbstractBigDecimalColumn {
    }
  }
}
