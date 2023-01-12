/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package formdata.client.ui.forms.replace;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

import formdata.shared.services.process.replace.TestingLookupCall;

public abstract class AbstractLookupField extends AbstractSmartField<Long> {

  @Override
  protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
    return TestingLookupCall.class;
  }
}
