/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class SeparatorPresenter extends AbstractPresenter {

  public SeparatorPresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    super(toolkit, parent);
    createPresenter();
  }

  public Control createPresenter() {
    Composite presenter = getContainer();
    presenter.setLayout(new GridLayout(1, false));
    Label separator = new Label(presenter, SWT.HORIZONTAL | SWT.SEPARATOR);
    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    return presenter;
  }
}
