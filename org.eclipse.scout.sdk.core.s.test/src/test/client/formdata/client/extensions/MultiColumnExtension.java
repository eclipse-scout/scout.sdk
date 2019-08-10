/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.extensions;

import org.eclipse.scout.rt.client.dto.Data;
import org.eclipse.scout.rt.client.extension.ui.basic.table.AbstractTableExtension;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBigDecimalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.ui.desktop.outline.pages.ExtendedEmptyTablePage;
import formdata.client.ui.desktop.outline.pages.ExtendedEmptyTablePage.Table;
import formdata.shared.extension.MultiColumnExtensionData;

@Data(MultiColumnExtensionData.class)
public class MultiColumnExtension extends AbstractTableExtension<ExtendedEmptyTablePage.Table> {

  /**
   * @param owner
   */
  public MultiColumnExtension(Table owner) {
    super(owner);
  }

  public ThirdLongColumn getThirdLongColumn() {
    return getOwner().getColumnSet().getColumnByClass(ThirdLongColumn.class);
  }

  public FourthDoubleColumn getFourthDoubleColumn() {
    return getOwner().getColumnSet().getColumnByClass(FourthDoubleColumn.class);
  }

  @Order(3000.0)
  public class ThirdLongColumn extends AbstractLongColumn {

  }

  @Order(4000.0)
  public class FourthDoubleColumn extends AbstractBigDecimalColumn {

  }
}
