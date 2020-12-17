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
