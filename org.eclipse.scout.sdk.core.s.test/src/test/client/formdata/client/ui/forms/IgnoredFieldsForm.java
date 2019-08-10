/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.ui.forms;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.ui.forms.IgnoredFieldsForm.MainBox.AGroupBox;
import formdata.client.ui.forms.IgnoredFieldsForm.MainBox.AGroupBox.IgnoredIntegerField;
import formdata.client.ui.forms.IgnoredFieldsForm.MainBox.AGroupBox.NotIgnoredField;
import formdata.client.ui.forms.IgnoredFieldsForm.MainBox.IgnoredGroupBox;
import formdata.client.ui.forms.IgnoredFieldsForm.MainBox.IgnoredGroupBox.InheritedIgnoredField;
import formdata.shared.services.process.IgnoredFieldsFormData;

@FormData(value = IgnoredFieldsFormData.class, sdkCommand = SdkCommand.CREATE)
public class IgnoredFieldsForm extends AbstractForm {

  public AGroupBox getAGroupBox() {
    return getFieldByClass(AGroupBox.class);
  }

  public IgnoredGroupBox getIgnoredGroupBox() {
    return getFieldByClass(IgnoredGroupBox.class);
  }

  public IgnoredIntegerField getIgnoredIntegerField() {
    return getFieldByClass(IgnoredIntegerField.class);
  }

  public InheritedIgnoredField getInheritedIgnoredField() {
    return getFieldByClass(InheritedIgnoredField.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public NotIgnoredField getNotIgnoredField() {
    return getFieldByClass(NotIgnoredField.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class AGroupBox extends AbstractGroupBox {

      @Order(10.0)
      @FormData(sdkCommand = SdkCommand.IGNORE)
      public class IgnoredIntegerField extends AbstractIntegerField {
      }

      @Order(20.0)
      public class NotIgnoredField extends AbstractStringField {
      }
    }

    @Order(20.0)
    @FormData(sdkCommand = SdkCommand.IGNORE)
    public class IgnoredGroupBox extends AbstractGroupBox {

      @Order(10.0)
      public class InheritedIgnoredField extends AbstractStringField {
      }
    }
  }
}
