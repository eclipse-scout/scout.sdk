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
import org.eclipse.scout.sdk.ws.jaxws.swt.action.CredentialValidationStrategyNewAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.CredentialValidationStrategyTablePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.swt.layout.GridData;

public class CredentialValidationStrategyTablePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final String SECTION_ID_FILTER = "section.jaxws.filter";
  public static final String SECTION_ID_CREDENTIAL_VALIDATION_STRATEGIES = "section.jaxws.links";

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
      createSection(SECTION_ID_CREDENTIAL_VALIDATION_STRATEGIES, Texts.get("CredentialValidationStrategies"), Texts.get("DescriptionCredentialValidatinStrategy"), true);

      // filter section
      PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), getSection(SECTION_ID_FILTER).getSectionClient(), getPage());
      applyLayoutData(filterPresenter);

      // QuickLink 'Create new credential validation strategy'
      CredentialValidationStrategyNewAction action = new CredentialValidationStrategyNewAction();
      action.init(m_bundle);
      ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_CREDENTIAL_VALIDATION_STRATEGIES).getSectionClient(), action, getFormToolkit());
      applyLayoutData(actionPresenter);
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  @Override
  public CredentialValidationStrategyTablePage getPage() {
    return (CredentialValidationStrategyTablePage) super.getPage();
  }

  private void applyLayoutData(AbstractPresenter presenter) {
    GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    presenter.getContainer().setLayoutData(layoutData);
  }
}
