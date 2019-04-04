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
package formdata.client.ui.forms;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.AbstractHtmlField;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.ui.forms.UsingTemplateForm.MainBox.ExternalGroupBox;
import formdata.client.ui.forms.UsingTemplateForm.MainBox.ExternalWithNoAnnotationBox;
import formdata.client.ui.forms.UsingTemplateForm.MainBox.InternalGroupBox;
import formdata.client.ui.forms.UsingTemplateForm.MainBox.InternalGroupBox.InternalHtmlField;
import formdata.client.ui.forms.UsingTemplateForm.MainBox.InternalGroupBox.TestCheckboxField;
import formdata.client.ui.forms.UsingTemplateForm.MainBox.InternalGroupBox.TestLimitedStringField;
import formdata.client.ui.template.formfield.AbstractExternalGroupBox;
import formdata.client.ui.template.formfield.AbstractExternalGroupBox.ExternalStringField;
import formdata.client.ui.template.formfield.AbstractExternalWithNoAnnotationBox;
import formdata.client.ui.template.formfield.AbstractExternalWithNoAnnotationBox.NameField;
import formdata.client.ui.template.formfield.AbstractExternalWithNoAnnotationBox.PlzField;
import formdata.client.ui.template.formfield.AbstractLimitedStringField;
import formdata.client.ui.template.formfield.AbstractTestCheckboxField;
import formdata.shared.services.process.UsingTemplateFormData;

@FormData(value = UsingTemplateFormData.class, sdkCommand = SdkCommand.CREATE)
public class UsingTemplateForm extends AbstractForm {

  public ExternalGroupBox getExternalGroupBox() {
    return getFieldByClass(ExternalGroupBox.class);
  }

  /**
   * @deprecated Use {@link #getExternalWithNoAnnotationBox()#getNameField()}
   */
  @Deprecated
  public NameField getNameField() {
    return getExternalWithNoAnnotationBox().getNameField();
  }

  /**
   * @deprecated Use {@link #getExternalWithNoAnnotationBox()#getPlzField()}
   */
  @Deprecated
  public PlzField getPlzField() {
    return getExternalWithNoAnnotationBox().getPlzField();
  }

  public TestCheckboxField getTestCheckboxField() {
    return getFieldByClass(TestCheckboxField.class);
  }

  /**
   * @deprecated Use {@link #getExternalGroupBox()#getExternalStringField()}
   */
  @Deprecated
  public ExternalStringField getExternalStringField() {
    return getExternalGroupBox().getExternalStringField();
  }

  public ExternalWithNoAnnotationBox getExternalWithNoAnnotationBox() {
    return getFieldByClass(ExternalWithNoAnnotationBox.class);
  }

  public InternalGroupBox getInternalGroupBox() {
    return getFieldByClass(InternalGroupBox.class);
  }

  public InternalHtmlField getInternalHtmlField() {
    return getFieldByClass(InternalHtmlField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public TestLimitedStringField getTestLimitedStringField() {
    return getFieldByClass(TestLimitedStringField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class InternalGroupBox extends AbstractGroupBox {

      @Order(10.0)
      public class InternalHtmlField extends AbstractHtmlField {
      }

      @Order(20.0)
      public class TestCheckboxField extends AbstractTestCheckboxField {
      }

      @Order(30.0)
      public class TestLimitedStringField extends AbstractLimitedStringField {

      }
    }

    @Order(20.0)
    public class ExternalGroupBox extends AbstractExternalGroupBox {

    }

    @Order(30.0)
    public class ExternalWithNoAnnotationBox extends AbstractExternalWithNoAnnotationBox {

    }
  }
}
