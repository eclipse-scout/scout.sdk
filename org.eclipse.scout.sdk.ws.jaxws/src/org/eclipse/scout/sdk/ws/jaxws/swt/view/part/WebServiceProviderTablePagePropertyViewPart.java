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
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction.FileExtensionType;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.ProviderNewWizardAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderTablePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.layout.GridData;

public class WebServiceProviderTablePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final String SECTION_ID_FILTER = "section.jaxws.filter";
  public static final String SECTION_ID_LINKS = "section.jaxws.links";
  public static final String SECTION_ID_PROVIDER = "section.jaxws.wsprovider";

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
      createSection(SECTION_ID_LINKS, Texts.get("ConsiderLinks"));
      createSection(SECTION_ID_PROVIDER, Texts.get("WebserviceProvider"));
      boolean sectionLinksVisible = false;

      // filter section
      PageFilterPresenter filterPresenter = new PageFilterPresenter(getFormToolkit(), getSection(SECTION_ID_FILTER).getSectionClient(), getPage());
      applyLayoutData(filterPresenter);

      // QuickLink 'Open sun-jaxws.xml'
      IFile sunJaxWsFile = ResourceFactory.getSunJaxWsResource(m_bundle).getFile();
      if (JaxWsSdkUtility.exists(sunJaxWsFile)) {
        FileOpenAction b = new FileOpenAction();
        b.init(sunJaxWsFile, sunJaxWsFile.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.SunJaxWsXmlFile), FileExtensionType.Xml);
        b.setToolTip(Texts.get("JaxWsDeploymentDescriptor"));
        ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), b, getFormToolkit());
        applyLayoutData(actionPresenter);
        sectionLinksVisible = true;
      }

      // QuickLink 'Open build-jaxws.xml'
      IFile buildJaxWsFile = ResourceFactory.getBuildJaxWsResource(m_bundle).getFile();
      if (JaxWsSdkUtility.exists(buildJaxWsFile)) {
        FileOpenAction c = new FileOpenAction();
        c.init(buildJaxWsFile, buildJaxWsFile.getName(), JaxWsSdk.getImageDescriptor(JaxWsIcons.BuildJaxWsXmlFile), FileExtensionType.Xml);
        c.setToolTip(Texts.get("JaxWsBuildDescriptor"));
        ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), c, getFormToolkit());
        applyLayoutData(actionPresenter);
        sectionLinksVisible = true;
      }
      getSection(SECTION_ID_LINKS).setVisible(sectionLinksVisible);

      // QuickLink 'Create new Provider'
      ProviderNewWizardAction d = new ProviderNewWizardAction();
      d.init(m_bundle);
      ActionPresenter actionPresenter = new ActionPresenter(getSection(SECTION_ID_PROVIDER).getSectionClient(), d, getFormToolkit());
      applyLayoutData(actionPresenter);
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  @Override
  public WebServiceProviderTablePage getPage() {
    return (WebServiceProviderTablePage) super.getPage();
  }

  private void applyLayoutData(AbstractPresenter presenter) {
    GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    presenter.getContainer().setLayoutData(layoutData);
  }
}
