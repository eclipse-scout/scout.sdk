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
import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.AbstractSinglePageSectionBasedViewPart;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.CleanupPhantomJarFileAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.FileOpenAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServicesTablePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.ActionPresenter;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter.InformationPresenter;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;

public class WebServicesTablePagePropertyViewPart extends AbstractSinglePageSectionBasedViewPart {

  public static final String SECTION_ID_LINKS = "section.jaxws.links";
  public static final String SECTION_ID_INFORMATION = "section.jaxws.information";

  private IScoutBundle m_bundle;

  @Override
  protected void init() {
    m_bundle = getPage().getScoutBundle();
  }

  @Override
  protected void createSections() {
    getForm().setRedraw(false);
    try {
      createSection(SECTION_ID_LINKS, Texts.get("ConsiderLinks"));

      // QuickLink 'Open build-jaxws.xml'
      IFile buildJaxWsFile = ResourceFactory.getBuildJaxWsResource(m_bundle).getFile();
      if (JaxWsSdkUtility.exists(buildJaxWsFile)) {
        FileOpenAction a = new FileOpenAction();
        a.setLinkText(buildJaxWsFile.getName());
        a.setImage(JaxWsSdk.getImageDescriptor(JaxWsSdk.BuildJaxWsXmlFile));
        a.setToolTip(Texts.get("JaxWsBuildDescriptor"));
        ActionPresenter presenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), a, getFormToolkit(), buildJaxWsFile);
        applyLayoutData(presenter);
      }

      // cleanup phantom JAR-Files
      CleanupPhantomJarFileAction b = new CleanupPhantomJarFileAction();
      AbstractPresenter presenter = new ActionPresenter(getSection(SECTION_ID_LINKS).getSectionClient(), b, getFormToolkit(), getPage());
      applyLayoutData(presenter);

      // JAX-WS library conflict
      if (TypeUtility.getTypes(javax.xml.ws.Service.class.getName()).size() > 1) {
        ISection section = createSection(SECTION_ID_INFORMATION, Texts.get("Information"), null, false);
        presenter = new InformationPresenter(section.getSectionClient(), "Multiple JAX-WS RI implementations detected which might cause problems at design and runtime. Please consider that the installed Scout JAX-WS integration supports JAX-WS RI 2.1.6 bundled with Java SE 6.", JaxWsSdk.getImageDescriptor(JaxWsIcons.LibraryConflict), new Point(48, 100), getFormToolkit());
        applyLayoutData(presenter);
      }
    }
    finally {
      getForm().setRedraw(true);
    }
  }

  @Override
  public WebServicesTablePage getPage() {
    return (WebServicesTablePage) super.getPage();
  }

  private void applyLayoutData(AbstractPresenter presenter) {
    GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    presenter.getContainer().setLayoutData(layoutData);
  }
}
