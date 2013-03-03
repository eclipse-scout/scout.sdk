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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.jws.WebService;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.marker.IMarkerRebuildListener;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerRebuildUtility;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerUtility;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.WsProviderCodeFirstDeleteAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.part.AnnotationProperty;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.AbstractTypeChangedListener;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageLoadedListener;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageReloadNotification;

public class WebServiceProviderCodeFirstNodePage extends AbstractPage implements IMarkerRebuildListener, IPageReloadNotification {

  public static final int DATA_ENDPOINT_TYPE = 1 << 0;
  public static final int DATA_ENDPOINT_INTERFACE_TYPE = 1 << 1;
  public static final int DATA_SUN_JAXWS_ENTRY = 1 << 2;

  private boolean m_pageUnloaded = false;
  private String m_markerGroupUUID;
  private IScoutBundle m_bundle; // necessary to be hold as in method unloadPage, a reference to the bundle is required
  private String m_alias;

  private Object m_pageLoadedListenerLock;
  private IType m_portType;

  private SunJaxWsBean m_sunJaxWsBean;

  private IResourceListener m_sunJaxWsResourceListener;
  private P_EndpointTypeChangeListener m_endpointTypeChangeListener;
  private P_EndpointInterfaceTypeChangeListener m_endpointInterfaceTypeChangeListener;
  private Set<IPageLoadedListener> m_pageLoadedListeners;

  public WebServiceProviderCodeFirstNodePage(IPage parent, SunJaxWsBean sunJaxWsBean) {
    setParent(parent);
    setName(StringUtility.nvl(sunJaxWsBean.getAlias(), "?"));
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsSdk.WebServiceProviderCodeFirst));

    m_sunJaxWsBean = sunJaxWsBean;
    m_alias = sunJaxWsBean.getAlias();
    m_bundle = getScoutBundle();
    m_markerGroupUUID = UUID.randomUUID().toString();

    m_sunJaxWsResourceListener = new P_SunJaxWsResourceListener();
    m_pageLoadedListeners = new HashSet<IPageLoadedListener>();
    m_pageLoadedListenerLock = new Object();

    // register for events being of interest
    getSunJaxWsResource().addResourceListener(sunJaxWsBean.getAlias(), m_sunJaxWsResourceListener);

    m_endpointTypeChangeListener = new P_EndpointTypeChangeListener();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_endpointTypeChangeListener);

    m_endpointInterfaceTypeChangeListener = new P_EndpointInterfaceTypeChangeListener();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_endpointInterfaceTypeChangeListener);

    // register page to receive page reload notification
    JaxWsSdk.getDefault().registerPage(WebServiceProviderNodePage.class, this);

    reloadPage(DATA_ENDPOINT_TYPE | DATA_ENDPOINT_INTERFACE_TYPE);
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.PROVIDER_CODE_FIRST_NODE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    if (menu instanceof WsProviderCodeFirstDeleteAction) {
      ((WsProviderCodeFirstDeleteAction) menu).init(m_bundle, getSunJaxWsBean());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    List<Class<? extends AbstractScoutHandler>> actions = new ArrayList<Class<? extends AbstractScoutHandler>>();
    actions.add(WsProviderCodeFirstDeleteAction.class);
    return actions.toArray(new Class[actions.size()]);
  }

  @Override
  public void reloadPage(int dataMask) {
    if (isPageUnloaded()) {
      return;
    }

    if ((dataMask & DATA_SUN_JAXWS_ENTRY) > 0) {
      SunJaxWsBean sunJaxWsBean = getSunJaxWsBean();
      if (sunJaxWsBean == null) {
        m_sunJaxWsBean = SunJaxWsBean.load(m_bundle, m_alias);
      }
      else {
        if (!sunJaxWsBean.reload(m_bundle)) {
          m_sunJaxWsBean = null;
        }
      }
    }

    // endpoint (port type)
    if ((dataMask & DATA_ENDPOINT_TYPE) > 0) {
      m_portType = null;
      if (m_sunJaxWsBean != null && m_sunJaxWsBean.getImplementation() != null && TypeUtility.existsType(m_sunJaxWsBean.getImplementation())) {
        m_portType = TypeUtility.getType(m_sunJaxWsBean.getImplementation());
      }
      m_endpointTypeChangeListener.setType(m_portType);
    }

    // endpoint interface
    if ((dataMask & DATA_ENDPOINT_INTERFACE_TYPE) > 0) {
      IAnnotation wsAnnotation = JaxWsSdkUtility.getAnnotation(m_portType, WebService.class.getName(), false);
      AnnotationProperty endpointProperty = JaxWsSdkUtility.parseAnnotationTypeValue(m_portType, wsAnnotation, "endpointInterface");
      if (endpointProperty.isDefined()) {
        IType portTypeInterfaceType = TypeUtility.getType(endpointProperty.getFullyQualifiedName());
        m_endpointInterfaceTypeChangeListener.setType(portTypeInterfaceType);
      }
    }

    // notify listeners
    notifyPageLoadedListeners();
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
  public void unloadPage() {
    m_pageUnloaded = true;

    MarkerUtility.clearMarkers(m_bundle, m_markerGroupUUID);
    getSunJaxWsResource().removeResourceListener(m_sunJaxWsResourceListener);
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_endpointTypeChangeListener);
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_endpointInterfaceTypeChangeListener);

    // unregister page to not receive page reload notification anymore
    JaxWsSdk.getDefault().unregisterPage(WebServiceProviderNodePage.class, this);

    super.unloadPage();
  }

  @Override
  public int getQuality() {
    int quality = MarkerUtility.getQuality(this, m_bundle, m_markerGroupUUID);

    if (quality == IMarker.SEVERITY_ERROR) {
      return quality;
    }

    IType portType = getPortType();
    if (portType != null) {
      quality = Math.max(quality, ScoutSeverityManager.getInstance().getSeverityOf(portType));
    }
    return quality;
  }

  @Override
  public boolean handleDoubleClickedDelegate() {
    if (getPortType() != null) {
      try {
        JavaUI.openInEditor(getPortType());
      }
      catch (Exception e) {
        JaxWsSdk.logWarning("could not open type in editor", e);
      }
      return true;
    }
    return false;
  }

  @Override
  protected void loadChildrenImpl() {
    // handler page
    new WebServiceProviderHandlerNodePage(this, getSunJaxWsBean());
  }

  @Override
  public void rebuildMarkers() {
    synchronized (m_markerGroupUUID) {
      try {
        MarkerUtility.clearMarkers(m_bundle, m_markerGroupUUID);

        if (isPageUnloaded()) {
          return;
        }

        MarkerRebuildUtility.rebuildCodeFirstPortTypeMarkers(m_bundle, getPortType(), getSunJaxWsBean(), getMarkerGroupUUID());
      }
      catch (Exception e) {
        JaxWsSdk.logWarning("failed to update markers", e);
      }
      finally {
        Set<IResource> resources = new HashSet<IResource>();
        if (JaxWsSdkUtility.exists(getSunJaxWsResource().getFile())) {
          resources.add(getSunJaxWsResource().getFile());
        }
        if (TypeUtility.exists(getPortType())) {
          resources.add(getPortType().getResource());
        }
        ScoutSeverityManager.getInstance().fireSeverityChanged(resources);
      }
    }
  }

  @Override
  public WebServiceProviderTablePage getParent() {
    return (WebServiceProviderTablePage) super.getParent();
  }

  @Override
  public String getMarkerGroupUUID() {
    return m_markerGroupUUID;
  }

  public IType getPortType() {
    return m_portType;
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
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

  public XmlResource getSunJaxWsResource() {
    return ResourceFactory.getSunJaxWsResource(m_bundle);
  }

  public boolean isPageUnloaded() {
    return m_pageUnloaded;
  }

  private class P_SunJaxWsResourceListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      if (event == IResourceListener.EVENT_SUNJAXWS_WSDL_CHANGED || event == EVENT_SUNJAXWS_HANDLER_CHANGED || event == EVENT_SUNJAXWS_URL_PATTERN_CHANGED) {
        reloadPage(DATA_SUN_JAXWS_ENTRY);
      }
      else {
        JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(WebServiceProviderCodeFirstNodePage.this);
      }
    }
  }

  private class P_EndpointTypeChangeListener extends AbstractTypeChangedListener {

    @Override
    protected boolean shouldAnalayseForChange(IResourceChangeEvent event) {
      return !isPageUnloaded();
    }

    @Override
    protected void typeChanged() {
      reloadPage(DATA_ENDPOINT_TYPE);
    }
  }

  private class P_EndpointInterfaceTypeChangeListener extends AbstractTypeChangedListener {

    @Override
    protected boolean shouldAnalayseForChange(IResourceChangeEvent event) {
      return !isPageUnloaded();
    }

    @Override
    protected void typeChanged() {
      reloadPage(DATA_ENDPOINT_INTERFACE_TYPE);
    }
  }
}
