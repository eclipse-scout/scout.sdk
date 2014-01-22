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
