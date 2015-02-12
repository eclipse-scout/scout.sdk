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
package formdata.client.ui.template.formfield;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

import formdata.client.ui.template.formfield.AbstractLoremTableField.Table;

/**
 *
 */
@FormData(value = AbstractTableFieldBeanData.class, sdkCommand = SdkCommand.USE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractLoremTableField extends AbstractTableField<Table> {
  public class Table extends AbstractLoremTable {

    public XxColumn getXxColumn() {
      return getColumnSet().getColumnByClass(XxColumn.class);
    }

    @Order(1000.0)
    public class XxColumn extends AbstractStringColumn {
    }

  }
}
