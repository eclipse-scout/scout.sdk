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
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.ui.forms.MasterFieldTestForm.MainBox.CancelButton;
import formdata.client.ui.forms.MasterFieldTestForm.MainBox.OkButton;
import formdata.client.ui.forms.MasterFieldTestForm.MainBox.TopBox;
import formdata.client.ui.forms.MasterFieldTestForm.MainBox.TopBox.InnerBox;
import formdata.client.ui.forms.MasterFieldTestForm.MainBox.TopBox.InnerBox.MyMasterField;
import formdata.client.ui.forms.MasterFieldTestForm.MainBox.TopBox.MySlaveField;
import formdata.shared.services.MasterFieldTestFormData;

/**
 *
 */
@FormData(value = MasterFieldTestFormData.class, sdkCommand = SdkCommand.CREATE)
public class MasterFieldTestForm extends AbstractForm {

  public void startNew() {
    startInternal(new NewHandler());
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public InnerBox getInnerBox() {
    return getFieldByClass(InnerBox.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public MyMasterField getMyMasterField() {
    return getFieldByClass(MyMasterField.class);
  }

  public MySlaveField getMySlaveField() {
    return getFieldByClass(MySlaveField.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public TopBox getTopBox() {
    return getFieldByClass(TopBox.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class TopBox extends AbstractGroupBox {

      @Order(10.0)
      public class InnerBox extends AbstractGroupBox {

        @Order(10.0)
        public class MyMasterField extends AbstractStringField {
        }
      }

      @Order(20.0)
      public class MySlaveField extends AbstractStringField {

        @Override
        protected Class<? extends IValueField<?>> getConfiguredMasterField() {
          return MasterFieldTestForm.MainBox.TopBox.InnerBox.MyMasterField.class;
        }
      }
    }

    @Order(20.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(30.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  public class NewHandler extends AbstractFormHandler {
  }
}
