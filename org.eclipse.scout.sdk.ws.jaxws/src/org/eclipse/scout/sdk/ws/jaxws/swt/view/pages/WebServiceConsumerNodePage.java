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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractScoutTypePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.ScoutSeverityManager;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants.MarkerType;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.marker.IMarkerRebuildListener;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerRebuildUtility;
import org.eclipse.scout.sdk.ws.jaxws.marker.MarkerUtility;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ManagedResource;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.resource.WsdlResource;
import org.eclipse.scout.sdk.ws.jaxws.resource.XmlResource;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.RefreshAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.StubRebuildAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.action.WsConsumerDeleteAction;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.WebserviceEnum;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.AbstractTypeChangedListener;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageLoadedListener;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageReloadNotification;

public class WebServiceConsumerNodePage extends AbstractScoutTypePage implements IMarkerRebuildListener, IPageReloadNotification {

  public static final int DATA_BUILD_JAXWS_ENTRY = 1 << 0;
  public static final int DATA_WSDL_FILE = 1 << 1;
  public static final int DATA_BINDING_FILE = 1 << 2;
  public static final int DATA_STUB_FILES = 1 << 3;
  public static final int DATA_JDT_TYPE = 1 << 4;

  private Object m_pageLoadedListenerLock;
  private boolean m_pageUnloaded = false;
  private String m_markerGroupUUID;
  private IScoutBundle m_bundle; // necessary to be hold as in method unloadPage, a reference to the bundle is required
  private BuildJaxWsBean m_buildJaxWsBean;

  private IResourceListener m_buildJaxWsResourceListener;
  private IResourceListener m_wsdlResourceListener;
  private IResourceListener m_bindingFileResourceListener;
  private Set<IPageLoadedListener> m_pageLoadedListeners;
  private IResourceListener m_manifestResourceListener;
  private IResourceListener m_stubJarResourceListener;
  private P_TypeChangeListener m_typeChangedListener;

  private WsdlResource m_wsdlResource;
  private Definition m_wsdlDefinition;
  private XmlResource[] m_bindingFileResources;
  private ManagedResource m_manifestResource;
  private ManagedResource m_stubJarResource;

  public WebServiceConsumerNodePage(IPage parent, IType type) {
    setParent(parent);
    setType(type);
    setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsSdk.WebserviceConsumer));

    m_bundle = getScoutResource();
    m_markerGroupUUID = UUID.randomUUID().toString();

    m_wsdlResource = new WsdlResource(m_bundle);
    m_wsdlResourceListener = new P_WsdlResourceListener();

    m_manifestResource = new ManagedResource(m_bundle.getProject());
    m_manifestResource.setFile(new PluginModelHelper(m_bundle.getProject()).Manifest.getFile());
    m_manifestResourceListener = new P_ManifestResourceListener();

    m_stubJarResource = new ManagedResource(m_bundle.getProject());
    m_stubJarResourceListener = new P_StubJarResourceListener();

    m_buildJaxWsResourceListener = new P_BuildJaxWsListener();

    m_bindingFileResources = new XmlResource[0];
    m_bindingFileResourceListener = new P_BindingFileResourceListener();

    m_pageLoadedListeners = new HashSet<IPageLoadedListener>();
    m_pageLoadedListenerLock = new Object();

    // register for events being of interest
    getBuildJaxWsResource().addResourceListener(getType().getElementName(), m_buildJaxWsResourceListener);
    getWsdlResource().addResourceListener(IResourceListener.EVENT_WSDL_REPLACED | IResourceListener.EVENT_UNKNOWN | IResourceListener.EVENT_STUB_REBUILT, m_wsdlResourceListener);
    m_manifestResource.addResourceListener(IResourceListener.EVENT_MANIFEST_CLASSPATH | IResourceListener.EVENT_UNKNOWN, m_manifestResourceListener);
    m_stubJarResource.addResourceListener(m_stubJarResourceListener);

    // register page to receive page reload notification
    JaxWsSdk.getDefault().registerPage(WebServiceConsumerNodePage.class, this);

    m_typeChangedListener = new P_TypeChangeListener();
    m_typeChangedListener.setType(getType());
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_typeChangedListener);

    reloadPage(DATA_BINDING_FILE | DATA_BUILD_JAXWS_ENTRY | DATA_WSDL_FILE | DATA_STUB_FILES);
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.CONSUMER_NODE_PAGE;
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    List<Class<? extends IScoutHandler>> list = new ArrayList<Class<? extends IScoutHandler>>();
    for (Class<? extends IScoutHandler> c : super.getSupportedMenuActions()) {
      list.add(c);
    }

    if (getBuildJaxWsBean() != null &&
        !MarkerUtility.containsMarker(m_bundle, MarkerType.StubFolder, getMarkerGroupUUID(), IMarker.SEVERITY_ERROR) &&
        !MarkerUtility.containsMarker(m_bundle, MarkerType.Wsdl, getMarkerGroupUUID(), IMarker.SEVERITY_ERROR)) {
      list.add(StubRebuildAction.class);
    }
    list.add(RefreshAction.class);
    list.add(WsConsumerDeleteAction.class);
    return list.toArray(new Class[list.size()]);
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
    super.prepareMenuAction(menu);
    if (menu instanceof WsConsumerDeleteAction) {
      ((WsConsumerDeleteAction) menu).init(m_bundle, getType(), getBuildJaxWsBean());
    }
    else if (menu instanceof StubRebuildAction) {
      ((StubRebuildAction) menu).init(m_bundle, getBuildJaxWsBean(), getWsdlResource(), m_markerGroupUUID, WebserviceEnum.Consumer);
    }
  }

  @Override
  public void reloadPage(int dataMask) {
    try {
      if ((dataMask & DATA_BUILD_JAXWS_ENTRY) > 0) {
        if (getBuildJaxWsBean() == null) {
          m_buildJaxWsBean = BuildJaxWsBean.load(m_bundle, getType().getElementName(), WebserviceEnum.Consumer);
        }
        else {
          m_buildJaxWsBean.reload(m_bundle);
        }
        if (m_buildJaxWsBean != null) {
          m_stubJarResource.setFile(JaxWsSdkUtility.getStubJarFile(m_bundle, m_buildJaxWsBean, m_buildJaxWsBean.getWsdl()));
        }
      }
      if ((dataMask & DATA_WSDL_FILE) > 0) {
        m_wsdlDefinition = loadWsdlDefinition();
      }
      if ((dataMask & DATA_BINDING_FILE) > 0) {
        m_bindingFileResources = loadBindingFiles();
      }
      if ((dataMask & DATA_STUB_FILES) > 0) {
        m_stubJarResource.setFile(JaxWsSdkUtility.getStubJarFile(m_bundle, m_buildJaxWsBean, m_buildJaxWsBean.getWsdl()));
      }
    }
    catch (Exception e) {
      JaxWsSdk.logError(e);
    }

    // notify listeners
    notifyPageLoadedListeners();
    JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(this);
  }

  @Override
  public void refresh(boolean clearCache) {
    if (clearCache) {
      reloadPage(DATA_BINDING_FILE | DATA_BUILD_JAXWS_ENTRY | DATA_WSDL_FILE);
    }
    else {
      JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(this);
    }
    super.refresh(clearCache);
  }

  @Override
  public void unloadPage() {
    m_pageUnloaded = true;

    MarkerUtility.clearMarkers(m_bundle, m_markerGroupUUID);
    getBuildJaxWsResource().removeResourceListener(m_buildJaxWsResourceListener);
    getWsdlResource().removeResourceListener(m_wsdlResourceListener);
    m_manifestResource.removeResourceListener(m_manifestResourceListener);
    m_stubJarResource.removeResourceListener(m_stubJarResourceListener);

    for (XmlResource resource : m_bindingFileResources) {
      resource.removeResourceListener(m_bindingFileResourceListener);
    }

    // unregister page to not receive page reload notification anymore
    JaxWsSdk.getDefault().unregisterPage(WebServiceConsumerNodePage.class, this);

    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_typeChangedListener);

    super.unloadPage();
  }

  @Override
  public IScoutBundle getScoutResource() {
    return (IScoutBundle) super.getScoutResource();
  }

  @Override
  public int getQuality() {
    int quality = MarkerUtility.getQuality(this, m_bundle, m_markerGroupUUID);

    if (quality == IMarker.SEVERITY_ERROR) {
      return quality;
    }

    if (TypeUtility.exists(getType())) {
      quality = Math.max(quality, ScoutSeverityManager.getInstance().getSeverityOf(getType()));
    }
    return quality;
  }

  @Override
  public void rebuildMarkers() {
    synchronized (m_markerGroupUUID) {
      try {
        MarkerUtility.clearMarkers(m_bundle, m_markerGroupUUID);

        if (isPageUnloaded() || !TypeUtility.exists(getType())) {
          return;
        }
        if (!MarkerRebuildUtility.rebuildBuildJaxWsMarkers(getBuildJaxWsResource().getFile(), m_buildJaxWsBean, getType().getElementName(), m_wsdlResource, m_markerGroupUUID, m_bundle, WebserviceEnum.Consumer)) {
          return;
        }
        if (!MarkerRebuildUtility.rebuildBindingFileMarkers(getBuildJaxWsResource().getFile(), m_bindingFileResources, m_wsdlResource, m_markerGroupUUID, m_bundle)) {
          return;
        }
        if (!MarkerRebuildUtility.rebuildWebserviceClientType(getType(), m_buildJaxWsBean, m_wsdlResource, m_markerGroupUUID, m_bundle)) {
          return;
        }
        if (!MarkerRebuildUtility.rebuildWsdlMarkers(m_wsdlResource, m_buildJaxWsBean, null, m_markerGroupUUID, m_bundle)) {
          return;
        }

        QName portTypeQName = JaxWsSdkUtility.extractPortTypeQNameFromWsClient(getType());
        QName serviceQName = JaxWsSdkUtility.extractServiceQNameFromWsClient(getType());
        MarkerRebuildUtility.rebuildStubJarFileMarkers(m_buildJaxWsBean, m_wsdlResource, portTypeQName, serviceQName, m_bundle, m_markerGroupUUID);
      }
      catch (Exception e) {
        JaxWsSdk.logWarning("failed to update markers", e);
      }
      finally {
        Set<IResource> resources = new HashSet<IResource>();
        if (JaxWsSdkUtility.exists(getBuildJaxWsResource().getFile())) {
          resources.add(getBuildJaxWsResource().getFile());
        }
        if (JaxWsSdkUtility.exists(getWsdlResource().getFile())) {
          resources.add(getWsdlResource().getFile());
        }
        for (XmlResource resource : m_bindingFileResources) {
          if (JaxWsSdkUtility.exists(resource.getFile())) {
            resources.add(resource.getFile());
          }
        }
        ScoutSeverityManager.getInstance().fireSeverityChanged(resources);
      }
    }
  }

  private Definition loadWsdlDefinition() {
    if (m_buildJaxWsBean == null) {
      return null;
    }
    IFile file = JaxWsSdkUtility.getFile(m_bundle, m_buildJaxWsBean.getWsdl(), false);
    if (!getWsdlResource().isSameFile(file)) {
      getWsdlResource().setFile(file);
    }
    return getWsdlResource().loadWsdlDefinition();
  }

  private XmlResource[] loadBindingFiles() {
    for (XmlResource resource : m_bindingFileResources) {
      resource.removeResourceListener(m_bindingFileResourceListener);
    }

    List<XmlResource> bindingFileResources = new LinkedList<XmlResource>();

    if (m_buildJaxWsBean != null) {
      IFile[] bindingFiles = JaxWsSdkUtility.getBindingFiles(m_bundle, m_buildJaxWsBean.getPropertiers());
      for (IFile bindingFile : bindingFiles) {
        XmlResource xmlResource = new XmlResource(m_bundle);
        xmlResource.setFile(bindingFile);
        xmlResource.addResourceListener(m_bindingFileResourceListener);
        bindingFileResources.add(xmlResource);
      }
    }

    return bindingFileResources.toArray(new XmlResource[bindingFileResources.size()]);
  }

  @Override
  public WebServiceConsumerTablePage getParent() {
    return (WebServiceConsumerTablePage) super.getParent();
  }

  @Override
  public String getMarkerGroupUUID() {
    return m_markerGroupUUID;
  }

  public BuildJaxWsBean getBuildJaxWsBean() {
    return m_buildJaxWsBean;
  }

  public Definition getWsdlDefinition() {
    if (m_wsdlDefinition == null) {
      m_wsdlDefinition = loadWsdlDefinition();
    }
    return m_wsdlDefinition;
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

  public XmlResource getBuildJaxWsResource() {
    return ResourceFactory.getBuildJaxWsResource(m_bundle);
  }

  public WsdlResource getWsdlResource() {
    return m_wsdlResource;
  }

  public XmlResource[] getBindingFileResources() {
    return m_bindingFileResources;
  }

  public boolean isPageUnloaded() {
    return m_pageUnloaded;
  }

  private class P_BuildJaxWsListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      if (event == IResourceListener.EVENT_BUILDJAXWS_PROPERTIES_CHANGED ||
          event == IResourceListener.EVENT_SUNJAXWS_WSDL_CHANGED) {
        reloadPage(DATA_BUILD_JAXWS_ENTRY | DATA_WSDL_FILE);
      }
      else {
        JaxWsSdk.getDefault().getMarkerQueueManager().queueRequest(WebServiceConsumerNodePage.this);
      }
    }
  }

  private class P_WsdlResourceListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      reloadPage(DATA_WSDL_FILE);
    }
  }

  private class P_BindingFileResourceListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      reloadPage(DATA_BINDING_FILE);
    }
  }

  private class P_ManifestResourceListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      reloadPage(DATA_STUB_FILES); // Bundle-ClassPath
    }
  }

  private class P_StubJarResourceListener implements IResourceListener {

    @Override
    public void changed(String element, int event) {
      reloadPage(DATA_STUB_FILES);
    }
  }

  private class P_TypeChangeListener extends AbstractTypeChangedListener {

    @Override
    protected boolean shouldAnalayseForChange(IResourceChangeEvent event) {
      return !isPageUnloaded();
    }

    @Override
    protected void typeChanged() {
      reloadPage(DATA_JDT_TYPE);
    }
  }
}
