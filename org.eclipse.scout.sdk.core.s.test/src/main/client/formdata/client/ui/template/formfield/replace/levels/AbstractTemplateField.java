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
package formdata.client.ui.template.formfield.replace.levels;

import java.util.List;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.holders.IHolder;

import formdata.shared.ui.template.formfield.replace.levels.AbstractTemplateFieldData;

@FormData(value = AbstractTemplateFieldData.class, defaultSubtypeSdkCommand = FormData.DefaultSubtypeSdkCommand.CREATE, sdkCommand = FormData.SdkCommand.USE, genericOrdinal = 0)
public abstract class AbstractTemplateField<T> extends AbstractTableField<AbstractTemplateField<T>.Table> implements IHolder<List<T>> {

  public class Table extends AbstractTable {

  }

  @FormData
  @Override
  public List<T> getValue() {
    return null;
  }

  @FormData
  @Override
  public void setValue(List<T> o) {
  }

  @FormData
  @Override
  public Class<List<T>> getHolderType() {
    return null;
  }
}
