/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
