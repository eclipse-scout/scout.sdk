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

import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
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
import org.eclipse.scout.sdk.ws.jaxws.marker.commands.CorruptBuildJaxWsXmlFileCommand;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class WebServicesTablePage extends AbstractPage implements IMarkerRebuildListener {

  private String m_markerGroupUUID;
  private IScoutBundle m_bundle; // necessary to be hold as in method unloadPage, a reference to the bundle is required

  private IResourceListener m_resourceListener;
  private ScoutXmlDocument m_buildJaxWsXml;
  private boolean m_pageUnloaded = false;

  public WebServicesTablePage(IPage parent) {
    setParent(parent);
    setName(Texts.get("Webservices"));
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsIcons.WebservicesFolder));

    m_bundle = getScoutResource();
    m_markerGroupUUID = UUID.randomUUID().toString();
    m_resourceListener = new P_BuildJaxWsResourceListener();

    // register for events being interest in
    int event = IResourceListener.EVENT_UNKNOWN |
                IResourceListener.EVENT_BUILDJAXWS_REPLACED |
                IResourceListener.EVENT_BUILDJAXWS_ENTRY_ADDED;
    getBuildJaxWsResource().addResourceListener(event, m_resourceListener);
    getBuildJaxWsResource().addResourceListener(IResourceListener.ELEMENT_FILE, m_resourceListener);

    reloadPage();
  }

  public XmlResource getBuildJaxWsResource() {
    return ResourceFactory.getBuildJaxWsResource(m_bundle);
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
  public int getQuality() {
    return MarkerUtility.getQuality(this, m_bundle, m_markerGroupUUID);
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
  public void unloadPage() {
    m_pageUnloaded = true;

    MarkerUtility.clearMarkers(m_bundle, m_markerGroupUUID);
    getBuildJaxWsResource().removeResourceListener(m_resourceListener);
    super.unloadPage();
  }

  public void reloadPage() {
    m_buildJaxWsXml = getBuildJaxWsResource().loadXml();
    JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(this);
  }

  @Override
  public void loadChildrenImpl() {
    try {
      if (m_buildJaxWsXml != null && m_buildJaxWsXml.getRoot() != null) {
        new ProviderTablePage(this);
        new ConsumerTablePage(this);
      }
      new HandlerTablePage(this);
      new SessionFactoryTablePage(this);
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

        if (m_buildJaxWsXml == null || m_buildJaxWsXml.getRoot() == null) {
          String markerSourceId = MarkerUtility.createMarker(m_bundle.getJavaProject().getResource(), m_markerGroupUUID, "Missing or corrupt file '" + JaxWsConstants.PATH_BUILD_JAXWS + "'.\nThis file holds the configuration to rebuild the webservice stub.");
          JaxWsSdk.getDefault().addMarkerCommand(markerSourceId, new CorruptBuildJaxWsXmlFileCommand(m_bundle));
        }
      }
      finally {
        ScoutSeverityManager.getInstance().fireSeverityChanged(JaxWsSdkUtility.createResourceSet(getBuildJaxWsResource().getFile()));
      }
    }
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.WEBSERVICES_TABLE_PAGE;
  }

  public String getMarkerGroupUUID() {
    return m_markerGroupUUID;
  }

  public boolean isPageUnloaded() {
    return m_pageUnloaded;
  }

  private class P_BuildJaxWsResourceListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      reloadPage();
      markStructureDirty();
    }
  }
}
