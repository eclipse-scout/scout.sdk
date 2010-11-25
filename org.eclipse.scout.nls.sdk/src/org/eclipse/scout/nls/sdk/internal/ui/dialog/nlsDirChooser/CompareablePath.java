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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.nlsDirChooser;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class CompareablePath extends AbstractNlsTreeItem {
  private IPath m_path;
  private TreeSet<AbstractNlsTreeItem> m_children = new TreeSet<AbstractNlsTreeItem>();
  private boolean m_loaded;

  public CompareablePath(IPath path) {
    m_path = path;
  }

  public IPath getPath() {
    return m_path;
  }

  @Override
  public Image getImage() {
    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
  }

  @Override
  public boolean isLoaded() {
    return m_loaded;
  }

  @Override
  public boolean hasChildren() {
    return !m_children.isEmpty();
  }

  @Override
  public String getText() {
    return getPath().lastSegment();
  }

  public void setLoaded(boolean loaded) {
    m_loaded = loaded;
  }

  public void addChild(AbstractNlsTreeItem child) {
    m_children.add(child);
  }

  public void removeChild(AbstractNlsTreeItem child) {
    m_children.remove(child);
  }

  public void unload() {
    m_children.clear();
  }

  @Override
  public Set<AbstractNlsTreeItem> getChildren() {
    return m_children;
  }

}
