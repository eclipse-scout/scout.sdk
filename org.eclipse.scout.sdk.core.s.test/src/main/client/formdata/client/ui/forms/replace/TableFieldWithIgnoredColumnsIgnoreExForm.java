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

import formdata.shared.services.process.replace.TableFieldWithIgnoredColumnsIgnoreExFormData;

@FormData(value = TableFieldWithIgnoredColumnsIgnoreExFormData.class, sdkCommand = SdkCommand.CREATE)
public class TableFieldWithIgnoredColumnsIgnoreExForm extends TableFieldWithIgnoredColumnsBaseForm {

  public TableFieldWithIgnoredColumnsIgnoreExForm() throws ProcessingException {
    super();
  }

  @Replace
  public class TableIgnoreExField extends TableFieldWithIgnoredColumnsBaseForm.MainBox.TableBaseField {

    public TableIgnoreExField(TableFieldWithIgnoredColumnsBaseForm.MainBox container) {
      container.super();
    }

    public class IgnoreExTable extends Table {

      public DefaultIgnoreColumn getDefaultIgnoreColumn() {
        return getColumnSet().getColumnByClass(DefaultIgnoreColumn.class);
      }

      public CreateIgnoreColumn getCreateIgnoreColumn() {
        return getColumnSet().getColumnByClass(CreateIgnoreColumn.class);
      }

      public IgnoreIgnoreColumn getIgnoreIgnoreColumn() {
        return getColumnSet().getColumnByClass(IgnoreIgnoreColumn.class);
      }

      @Replace
      @ColumnData(SdkColumnCommand.IGNORE)
      public class DefaultIgnoreColumn extends DefaultColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.IGNORE)
      public class CreateIgnoreColumn extends CreateColumn {
      }

      @Replace
      @ColumnData(SdkColumnCommand.IGNORE)
      public class IgnoreIgnoreColumn extends IgnoreColumn {
      }
    }
  }
}
