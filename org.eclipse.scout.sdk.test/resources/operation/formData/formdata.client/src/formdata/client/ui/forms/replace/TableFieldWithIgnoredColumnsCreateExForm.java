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
package formdata.client.ui.forms.replace;

import org.eclipse.scout.commons.annotations.ColumnData;
import org.eclipse.scout.commons.annotations.ColumnData.SdkColumnCommand;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.exception.ProcessingException;

import formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsCreateExFormData;

@FormData(value = TableFieldWithIgnoredColumnsCreateExFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldWithIgnoredColumnsCreateExForm extends TableFieldWithIgnoredColumnsBaseForm {

  public TableFieldWithIgnoredColumnsCreateExForm() throws ProcessingException {
    super();
  }

  @Replace
  public class TableCreateExField extends TableFieldWithIgnoredColumnsBaseForm.MainBox.TableBaseField {

    public TableCreateExField(TableFieldWithIgnoredColumnsBaseForm.MainBox container) {
      container.super();
    }

    public class CreateExTable extends Table {

      public DefaultCreateColumn getDefaultCreateColumn() {
        return getColumnSet().getColumnByClass(DefaultCreateColumn.class);
      }

      public CreateCreateColumn getCreateCreateColumn() {
        return getColumnSet().getColumnByClass(CreateCreateColumn.class);
      }

      public IgnoreCreateColumn getIgnoreCreateColumn() {
        return getColumnSet().getColumnByClass(IgnoreCreateColumn.class);
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class DefaultCreateColumn extends DefaultColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class CreateCreateColumn extends CreateColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.CREATE)
      public class IgnoreCreateColumn extends IgnoreColumn {
      }
    }
  }
}
