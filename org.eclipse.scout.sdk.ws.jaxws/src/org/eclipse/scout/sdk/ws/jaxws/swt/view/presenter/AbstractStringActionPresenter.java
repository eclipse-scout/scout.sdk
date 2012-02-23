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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public abstract class AbstractStringActionPresenter extends AbstractPropertyPresenter<String> {

  protected StyledText m_styledText;
  private String m_tooltip;
  protected ImageHyperlink m_actionLink;
  private String m_actionLinkTooltip;
  private boolean m_actionLinkEnabled;

  public AbstractStringActionPresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    super(parent, toolkit, false);
    setAcceptNullValue(true);
    setUseLinkAsLabel(false);
    setResetLinkVisible(false);
    setActionLinkEnabled(true);
    callInitializer();
  }

  public String getTooltip() {
    return m_tooltip;
  }

  @Override
  public void setTooltip(String tooltip) {
    m_tooltip = tooltip;
    if (isControlCreated()) {
      m_styledText.setToolTipText(StringUtility.nvl(m_tooltip, ""));
    }
  }

  public String getActionLinkTooltip() {
    return m_actionLinkTooltip;
  }

  public void setActionLinkTooltip(String actionTooltip) {
    m_actionLinkTooltip = actionTooltip;
    if (isControlCreated()) {
      m_actionLink.setToolTipText(StringUtility.nvl(actionTooltip, ""));
    }
  }

  public void setActionLinkEnabled(boolean actionLinkEnabled) {
    m_actionLinkEnabled = actionLinkEnabled;
    if (isControlCreated()) {
      m_actionLink.setEnabled(actionLinkEnabled);
    }
  }

  @Override
  protected Control createContent(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);

    m_styledText = new StyledText(composite, SWT.SINGLE | SWT.BORDER);
    m_styledText.setEditable(false);
    m_styledText.setBackground(JaxWsSdkUtility.getColorLightGray());
    m_styledText.setToolTipText(StringUtility.nvl(m_tooltip, ""));

    m_actionLink = getToolkit().createImageHyperlink(composite, SWT.NONE);
    m_actionLink.setToolTipText(StringUtility.nvl(m_actionLinkTooltip, ""));
    m_actionLink.setImage(JaxWsSdk.getImage(JaxWsIcons.UrlPattern));
    m_actionLink.setEnabled(m_actionLinkEnabled);
    m_actionLink.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(HyperlinkEvent event) {
        try {
          execAction();
        }
        catch (CoreException e) {
          JaxWsSdk.logError(e);
        }
      }
    });

    // layout
    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 0;
    layout.marginWidth = 0;
    layout.numColumns = 5;
    layout.marginBottom = 0;
    layout.marginTop = 0;
    layout.verticalSpacing = 0;
    layout.makeColumnsEqualWidth = false;
    composite.setLayout(layout);

    GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    gd.grabExcessHorizontalSpace = true;
    m_styledText.setLayoutData(gd);

    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    gd.horizontalIndent = 5;
    m_actionLink.setLayoutData(gd);

    return composite;
  }

  @Override
  protected void setInputInternal(String input) {
    m_styledText.setText(StringUtility.nvl(input, ""));
  }

  protected abstract void execAction() throws CoreException;
}
