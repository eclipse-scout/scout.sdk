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

import org.eclipse.core.resources.IProject;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.nlsDirChooser.AbstractNlsTreeItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class CompareableProject extends AbstractNlsTreeItem {
  private TreeSet<AbstractNlsTreeItem> m_children;
  private IProject m_project;

  public CompareableProject(IProject project) {
    m_project = project;
    m_children = new TreeSet<AbstractNlsTreeItem>();
  }

  public void addChild(AbstractNlsTreeItem item) {
    m_children.add(item);
  }

  @Override
  public Set<AbstractNlsTreeItem> getChildren() {
    return m_children;
  }

  @Override
  public Image getImage() {
    return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
  }

  @Override
  public String getText() {
    return m_project.getName();
  }

  @Override
  public boolean hasChildren() {
    return !m_children.isEmpty();
  }

  @Override
  public boolean isLoaded() {
    return true;
  }

  public IProject getProject() {
    return m_project;
  }
}
