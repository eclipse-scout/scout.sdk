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
package org.eclipse.scout.sdk.ws.jaxws.swt.view.pages;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.ProviderNewWizardAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class WebServiceProviderTablePage extends AbstractPage {

  private IScoutBundle m_bundle; // necessary to be hold as in method unloadPage, a reference to the bundle is required
  private ScoutXmlDocument m_sunJaxWsXml;
  private IResourceListener m_resourceListener;

  public WebServiceProviderTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Services"));
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsIcons.WebserviceProviderFolder));

    m_bundle = getScoutBundle();

    m_resourceListener = new P_SunJaxWsResourceListener();
    getSunJaxWsResource().addResourceListener(m_resourceListener);
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.WEBSERVICE_PROVIDER_TABLE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return true;
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof ProviderNewWizardAction) {
      ((ProviderNewWizardAction) menu).init(m_bundle);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return new Class[]{ProviderNewWizardAction.class};
  }

  @Override
  public void unloadPage() {
    getSunJaxWsResource().removeResourceListener(m_resourceListener);
    super.unloadPage();
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache) {
      m_sunJaxWsXml = null;
    }
    super.refresh(clearCache);
  }

  @Override
  protected void loadChildrenImpl() {
    if (m_sunJaxWsXml == null) {
      m_sunJaxWsXml = getSunJaxWsResource().loadXml();
    }
    for (ScoutXmlElement sunJaxWsXml : getEndpoints()) {
      SunJaxWsBean sunJaxWsBean = new SunJaxWsBean(sunJaxWsXml);
      BuildJaxWsBean buildJaxWsBean = BuildJaxWsBean.load(m_bundle, sunJaxWsBean.getAlias(), WebserviceEnum.Provider);

      if (buildJaxWsBean != null) {
        new WebServiceProviderNodePage(this, sunJaxWsBean.getAlias());
      }
      else {
        new WebServiceProviderCodeFirstNodePage(this, sunJaxWsBean);
      }
    }
  }

  private final XmlResource getSunJaxWsResource() {
    return ResourceFactory.getSunJaxWsResource(m_bundle);
  }

  private List<ScoutXmlElement> getEndpoints() {
    if (m_sunJaxWsXml == null || m_sunJaxWsXml.getRoot() == null) {
      return Collections.emptyList();
    }
    return m_sunJaxWsXml.getRoot().getChildren(StringUtility.join(":", m_sunJaxWsXml.getRoot().getNamePrefix(), SunJaxWsBean.XML_ENDPOINT));
  }

  private class P_SunJaxWsResourceListener implements IResourceListener {
    private boolean isContentAvailable(IFile file) {
      File osFile = file.getRawLocation().makeAbsolute().toFile();
      return osFile.length() > 0;
    }

    @Override
    public void changed(String element, int event) {
      if (!isContentAvailable(getSunJaxWsResource().getFile())) {
        return;
      }

      m_sunJaxWsXml = getSunJaxWsResource().loadXml();

      // if endpoint was added or removed, mark structure dirty
      final Set<String> endpoints = new HashSet<String>();
      final Set<String> endpointsLoaded = new HashSet<String>();

      for (ScoutXmlElement sunJaxWsXml : getEndpoints()) {
        SunJaxWsBean sunJaxWsBean = new SunJaxWsBean(sunJaxWsXml);
        endpoints.add(sunJaxWsBean.getAlias());
      }
      for (IPage page : getChildren()) {
        if (page instanceof WebServiceProviderNodePage) {
          endpointsLoaded.add(((WebServiceProviderNodePage) page).getAlias());
        }
      }
      if (!endpointsLoaded.equals(endpoints)) {
        JaxWsSdkUtility.markStructureDirtyAndFixSelection(WebServiceProviderTablePage.this);
      }
    }
  }
}
