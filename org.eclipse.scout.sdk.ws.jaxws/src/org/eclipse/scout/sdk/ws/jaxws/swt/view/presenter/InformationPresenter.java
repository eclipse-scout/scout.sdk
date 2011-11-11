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

import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class InformationPresenter extends AbstractPresenter {

  private String m_text;
  private ImageDescriptor m_imageDescriptor;
  private Point m_iconDimension;

  public InformationPresenter(Composite parent, String text, FormToolkit toolkit) {
    this(parent, text, null, null, toolkit);
  }

  public InformationPresenter(Composite parent, String text, ImageDescriptor imageDescriptor, FormToolkit toolkit) {
    this(parent, text, imageDescriptor, new Point(16, 16), toolkit);
  }

  public InformationPresenter(Composite parent, String text, ImageDescriptor imageDescriptor, Point iconDimension, FormToolkit toolkit) {
    super(toolkit, parent);
    m_text = text;
    m_imageDescriptor = imageDescriptor;
    m_iconDimension = iconDimension;
    createPresenter();
  }

  public Control createPresenter() {
    Composite presenter = getContainer();

    // icon
    Label icon = new Label(presenter, SWT.NONE);
    if (m_imageDescriptor != null) {
      icon.setImage(ScoutSdkUi.getImage(new JavaElementImageDescriptor(m_imageDescriptor, SWT.NONE, m_iconDimension)));
    }

    // text
    Label text = new Label(presenter, SWT.WRAP);
    text.setText(m_text);

    // layout
    GridLayout layout = new GridLayout(3, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    presenter.setLayout(layout);

    GridData gd = new GridData();
    gd.exclude = (icon.getImage() == null);
    if (icon.getImage() != null) {
      gd.widthHint = m_iconDimension.x + 5;
    }
    icon.setLayoutData(gd);

    gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
    text.setLayoutData(gd);

    return presenter;
  }

  @Override
  public boolean isMultiLine() {
    return false;
  }
}
