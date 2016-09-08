/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package formdata.client.scope.orig;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;

import formdata.client.scope.field.AbstractScopeTestGroupBox;
import formdata.shared.scope.orig.ScopeTestFormData;

@FormData(value = ScopeTestFormData.class, sdkCommand = SdkCommand.CREATE)
public class ScopeTestForm extends AbstractForm {
  @Order(10)
  public class MainBox extends AbstractGroupBox {
    @Order(50)
    public class ProcessesBox extends AbstractScopeTestGroupBox {
    }
  }
}
