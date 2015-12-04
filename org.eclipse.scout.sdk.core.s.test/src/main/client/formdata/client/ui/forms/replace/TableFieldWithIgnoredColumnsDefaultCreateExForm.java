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
package formdata.client.ui.forms.replace;

import org.eclipse.scout.rt.client.dto.ColumnData;
import org.eclipse.scout.rt.client.dto.ColumnData.SdkColumnCommand;
import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

import formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsDefaultCreateExFormData;

@FormData(value = TableFieldWithIgnoredColumnsDefaultCreateExFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldWithIgnoredColumnsDefaultCreateExForm extends TableFieldWithIgnoredColumnsBaseForm {

  public TableFieldWithIgnoredColumnsDefaultCreateExForm() throws ProcessingException {
    super();
  }

  @Replace
  public class TableDefaultCreateExField extends TableFieldWithIgnoredColumnsBaseForm.MainBox.TableBaseField {

    public TableDefaultCreateExField(TableFieldWithIgnoredColumnsBaseForm.MainBox container) {
      container.super();
    }

    public class DefaultCreateExTable extends Table {

      public DefaultDefaultCreateColumn getDefaultDefaultCreateColumn() {
        return getColumnSet().getColumnByClass(DefaultDefaultCreateColumn.class);
      }

      public CreateDefaultCreateColumn getCreateDefaultCreateColumn() {
        return getColumnSet().getColumnByClass(CreateDefaultCreateColumn.class);
      }

      public IgnoreDefaultCreateColumn getIgnoreDefaultCreateColumn() {
        return getColumnSet().getColumnByClass(IgnoreDefaultCreateColumn.class);
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class DefaultDefaultCreateColumn extends DefaultColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class CreateDefaultCreateColumn extends CreateColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class IgnoreDefaultCreateColumn extends IgnoreColumn {
      }
    }
  }
}
