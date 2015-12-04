/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package formdata.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.dto.ColumnData;
import org.eclipse.scout.rt.client.dto.ColumnData.SdkColumnCommand;
import org.eclipse.scout.rt.client.dto.PageData;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

import formdata.shared.services.pages.ExtendedTablePageData;

@PageData(ExtendedTablePageData.class)
public class ExtendedTablePage extends BaseTablePage {

  public class Table extends BaseTablePage.Table {

    public IntermediateColumn getIntermediateColumn() {
      return getColumnSet().getColumnByClass(IntermediateColumn.class);
    }

    public SecondColumnEx getSecondColumnEx() {
      return getColumnSet().getColumnByClass(SecondColumnEx.class);
    }

    public IgnoredColumnEx getIgnoredColumnEx() {
      return getColumnSet().getColumnByClass(IgnoredColumnEx.class);
    }

    @Order(15)
    public class IntermediateColumn extends AbstractBigDecimalColumn {
    }

    @Replace
    public class SecondColumnEx extends SecondColumn {
    }

    @Replace
    @ColumnData(SdkColumnCommand.CREATE)
    public class IgnoredColumnEx extends IgnoredColumn {
    }
  }
}
