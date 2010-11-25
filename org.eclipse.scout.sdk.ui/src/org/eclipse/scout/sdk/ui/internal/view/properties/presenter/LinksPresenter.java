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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.ILink;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinkGroup;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public class LinksPresenter extends AbstractPresenter {

  private LinksPresenterModel m_linksProperty;

  public LinksPresenter(FormToolkit toolkit, Composite parent, LinksPresenterModel linksProperty) {
    super(toolkit, parent);
    m_linksProperty = linksProperty;
    GridLayout layout = new GridLayout(2, true);
    layout.marginHeight = 0;
    layout.horizontalSpacing = 3;
    layout.verticalSpacing = 3;
    layout.marginWidth = 0;
    getContainer().setLayout(layout);
    createContent(getContainer());
  }

  private void createContent(Composite container) {
    ILink[] globalLinks = getLinksProperty().getOrderdGlobalLinks();
    LinkGroup[] linkGroups = getLinksProperty().getOrderedNotEmtyGroups();
    if (globalLinks.length > 0) {
      Control globalGroup = createLinkGroup(container, null, globalLinks);
      GridData globData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);

      globData.horizontalSpan = (2 - (linkGroups.length % 2));
      globalGroup.setLayoutData(globData);
    }
    for (LinkGroup group : linkGroups) {
      Control uiGroup = createLinkGroup(container, group.getName(), group.getLinks());
      GridData formData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
      uiGroup.setLayoutData(formData);
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    for (ILink l : m_linksProperty.getOrderdGlobalLinks()) {
      l.dispose();
    }
    for (LinkGroup g : m_linksProperty.getOrderedNotEmtyGroups()) {
      for (ILink l : g.getLinks()) {
        l.dispose();
      }
    }
  }

  private Control createLinkGroup(Composite parent, String title, ILink[] links) {
    Composite group = null;
    if (StringUtility.isNullOrEmpty(title)) {
      group = new Composite(parent, SWT.NONE);
    }
    else {
      group = new Group(parent, SWT.SHADOW_ETCHED_OUT);
      ((Group) group).setText(title);
    }
    getToolkit().adapt(group);
    for (ILink link : links) {
      createHyperlink(group, link);
    }
    // layout
    GridLayout layout = new GridLayout(1, true);
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    group.setLayout(layout);
    return group;
  }

  private Control createHyperlink(Composite parent, ILink link) {
    ImageHyperlink field = getToolkit().createImageHyperlink(parent, SWT.NONE);
    field.setUnderlined(true);
    field.setText(link.getName());
    field.setImage(link.getImage());
    field.addHyperlinkListener(new P_ExecuteLinkListener(link));
    GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
    field.setLayoutData(layoutData);
    return field;
  }

  private void doShowSourceCode() {

  }

  public void setLinksProperty(LinksPresenterModel linksProperty) {
    m_linksProperty = linksProperty;
  }

  public LinksPresenterModel getLinksProperty() {
    return m_linksProperty;
  }

  private class P_ExecuteLinkListener extends HyperlinkAdapter {
    private final ILink m_link;

    public P_ExecuteLinkListener(ILink link) {
      m_link = link;
    }

    @Override
    public void linkActivated(HyperlinkEvent e) {
      m_link.execute();
    }
  }

}
