/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package formdata.client.ui.template.formfield;

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;

import formdata.shared.IConstants;

public abstract class AbstractLimitedStringField extends AbstractStringField {
  @Override
  protected final int getConfiguredMaxLength() {
    return IConstants.MAX_LENGTH * 4;
  }
}
