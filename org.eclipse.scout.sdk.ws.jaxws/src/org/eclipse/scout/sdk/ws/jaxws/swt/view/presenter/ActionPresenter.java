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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.IPresenterAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public class ActionPresenter extends AbstractPresenter {

  private IPresenterAction m_presenterAction;

  private Label m_icon;
  private Text m_text;
  private ImageHyperlink m_link;

  public ActionPresenter(Composite parent, IPresenterAction presenterAction, PropertyViewFormToolkit toolkit) {
    super(toolkit, parent);
    m_presenterAction = presenterAction;
    createPresenter();
  }

  /**
   * To reset the action
   * 
   * @param action
   */
  public void setAction(IPresenterAction action) {
    m_presenterAction = action;

    if (!getContainer().isDisposed()) {
      for (Control child : getContainer().getChildren()) {
        child.dispose();
      }
    }

    createPresenter();
    getContainer().layout();
  }

  public Control createPresenter() {
    Composite presenter = getContainer();
    if (m_presenterAction == null) {
      return presenter; // not initialized yet
    }

    // icon
    m_icon = new Label(presenter, SWT.NONE);
    m_icon.setToolTipText(m_presenterAction.getToolTip());
    if (m_presenterAction.getImage() != null) {
      m_icon.setImage(ScoutSdkUi.getImage(new JavaElementImageDescriptor(m_presenterAction.getImage(), SWT.NONE, new Point(16, 16))));
    }

    // text
    m_text = new Text(presenter, SWT.READ_ONLY | SWT.MULTI);
    if (StringUtility.hasText(m_presenterAction.getLeadingText())) {
      m_text.setText(m_presenterAction.getLeadingText());
    }
    m_text.setToolTipText(m_presenterAction.getToolTip());

    // link
    m_link = getToolkit().createImageHyperlink(presenter, SWT.NONE);
    if (StringUtility.hasText(m_presenterAction.getLinkText())) {
      m_link.setText(m_presenterAction.getLinkText());
    }
    m_link.setToolTipText(m_presenterAction.getToolTip());
    m_link.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(HyperlinkEvent e) {
        try {
          m_presenterAction.execute(null, null, null);
        }
        catch (ExecutionException e1) {
          JaxWsSdk.logError(e1);
        }
      }
    });

    // layout
    GridLayout layout = new GridLayout(3, false);
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    presenter.setLayout(layout);

    // icon
    GridData gd = new GridData();
    gd.exclude = (m_icon.getImage() == null);
    gd.widthHint = 20;
    m_icon.setLayoutData(gd);

    // text
    gd = new GridData();
    gd.exclude = !StringUtility.hasText(m_presenterAction.getLeadingText());
    m_text.setLayoutData(gd);

    // link
    gd = new GridData();
    gd.exclude = !StringUtility.hasText(m_presenterAction.getLinkText());
    m_link.setLayoutData(gd);

    return presenter;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (!isDisposed()) {
      m_link.setEnabled(enabled);
      m_icon.setEnabled(enabled);
      m_text.setEnabled(enabled);
    }
  }

  @Override
  public boolean isMultiLine() {
    return false;
  }
}
