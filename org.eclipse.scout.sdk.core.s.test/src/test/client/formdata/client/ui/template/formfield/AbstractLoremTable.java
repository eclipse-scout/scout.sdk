/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.template.formfield;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;

/**
 *
 */
public abstract class AbstractLoremTable extends AbstractTable {

  public BbColumn getBbColumn() {
    return getColumnSet().getColumnByClass(BbColumn.class);
  }

  public AaColumn getAaColumn() {
    return getColumnSet().getColumnByClass(AaColumn.class);
  }

  @Order(1000.0)
  public class AaColumn extends AbstractStringColumn {
  }

  @Order(2000.0)
  public class BbColumn extends AbstractStringColumn {
  }

}
