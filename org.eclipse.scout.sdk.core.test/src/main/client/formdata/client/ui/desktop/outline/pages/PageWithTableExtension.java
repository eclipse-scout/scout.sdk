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

import org.eclipse.scout.commons.annotations.Data;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages.AbstractPageWithTableExtension;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;

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
