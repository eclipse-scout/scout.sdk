/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.forms;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

import formdata.shared.services.ChildWithExtendedTableFormData;

/**
 *
 */
@FormData(value = ChildWithExtendedTableFormData.class, sdkCommand = SdkCommand.CREATE)
public class ChildWithExtendedTableForm extends BaseWithExtendedTableForm {

  @Order(10.0)
  public class MainBox extends formdata.client.ui.forms.BaseWithExtendedTableForm.MainBox {
    @Order(10.0)
    public class ChildTableField extends TableInFormField {
      public class InnerTab extends formdata.client.ui.forms.BaseWithExtendedTableForm.MainBox.TableInFormField.Table {
        @Order(-20.0)
        public class Col1InChildForm extends AbstractStringColumn {

        }

        @Replace
        public class Col2InChildFormReplacing extends ColInDesktopForm {

        }
      }
    }
  }
}
