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
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

import formdata.client.ClientAnnotation;
import formdata.client.ui.forms.AnnotationCopyTestForm.MainBox.CancelButton;
import formdata.client.ui.forms.AnnotationCopyTestForm.MainBox.FirstField;
import formdata.client.ui.forms.AnnotationCopyTestForm.MainBox.OkButton;
import formdata.shared.SharedAnnotation;
import formdata.shared.ui.forms.AnnotationCopyTestFormData;

/**
 *
 */
@FormData(value = AnnotationCopyTestFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class AnnotationCopyTestForm extends AbstractForm {

  public void startNew() {
    startInternal(new NewHandler());
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public FirstField getFirstField() {
    return getFieldByClass(FirstField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    @ClientAnnotation
    @SharedAnnotation(type = IRunnable.class)
    public class FirstField extends AbstractStringField {
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
