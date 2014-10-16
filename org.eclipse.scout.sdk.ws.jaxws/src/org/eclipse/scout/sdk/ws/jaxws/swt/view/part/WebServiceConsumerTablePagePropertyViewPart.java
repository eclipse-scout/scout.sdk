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

import org.eclipse.core.resources.IFile;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.PageFilterPresenter;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.AbstractSinglePageSectionBasedViewPart;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.ConsumerNewWizardAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceConsumerTablePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.layout.GridData;

public class WebServiceConsumerTablePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final String SECTION_ID_FILTER = "section.jaxws.filter";
  public static final String SECTION_ID_LINKS = "section.jaxws.links";
  public static final String SECTION_ID_CONSUMER = "section.jaxws.wsconsumer";

  private IScoutBundle m_bundle;

  @Override
  protected void init() {
    m_bundle = getPage().getScoutBundle();
  }

  @Override
  protected void createSections() {
    getForm().setRedraw(false);
    try {
      createSection(SECTION_ID_FILTER, Texts.get("Filter"));
      getSection(SECTION_ID_FILTER).setExpanded(false);
      createSection(SECTION_ID_LINKS, Texts.get("ConsiderLinks"));
      createSection(SECTION_ID_CONSUMER, Texts.get("WebserviceConsumer"));
      boolean sectionLinksVisible = false;

      // filter section
      PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), getSection(SECTION_ID_FILTER).getSectionClient(), getPage());
      applyLayoutData(filterPresenter);

      // QuickLink section
      // QuickLink 'Open build-jaxws.xml'
      IFile buildJaxWsFile = ResourceFactory.getBuildJaxWsResource(m_bundle).getFile();
      if (JaxWsSdkUtility.exists(buildJaxWsFile)) {
        FileOpenAction b = new FileOpenAction();
        b.setLinkText(buildJaxWsFile.getName());
        b.setImage(JaxWsSdk.getImageDescriptor(JaxWsIcons.BuildJaxWsXmlFile));
        b.setToolTip(Texts.get("JaxWsBuildDescriptor"));
        ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), b, getFormToolkit(), buildJaxWsFile);
        applyLayoutData(actionPresenter);
        sectionLinksVisible = true;
      }

      // QuickLink 'Create new Webservice consumer'
      ConsumerNewWizardAction action = new ConsumerNewWizardAction();
      ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_CONSUMER).getSectionClient(), action, getFormToolkit(), getPage());
      applyLayoutData(actionPresenter);

      getSection(SECTION_ID_LINKS).setVisible(sectionLinksVisible);
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  @Override
  public WebServiceConsumerTablePage getPage() {
    return (WebServiceConsumerTablePage) super.getPage();
  }

  private void applyLayoutData(AbstractPresenter presenter) {
    GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    presenter.getContainer().setLayoutData(layoutData);
  }
}
