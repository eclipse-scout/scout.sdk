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

import org.eclipse.scout.commons.annotations.ColumnData;
import org.eclipse.scout.commons.annotations.ColumnData.SdkColumnCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.PageData;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;

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
