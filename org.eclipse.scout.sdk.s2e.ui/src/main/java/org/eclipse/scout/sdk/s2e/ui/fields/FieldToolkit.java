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
package org.eclipse.scout.sdk.s2e.ui.fields;

import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.swt.widgets.Composite;

public class FieldToolkit {

  public StyledTextField createStyledTextField(Composite parent, String label) {
    return new StyledTextField(parent, label);
  }

  public StyledTextField createStyledTextField(Composite parent, String label, int labelPercentage) {
    return new StyledTextField(parent, label, labelPercentage);
  }
}
