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

import java.util.UUID;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.marker.IMarkerRebuildListener;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerUtility;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.CorruptSunJaxWsXmlFileCommand;
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.MissingJaxWsServletRegistrationCommand;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.ProviderNewWizardAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class WebServiceProviderTablePage extends AbstractPage implements IMarkerRebuildListener {

  public static final int DATA_SUN_JAXWS_FILE = 1 << 0;

  private String m_markerGroupUUID;
  private IScoutBundle m_bundle; // necessary to be hold as in method unloadPage, a reference to the bundle is required

  private ScoutXmlDocument m_sunJaxWsXml;
  private IResourceListener m_resourceListener;
  private boolean m_pageUnloaded = false;

  public WebServiceProviderTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Services"));
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsIcons.WebserviceProviderFolder));

    m_bundle = getScoutResource();
    m_markerGroupUUID = UUID.randomUUID().toString();
    m_resourceListener = new P_SunJaxWsResourceListener();

    // register for events being interest in
    int event = IResourceListener.EVENT_SUNJAXWS_ENTRY_ADDED |
                IResourceListener.EVENT_SUNJAXWS_ENTRY_REMOVED |
                IResourceListener.EVENT_SUNJAXWS_REPLACED |
                IResourceListener.EVENT_UNKNOWN;
    getSunJaxWsResource().addResourceListener(event, m_resourceListener);
    getSunJaxWsResource().addResourceListener(IResourceListener.ELEMENT_FILE, m_resourceListener);

    reloadData(DATA_SUN_JAXWS_FILE);
  }

  public XmlResource getSunJaxWsResource() {
    return ResourceFactory.getSunJaxWsResource(m_bundle);
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
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public void prepareMenuAction(AbstractScoutHandler menu) {
    if (menu instanceof ProviderNewWizardAction) {
      ((ProviderNewWizardAction) menu).init(getScoutResource());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends AbstractScoutHandler>[] getSupportedMenuActions() {
    if (m_sunJaxWsXml != null && m_sunJaxWsXml.getRoot() != null && JaxWsSdkUtility.getJaxWsAlias(m_bundle) != null) {
      return new Class[]{ProviderNewWizardAction.class};
    }
    return null;
  }

  @Override
  public void unloadPage() {
    m_pageUnloaded = true;

    MarkerUtility.clearMarkers(m_bundle, m_markerGroupUUID);
    getSunJaxWsResource().removeResourceListener(m_resourceListener);
    super.unloadPage();
  }

  @Override
  public int getQuality() {
    return MarkerUtility.getQuality(this, m_bundle, m_markerGroupUUID);
  }

  /**
   * Reloads data of this node page
   * The data value is either one of the data constants defined in
   * class {@link WebServiceProviderTablePage} or must be built by <em>bitwise OR</em>'ing together
   * 
   * @param data
   */
  public void reloadData(int data) {
    if ((data & DATA_SUN_JAXWS_FILE) > 0) {
      m_sunJaxWsXml = getSunJaxWsResource().loadXml();
    }
    JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(this);
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache) {
      super.refresh(clearCache);
    }
    else {
      JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(this);
    }
  }

  @Override
  protected void loadChildrenImpl() {
    try {
      if (m_sunJaxWsXml == null || m_sunJaxWsXml.getRoot() == null) {
        return;
      }

      for (Object sunJaxWsXml : m_sunJaxWsXml.getRoot().getChildren(StringUtility.join(":", m_sunJaxWsXml.getRoot().getNamePrefix(), SunJaxWsBean.XML_ENDPOINT))) {
        SunJaxWsBean sunJaxWsBean = new SunJaxWsBean((ScoutXmlElement) sunJaxWsXml);
        BuildJaxWsBean buildJaxWsBean = BuildJaxWsBean.load(m_bundle, sunJaxWsBean.getAlias());

        if (buildJaxWsBean != null) {
          new WebServiceProviderNodePage(this, sunJaxWsBean, buildJaxWsBean);
        }
        else {
          new WebServiceProviderCodeFirstNodePage(this, sunJaxWsBean);
        }
      }
    }
    finally {
      JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(this);
    }
  }

  @Override
  public void rebuildMarkers() {
    synchronized (m_markerGroupUUID) {
      try {
        MarkerUtility.clearMarkers(m_bundle, m_markerGroupUUID);

        if (isPageUnloaded()) {
          return;
        }

        if (m_sunJaxWsXml == null || m_sunJaxWsXml.getRoot() == null) {
          String markerSourceId = MarkerUtility.createMarker(m_bundle.getJavaProject().getResource(), m_markerGroupUUID, "Missing or corrupt file '" + JaxWsConstants.PATH_SUN_JAXWS + "'.\nThis file contains the webservices to be installed with their respective properties.");
          JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new CorruptSunJaxWsXmlFileCommand(m_bundle));
        }

        String jaxWsAlias = JaxWsSdkUtility.getJaxWsAlias(m_bundle);
        if (jaxWsAlias == null) {
          String markerSourceId = MarkerUtility.createMarker(m_bundle.getJavaProject().getResource(), m_markerGroupUUID, "Missing or invalid JAX-WS servlet registration in plugin.xml of root server Plug-In.");
          JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new MissingJaxWsServletRegistrationCommand(m_bundle));
        }
      }
      finally {
        ScoutSeverityManager.getInstance().fireSeverityChanged(JaxWsSdkUtility.createResourceSet(getSunJaxWsResource().getFile()));
      }
    }
  }

  public String getMarkerGroupUUID() {
    return m_markerGroupUUID;
  }

  public boolean isPageUnloaded() {
    return m_pageUnloaded;
  }

  private class P_SunJaxWsResourceListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      reloadData(DATA_SUN_JAXWS_FILE);
      markStructureDirty();
    }
  }
}
