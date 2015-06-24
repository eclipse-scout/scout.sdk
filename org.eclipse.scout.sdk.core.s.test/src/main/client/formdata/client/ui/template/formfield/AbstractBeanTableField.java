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
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;

@FormData(sdkCommand = SdkCommand.USE, value = AbstractTableFieldBeanData.class, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractBeanTableField<T extends ITable> extends AbstractTableField<T> {

  public AbstractBeanTableField() {
    super();
  }

  public AbstractBeanTableField(boolean callInitializer) {
    super(callInitializer);
  }
}
