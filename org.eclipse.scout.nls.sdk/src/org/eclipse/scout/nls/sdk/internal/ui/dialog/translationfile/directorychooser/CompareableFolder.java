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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.translationfile.directorychooser;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFolder;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.nlsDirChooser.AbstractNlsTreeItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class CompareableFolder extends AbstractNlsTreeItem {
  private IFolder m_folder;
  private TreeSet<AbstractNlsTreeItem> m_children = new TreeSet<AbstractNlsTreeItem>();
  private boolean m_loaded;

  public CompareableFolder(IFolder folder) {
    m_folder = folder;
  }

  public IFolder getFolder() {
    return m_folder;
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
    return getFolder().getName();
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
