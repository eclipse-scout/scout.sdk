/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.scope.extended;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;

import formdata.client.scope.field.AbstractScopeTestGroupBox;
import formdata.client.scope.field.AbstractScopeTestGroupBox.ProcessField;
import formdata.client.scope.orig.ScopeTestForm;
import formdata.client.scope.orig.ScopeTestForm.MainBox.ProcessesBox;
import formdata.shared.scope.extended.ExtendedScopeTestFormData;

@Replace
@FormData(value = ExtendedScopeTestFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
public class ExtendedScopeTestForm extends ScopeTestForm {

  @Replace
  @Order(40.0)
  public class AnliegenBox extends ProcessesBox {
    public AnliegenBox(MainBox container) {
      container.super();
    }
  }

  @Replace
  @Order(66.0)
  public class ExtendedProcessField extends ProcessField {
    public ExtendedProcessField(AbstractScopeTestGroupBox container) {
      container.super();
    }
  }
}
