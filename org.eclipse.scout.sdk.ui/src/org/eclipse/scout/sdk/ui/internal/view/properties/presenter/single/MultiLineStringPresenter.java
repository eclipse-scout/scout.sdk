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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>StringPresenter</h3> Representing a plain text property method.
 * References like 'm_value' or 'IConstants.ASTRING' are handled.
 */
public class MultiLineStringPresenter extends StringPresenter {

  public MultiLineStringPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected String formatSourceValue(String value) throws CoreException {
    return "\"" + value + "\"";
  }

  @Override
  protected String formatDisplayValue(String value) throws CoreException {
    return value;
  }

  @Override
  public boolean isMultiLine() {
    return true;
  }
}
