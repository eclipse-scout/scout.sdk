/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
