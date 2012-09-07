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
package org.eclipse.scout.sdk.ws.jaxws.swt.view.part;

import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.AbstractSinglePageSectionBasedViewPart;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.SessionFactoryNewAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.SessionFactoryTablePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.swt.layout.GridData;

public class SessionFactoryTablePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final String SECTION_ID_FILTER = "section.jaxws.filter";
  public static final String SECTION_ID_SESSION_FACTORIES = "section.jaxws.links";

  private IScoutBundle m_bundle;

  @Override
  protected void init() {
    m_bundle = getPage().getScoutResource();
  }

  @Override
  protected void createSections() {
    getForm().setRedraw(false);
    try {
      createSection(SECTION_ID_FILTER, Texts.get("Filter"));
      getSection(SECTION_ID_FILTER).setExpanded(false);
      createSection(SECTION_ID_SESSION_FACTORIES, Texts.get("ServerSessionFactories"), Texts.get("DescriptionServerSerssionFactory"), true);

      // filter section
      PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), getSection(SECTION_ID_FILTER).getSectionClient(), getPage());
      applyLayoutData(filterPresenter);

      // QuickLink 'Create new Session Factory'
      SessionFactoryNewAction action = new SessionFactoryNewAction();
      action.init(m_bundle);
      ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_SESSION_FACTORIES).getSectionClient(), action, getFormToolkit());
      applyLayoutData(actionPresenter);
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  @Override
  public SessionFactoryTablePage getPage() {
    return (SessionFactoryTablePage) super.getPage();
  }

  private void applyLayoutData(AbstractPresenter presenter) {
    GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    presenter.getContainer().setLayoutData(layoutData);
  }
}
