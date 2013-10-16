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
package formdata.client.ui.forms;

import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.Replace;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;

import formdata.shared.services.ChildWithExtendedTableFormData;

/**
 * 
 */
@FormData(value = ChildWithExtendedTableFormData.class, sdkCommand = SdkCommand.CREATE)
public class ChildWithExtendedTableForm extends BaseWithExtendedTableForm {

  /**
   * @throws org.eclipse.scout.commons.exception.ProcessingException
   */
  public ChildWithExtendedTableForm() throws ProcessingException {
    super();
  }

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
