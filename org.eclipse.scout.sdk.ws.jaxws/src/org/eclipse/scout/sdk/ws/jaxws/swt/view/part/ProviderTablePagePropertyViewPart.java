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

import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.AbstractSinglePageSectionBasedViewPart;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.JaxWsAliasChangeWizardAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.ProviderTablePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.swt.layout.GridData;

public class ProviderTablePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final String SECTION_ID_GENERAL = "section.jaxws.general";

  private IScoutBundle m_bundle;

  @Override
  protected void init() {
    m_bundle = getPage().getScoutBundle();
  }

  @Override
  protected void createSections() {
    getForm().setRedraw(false);
    try {
      createSection(SECTION_ID_GENERAL, Texts.get("General"));

      // Change webservice servlet alias
      JaxWsAliasChangeWizardAction e = new JaxWsAliasChangeWizardAction();
      e.init(m_bundle);
      ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_GENERAL).getSectionClient(), e, getFormToolkit());
      applyLayoutData(actionPresenter);
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  @Override
  public ProviderTablePage getPage() {
    return (ProviderTablePage) super.getPage();
  }

  private void applyLayoutData(AbstractPresenter presenter) {
    GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    presenter.getContainer().setLayoutData(layoutData);
  }
}
