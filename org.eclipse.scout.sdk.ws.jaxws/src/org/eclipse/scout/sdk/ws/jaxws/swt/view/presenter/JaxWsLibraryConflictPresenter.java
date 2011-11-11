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

import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class JaxWsLibraryConflictPresenter extends AbstractPresenter {

  public JaxWsLibraryConflictPresenter(Composite parent, FormToolkit toolkit) {
    super(toolkit, parent);
    createPresenter();
  }

  public Control createPresenter() {
    Composite presenter = getContainer();

    // text
    final Label text = new Label(presenter, SWT.WRAP | SWT.LEFT);
    text.setBackground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    text.addPaintListener(new PaintListener() {
      @Override
      public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        gc.setBackground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        Rectangle rect = text.getBounds();
        Rectangle border = new Rectangle(rect.x, rect.y, rect.width - 12, rect.height);
        gc.drawRectangle(border);
      }
    });

    text.setBackground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    text.setText("Conflicasdfasdfas\nfas dfasdf asf \nasdf asf asf asdf asdf asdfdfasdf");

    // layout
    GridLayout layout = new GridLayout(1, true);
    layout.marginWidth = 20;
    layout.marginHeight = 0;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    presenter.setLayout(layout);

    // icon
    GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
    text.setLayoutData(gd);

    return presenter;
  }

  @Override
  public boolean isMultiLine() {
    return false;
  }
}
