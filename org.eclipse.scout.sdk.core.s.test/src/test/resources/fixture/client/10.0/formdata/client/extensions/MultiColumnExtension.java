/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
