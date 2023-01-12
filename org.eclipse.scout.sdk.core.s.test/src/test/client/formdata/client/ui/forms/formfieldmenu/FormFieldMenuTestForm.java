/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.forms.formfieldmenu;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.ui.action.menu.form.fields.AbstractFormFieldMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.AbstractBooleanField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;

import formdata.shared.ui.forms.formfieldmenu.FormFieldMenuTestFormData;

@FormData(value = FormFieldMenuTestFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class FormFieldMenuTestForm extends AbstractForm {

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(5)
    public class TestFormMenu extends AbstractFormFieldMenu {

      @Order(10)
      public class TestBooleanField extends AbstractBooleanField {

      }
    }
  }
}
