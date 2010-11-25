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
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.extensions.IPageFactory;
import org.eclipse.scout.sdk.ui.internal.extensions.ExplorerPageExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.extensions.ExplorerPageExtensionPoint.ExplorerPageExtension;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.scout.sdk.workspace.IScoutElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Tree Node with editor adapter of type ICfgAdapter
 */
public abstract class AbstractPage implements IPage {

  private String m_name;
  private ArrayList<IPage> m_children;
  private IPage m_parent;
  private boolean m_recursive;
  private boolean m_childrenLoaded;
  private int m_quality;
  private PageDirtyListener m_pageDirtyListener;
  // child filter cached
  private PageFilter m_cachedPageFilter;

  private ImageDescriptor m_imageDesc;
  private String m_textColorId;
  private IPageFilter m_pageFilter;

  public AbstractPage() {
    // m_name = "...";
    if (isFolder()) {
      m_imageDesc = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_FOLDER);
    }
    else {
      m_imageDesc = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_DEFAULT);
    }
    m_pageDirtyListener = new PageDirtyListener(this);
    m_children = new ArrayList<IPage>();
  }

  public void setParent(IPage parent) {
    m_parent = parent;
    if (m_parent != null) {
      m_parent.addChild(this);
    }
  }

  public IPage getParent() {
    return m_parent;
  }

  @Override
  public IScoutElement getScoutResource() {
    if (getParent() != null) return getParent().getScoutResource();
    else return null;
  }

  public ScoutExplorerPart getOutlineView() {
    IPage parent = getParent();
    if (parent != null) return parent.getOutlineView();
    else return null;
  }

  public String getName() {

    return m_name;
  }

  public void setName(String s) {
    m_name = s;
  }

  public boolean isChildrenLoaded() {
    return m_childrenLoaded;
  }

  public boolean isInitiallyLoaded() {
    return false;
  }

  public void markStructureDirty() {
    ScoutExplorerPart o = getOutlineView();
    if (o != null) {
      o.markStructureDirty(this);
    }
  }

  public String getDecoratedName() {
    return getName() + (m_recursive ? (" (R)") : (""));
  }

  public void addChild(IPage child) {
    if (child == null) throw new IllegalArgumentException("adding null child to " + getName());
    m_children.add(child);
  }

  public boolean removeChild(IPage childPage) {
    if (childPage == null) throw new IllegalArgumentException("remove null child to " + getName());
    childPage.setParent(null);
    return m_children.remove(childPage);
  }

  public IPage[] getChildArray() {
    return getChildArray(null);
  }

  @Override
  public IPage[] getChildArray(IPageFilter filter) {
    ArrayList<IPage> children = new ArrayList<IPage>();
    for (IPage p : m_children) {
      if (filter == null || filter.accept(p)) {
        children.add(p);
      }
    }
    return children.toArray(new IPage[children.size()]);
  }

  @Override
  public List<IPage> getChildren() {
    return Collections.unmodifiableList(m_children);
  }

  @Override
  public boolean hasChildren() {
    return m_children.size() > 0;
  }

  @Override
  public void unloadPage() {
  }

  public final void unloadChildren() {
    for (IPage page : m_children) {
      page.setParent(null);
      page.unloadChildren();
      page.unloadPage();
    }
    m_children.clear();
    m_childrenLoaded = false;
  }

  @Override
  public void refresh(boolean clearCache) {
    getOutlineView().markStructureDirty(this);
  }

  @Override
  public String toString() {
    return getDecoratedName();
  }

  public final void loadChildren() {
    loadChildrenImpl();
    // extension point
    ExplorerPageExtension[] extensions = ExplorerPageExtensionPoint.getExtensions(this);
    if (extensions != null) {
      for (ExplorerPageExtension ext : extensions) {
        if (ext.getFactoryClass() != null) {
          IPageFactory factory = ext.createFactoryClass();
          factory.createChildren(this);
        }
        else if (ext.getPageClass() != null) {
          IPage childPage = ext.createPageInstance();
          childPage.setParent(this);
        }
      }
    }
    // call extensions to contribute their children
//    for (IScoutSdkExtension ext : ScoutExtensionsExtensionPoint.getExtensions()) {
//      try {
//        ext.contributePageChildren(this);
//      }
//      catch (Throwable t) {
//        ScoutSdkUi.logWarning("contribution from " + ext.getClass().getSimpleName(), t);
//      }
//    }
    m_childrenLoaded = true;
  }

  protected void loadChildrenImpl() {
  }

  /**
   * called when filter has changed
   */
  public void refreshFilteredChildren() {
    ScoutExplorerPart o = getOutlineView();
    if (o != null) {
      o.markFilterChanged(this);
    }
  }

  public final PageDirtyListener getPageDirtyListener() {
    return m_pageDirtyListener;
  }

  public void fillContextMenu(IMenuManager manager) {
    Action na = createNewAction();
    if (na != null) {
      manager.add(na);
    }
    manager.add(new Separator());
    Action ea = createEditAction();
    if (ea != null) {
      manager.add(ea);
    }
    Action ra = createRenameAction();
    if (ra != null) {
      manager.add(ra);
    }
    manager.add(new Separator());
    Action da = createDeleteAction();
    if (da != null) {
      manager.add(da);
    }
  }

  public void setImageDescriptor(ImageDescriptor desc) {
    m_imageDesc = desc;
  }

  public ImageDescriptor getBaseImageDescriptor() {
    return m_imageDesc;
  }

  public final Image getImage() {
    ImageDescriptor baseDesc = getBaseImageDescriptor();
    int flags = 0;
    Point size = new Point(16, 16);
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
    JavaElementImageDescriptor desc = new JavaElementImageDescriptor(baseDesc, flags, size);
    return ScoutSdkUi.getImage(desc);
  }

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

  public Action createNewAction() {
    return null;
  }

  public Action createRenameAction() {
    return null;
  }

  public Action createDeleteAction() {
    return null;
  }

  public Action createEditAction() {
    return null;
  }

  public Action createMoveAction(int moveOperation) {
    return null;
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

  public boolean isFolder() {
    return false;
  }

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

  private int visitChildren(INodeVisitor visitor) {
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

}
