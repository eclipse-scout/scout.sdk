/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
