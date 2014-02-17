/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.view.outline.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.ui.action.IScoutHandler;
import org.eclipse.scout.sdk.ui.extensions.IPageFactory;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.ExplorerPageExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.extensions.ExplorerPageExtensionPoint.ExplorerPageExtension;
import org.eclipse.scout.sdk.ui.menu.IContextMenuProvider;
import org.eclipse.scout.sdk.ui.view.outline.IPageOutlineView;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Tree Node with editor adapter of type ICfgAdapter
 */
public abstract class AbstractPage implements IPage, IContextMenuProvider {

  private final static Point ICON_SIZE = new Point(16, 16);

  private final Map<CompositeObject, IPage> m_children;
  private final PageDirtyListener m_pageDirtyListener;

  private String m_name;
  private IPage m_parent;
  private boolean m_recursive;
  private boolean m_childrenLoaded;
  private ImageDescriptor m_imageDesc;

  public AbstractPage() {
    m_pageDirtyListener = new PageDirtyListener(this);
    m_children = new TreeMap<CompositeObject, IPage>();
  }

  @Override
  public int getOrder() {
    return -1;
  }

  @Override
  public void setParent(IPage parent) {
    m_parent = parent;
    if (m_parent != null) {
      m_parent.addChild(this);
    }
  }

  @Override
  public IPage getParent() {
    return m_parent;
  }

  @Override
  public IScoutBundle getScoutBundle() {
    if (getParent() != null) return getParent().getScoutBundle();
    else return null;
  }

  @Override
  public IPageOutlineView getOutlineView() {
    IPage parent = getParent();
    if (parent != null) return parent.getOutlineView();
    else return null;
  }

  @Override
  public String getName() {
    return m_name;
  }

  public void setName(String s) {
    m_name = s;
  }

  @Override
  public boolean isChildrenLoaded() {
    synchronized (m_children) {
      return m_childrenLoaded;
    }
  }

  @Override
  public boolean isInitiallyLoaded() {
    return false;
  }

  @Override
  public void markStructureDirty() {
    IPageOutlineView o = getOutlineView();
    if (o != null) {
      o.markStructureDirty(this);
    }
  }

  public String getDecoratedName() {
    return getName() + (m_recursive ? (" (R)") : (""));
  }

  @Override
  public void addChild(IPage child) {
    if (child == null) throw new IllegalArgumentException("adding null child to " + getName());
    synchronized (m_children) {
      m_children.put(new CompositeObject(child.getOrder(), m_children.size(), child), child);
    }
  }

  @Override
  public boolean removeChild(IPage childPage) {
    if (childPage == null) throw new IllegalArgumentException("remove null child to " + getName());
    childPage.setParent(null);
    synchronized (m_children) {
      for (Iterator<Entry<CompositeObject, IPage>> it = m_children.entrySet().iterator(); it.hasNext();) {
        Entry<CompositeObject, IPage> entry = it.next();
        if (childPage.equals(entry.getValue())) {
          it.remove();
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public IPage[] getChildArray() {
    return getChildArray(null);
  }

  @Override
  public IPage[] getChildArray(IPageFilter filter) {
    ArrayList<IPage> children = new ArrayList<IPage>();
    synchronized (m_children) {
      for (IPage p : m_children.values()) {
        if (filter == null || filter.accept(p)) {
          children.add(p);
        }
      }
    }
    return children.toArray(new IPage[children.size()]);
  }

  @Override
  public List<IPage> getChildren() {
    synchronized (m_children) {
      return Collections.unmodifiableList(new ArrayList<IPage>(m_children.values()));
    }
  }

  @Override
  public boolean hasChildren() {
    synchronized (m_children) {
      return m_children.size() > 0;
    }
  }

  @Override
  public void unloadPage() {
  }

  @Override
  public final void unloadChildren() {
    synchronized (m_children) {
      for (IPage page : m_children.values()) {
        page.setParent(null);
        page.unloadChildren();
        page.unloadPage();
      }
      m_children.clear();
      m_childrenLoaded = false;
    }
  }

  @Override
  public void refresh(boolean clearCache) {
    IPageOutlineView outlineView = getOutlineView();
    if (outlineView != null) {
      outlineView.markStructureDirty(this);
    }
  }

  @Override
  public String toString() {
    return getDecoratedName();
  }

  @Override
  public final void loadChildren() {
    synchronized (m_children) {
      if (!isChildrenLoaded()) {
        try {
          loadChildrenImpl();
          // extension point
          ExplorerPageExtension[] extensions = ExplorerPageExtensionPoint.getExtensions(this);
          if (extensions != null) {
            for (ExplorerPageExtension ext : extensions) {
              try {
                if (ext.getFactoryClass() != null) {
                  IPageFactory factory = ext.createFactoryClass();
                  factory.createChildren(this);
                }
                else if (ext.getPageClass() != null) {
                  IPage childPage = ext.createPageInstance();
                  if (childPage != null) {
                    childPage.setParent(this);
                  }
                }
              }
              catch (Exception t) {
                ScoutSdkUi.logError("could not load extension '" + ext.getPageClass() + "'!", t);
              }
            }
          }
        }
        finally {
          // crucial to mark children as loaded to prevent an infinite loop
          m_childrenLoaded = true;
        }
      }
    }
  }

  protected void loadChildrenImpl() {
  }

  /**
   * called when filter has changed
   */
  public void refreshFilteredChildren() {
    IPageOutlineView o = getOutlineView();
    if (o != null) {
      o.markFilterChanged(this);
    }
  }

  public final PageDirtyListener getPageDirtyListener() {
    return m_pageDirtyListener;
  }

  @Override
  public void setImageDescriptor(ImageDescriptor desc) {
    m_imageDesc = desc;
  }

  public ImageDescriptor getBaseImageDescriptor() {
    if (m_imageDesc == null) {
      if (isFolder()) {
        m_imageDesc = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FolderOpen);
      }
      else {
        m_imageDesc = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Default);
      }
    }
    return m_imageDesc;
  }

  public final Image getImage() {
    ImageDescriptor baseDesc = getBaseImageDescriptor();
    int flags = 0;

    int quality = getQuality();
    switch (quality) {
      case IMarker.SEVERITY_ERROR: {
        flags = flags | JavaElementImageDescriptor.ERROR;
        break;
      }
      case IMarker.SEVERITY_WARNING: {
        flags = flags | JavaElementImageDescriptor.WARNING;
        break;
      }
    }
    JavaElementImageDescriptor desc = new JavaElementImageDescriptor(baseDesc, flags, ICON_SIZE);
    return ScoutSdkUi.getImage(desc);
  }

  @Override
  public int getQuality() {
    if (hasChildren()) {
      IPage[] a = getChildArray();
      int q = IMarker.SEVERITY_INFO;
      for (int i = 0; i < a.length; i++) {
        q = Math.max(q, a[i].getQuality());
        if (q == IMarker.SEVERITY_ERROR) {
          break;
        }
      }
      return q;
    }
    else {
      return IMarker.SEVERITY_INFO;
    }
  }

  /**
   * to handle the node selection
   */
  public void handleSelectionDelegate() {
  }

  /**
   * to handle the node double click
   * 
   * @return consumed if the expansion should be prevented
   */
  public boolean handleDoubleClickedDelegate() {
    return false;
  }

  @Override
  public boolean isFolder() {
    return false;
  }

  @Override
  public int accept(INodeVisitor visitor) {
    switch (visitor.visit(this)) {
      case INodeVisitor.CANCEL:
        return INodeVisitor.CANCEL;
      case INodeVisitor.CANCEL_SUBTREE:
        return INodeVisitor.CONTINUE;
      case INodeVisitor.CONTINUE_BRANCH:
        visitChildren(visitor);
        return INodeVisitor.CANCEL;
      default:
        return visitChildren(visitor);
    }
  }

  protected int visitChildren(INodeVisitor visitor) {
    if (!isChildrenLoaded()) {
      loadChildren();
    }
    for (IPage childPage : getChildren()) {
      switch (childPage.accept(visitor)) {
        case INodeVisitor.CANCEL:
          return INodeVisitor.CANCEL;
      }
    }
    return INodeVisitor.CONTINUE;
  }

  @Override
  public Class<? extends IScoutHandler>[] getSupportedMenuActions() {
    return null;
  }

  @Override
  public void prepareMenuAction(IScoutHandler menu) {
  }

  /**
   * @param debugActions
   */
  public void addDebugMenus(List<Action> debugActions) {
  }
}
