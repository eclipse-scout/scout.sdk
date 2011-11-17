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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.marker.IMarkerRebuildListener;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerRebuildUtility;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerUtility;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean.IHandlerVisitor;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageLoadedListener;

public class WebServiceProviderHandlerNodePage extends AbstractPage implements IMarkerRebuildListener {

  public static final int DATA_SUN_JAXWS_ENTRY = 1 << 0;

  private String m_markerGroupUUID;
  private IScoutBundle m_bundle; // necessary to be hold as in method unloadPage, a reference to the bundle is required
  private String m_alias;
  private boolean m_pageUnloaded = false;

  private Object m_pageLoadedListenerLock;
  private SunJaxWsBean m_sunJaxWsBean;
  private IResourceListener m_sunJaxWsResourceListener;
  private Set<IPageLoadedListener> m_pageLoadedListeners;

  public WebServiceProviderHandlerNodePage(IPage parent, SunJaxWsBean sunJaxWsBean) {
    setParent(parent);
    setName(Texts.get("HandlerRegistration"));
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsSdk.Handlers));

    m_sunJaxWsBean = sunJaxWsBean;
    m_alias = sunJaxWsBean.getAlias();
    m_bundle = getScoutResource();
    m_markerGroupUUID = UUID.randomUUID().toString();

    m_pageLoadedListeners = new HashSet<IPageLoadedListener>();
    m_pageLoadedListenerLock = new Object();

    m_sunJaxWsResourceListener = new P_SunJaxWsResourceListener();
    ResourceFactory.getSunJaxWsResource(m_bundle).addResourceListener(getSunJaxWsBean().getAlias(), IResourceListener.EVENT_SUNJAXWS_HANDLER_CHANGED, m_sunJaxWsResourceListener);

    JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(this);
  }

  public void reloadPage(int dataMask) {
    if ((dataMask & DATA_SUN_JAXWS_ENTRY) > 0) {
      ScoutXmlElement xmlRoot = getSunJaxWsResource().loadXml().getRoot();
      ScoutXmlElement sunJaxWsXml = xmlRoot.getChild(StringUtility.join(":", xmlRoot.getNamePrefix(), SunJaxWsBean.XML_ENDPOINT), "name", m_alias);
      m_sunJaxWsBean = new SunJaxWsBean(sunJaxWsXml);
    }

    // notify listeners
    notifyPageLoadedListeners();
    JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(this);
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.PROVIDER_HANDLER_NODE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return false;
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
    ResourceFactory.getSunJaxWsResource(m_bundle).removeResourceListener(m_sunJaxWsResourceListener);
    super.unloadPage();
  }

  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public int getQuality() {
    final IntegerHolder qualityHolder = new IntegerHolder(MarkerUtility.getQuality(this, m_bundle, m_markerGroupUUID));

    if (qualityHolder.getValue() >= IMarker.SEVERITY_ERROR) {
      return IMarker.SEVERITY_ERROR;
    }

    // calculate quality of handlers
    getSunJaxWsBean().visitHandlers(new IHandlerVisitor() {

      @Override
      public boolean visit(ScoutXmlElement xmlHandlerElement, String fullyQualifiedName, int handlerIndex, int handlerCount) {
        IType type = TypeUtility.getType(fullyQualifiedName);
        if (TypeUtility.exists(type)) {
          qualityHolder.setValue(Math.max(qualityHolder.getValue(), ScoutSeverityManager.getInstance().getSeverityOf(type)));
          if (qualityHolder.getValue() >= IMarker.SEVERITY_ERROR) {
            return false;
          }
        }
        return true;
      }
    });

    return qualityHolder.getValue();
  }

  @Override
  public void rebuildMarkers() {
    synchronized (m_markerGroupUUID) {
      try {
        MarkerUtility.clearMarkers(m_bundle, m_markerGroupUUID);

        if (isPageUnloaded()) {
          return;
        }

        MarkerRebuildUtility.rebuildHandlerMarkers(getSunJaxWsBean(), m_bundle, m_markerGroupUUID);
      }
      catch (Exception e) {
        JaxWsSdk.logWarning("failed to update markers", e);
      }
      finally {
        Set<IResource> resources = new HashSet<IResource>();
        resources.add(ResourceFactory.getSunJaxWsResource(m_bundle).getFile());
        ScoutSeverityManager.getInstance().fireSeverityChanged(resources);
      }
    }
  }

  public String getMarkerGroupUUID() {
    return m_markerGroupUUID;
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
  }

  public XmlResource getSunJaxWsResource() {
    return ResourceFactory.getSunJaxWsResource(m_bundle);
  }

  public void addPageLoadedListener(IPageLoadedListener listener) {
    synchronized (m_pageLoadedListenerLock) {
      m_pageLoadedListeners.add(listener);
    }
  }

  public void removePageLoadedListener(IPageLoadedListener listener) {
    synchronized (m_pageLoadedListenerLock) {
      m_pageLoadedListeners.remove(listener);
    }
  }

  private void notifyPageLoadedListeners() {
    IPageLoadedListener[] listeners;
    synchronized (m_pageLoadedListenerLock) {
      listeners = m_pageLoadedListeners.toArray(new IPageLoadedListener[m_pageLoadedListeners.size()]);
    }

    for (IPageLoadedListener listener : listeners) {
      try {
        listener.pageLoaded();
      }
      catch (Exception e) {
        JaxWsSdk.logError("error while notifying pageLoaded listener", e);
      }
    }
  }

  public boolean isPageUnloaded() {
    return m_pageUnloaded;
  }

  private class P_SunJaxWsResourceListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      reloadPage(DATA_SUN_JAXWS_ENTRY);
    }
  }
}
