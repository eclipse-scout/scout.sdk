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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.scout.sdk.jdt.compile.ScoutSeverityManager;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.action.ShowJavaReferencesAction;
import org.eclipse.scout.sdk.ui.action.delete.DeleteAction;
import org.eclipse.scout.sdk.ui.action.rename.TypeRenameAction;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ui.view.outline.pages.ITypePage;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.AbstractTypeChangedListener;
import org.eclipse.scout.sdk.ws.jaxws.util.listener.IPageLoadedListener;

public class HandlerNodePage extends AbstractPage implements ITypePage {

  private IType m_type;

  private boolean m_pageUnloaded = false;
  private Object m_pageLoadedListenerLock;
  private Set<IPageLoadedListener> m_pageLoadedListeners;
  private P_TypeChangeListener m_handlerChangedListener;

  public HandlerNodePage(IPage parent, IType type) {
    setParent(parent);
    setName(type.getElementName());
    m_type = type;
    if (type.isBinary()) {
      setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsSdk.HandlerBinary));
    }
    else {
      setImageDescriptor(JaxWsSdk.getImageDescriptor(JaxWsSdk.Handler));
    }

    m_pageLoadedListeners = new HashSet<IPageLoadedListener>();
    m_pageLoadedListenerLock = new Object();

    m_handlerChangedListener = new P_TypeChangeListener();
    m_handlerChangedListener.setType(m_type);
    ResourcesPlugin.getWorkspace().addResourceChangeListener(m_handlerChangedListener);
  }

  @Override
  public String getPageId() {
    return IJaxWsPageConstants.HANDLER_NODE_PAGE;
  }

  @Override
  public void unloadPage() {
    m_pageUnloaded = true;

    ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_handlerChangedListener);
    super.unloadPage();
  }

  @Override
  public int getQuality() {
    int quality = IMarker.SEVERITY_INFO;
    if (getType().exists()) {
      quality = ScoutSeverityManager.getInstance().getSeverityOf(getType());
    }
    return quality;
  }

  @Override
  public boolean handleDoubleClickedDelegate() {
    if (getType() != null) {
      try {
        JavaUI.openInEditor(getType());
      }
      catch (Exception e) {
        JaxWsSdk.logWarning("could not open type in editor", e);
      }
      return true;
    }
    return false;
  }

  @Override
  public Set<Class<? extends IScoutHandler>> getSupportedMenuActions() {
    if (!m_type.isBinary()) {
      return newSet(DeleteAction.class, TypeRenameAction.class, ShowJavaReferencesAction.class);
    }
    return newSet(ShowJavaReferencesAction.class);
  }

  @Override
  public boolean isFolder() {
    return false;
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

  @Override
  public IType getType() {
    return m_type;
  }

  @Override
  public void setType(IType type) {
    m_type = type;
  }

  public boolean isPageUnloaded() {
    return m_pageUnloaded;
  }

  private class P_TypeChangeListener extends AbstractTypeChangedListener {

    @Override
    protected boolean shouldAnalayseForChange(IResourceChangeEvent event) {
      return !isPageUnloaded();
    }

    @Override
    protected void typeChanged() {
      markStructureDirty();
      notifyPageLoadedListeners();
    }
  }
}
