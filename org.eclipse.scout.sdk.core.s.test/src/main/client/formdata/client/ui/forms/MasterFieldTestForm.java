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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

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

  /**
   * @throws org.eclipse.scout.commons.exception.ProcessingException
   */
  public MasterFieldTestForm() throws ProcessingException {
    super();
  }

  /**
   * @throws org.eclipse.scout.commons.exception.ProcessingException
   */
  public void startNew() throws ProcessingException {
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
