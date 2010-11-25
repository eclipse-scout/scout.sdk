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

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.nlsDirChooser.AbstractNlsTreeItem;
import org.eclipse.swt.graphics.Image;

public class NlsTranlationFolderChooserDialogModel implements ITreeContentProvider, ILabelProvider {
  private TreeSet<AbstractNlsTreeItem> m_rootFolders;
  private List<IJavaProject> m_projects;

  public NlsTranlationFolderChooserDialogModel(List<IProject> projects) {
    try {
      m_projects = new LinkedList<IJavaProject>();
      for (IProject project : projects) {
        if (project.hasNature(PDE.PLUGIN_NATURE) && project.hasNature(JavaCore.NATURE_ID) && project.isOpen()) {
          m_projects.add(JavaCore.create(project));
        }
      }
      m_rootFolders = new TreeSet<AbstractNlsTreeItem>();
      for (IJavaProject project : m_projects) {
        CompareableProject comp = new CompareableProject(project.getProject());
        m_rootFolders.add(comp);
        load(comp);
      }
    }
    catch (CoreException e) {
      NlsCore.logWarning(e);
    }

  }

  private void load(CompareableProject project) throws CoreException {
    IResource[] resources = project.getProject().members();
    for (IResource resource : resources) {
      AbstractNlsTreeItem item = isRelevant(resource);
      if (item != null) {
        project.addChild(item);
      }
    }
  }

  private List<AbstractNlsTreeItem> getSubfolders(CompareableFolder path) throws CoreException {
    List<AbstractNlsTreeItem> paths = new LinkedList<AbstractNlsTreeItem>();

    if (path.getFolder().exists()) {
      IResource[] resources = path.getFolder().members();
      for (IResource resource : resources) {
        AbstractNlsTreeItem item = isRelevant(resource);
        if (item != null) {
          paths.add(item);
        }
      }
    }
    return paths;
  }

  private AbstractNlsTreeItem isRelevant(IResource resource) throws CoreException {

    if (resource instanceof IFolder) {
      IJavaProject project = JavaCore.create(resource.getProject());
      IFolder folder = (IFolder) resource;
      String outputFolder = project.getOutputLocation().toString().replace(project.getProject().getName(), "").replace("/", "");
      if (outputFolder.equals(folder.getProjectRelativePath().segment(0))) {
        return null;
      }
      if (folder.getProjectRelativePath().segment(0).equals("META-INF")) {
        return null;
      }
      return new CompareableFolder(folder);
    }
    return null;
  }

  private void ensureLoaded(CompareableFolder path) {
    if (!path.isLoaded()) {
      try {
        List<AbstractNlsTreeItem> children = getSubfolders(path);
        for (AbstractNlsTreeItem newPath : children) {
          path.addChild(newPath);
        }
        path.setLoaded(true);
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        NlsCore.logWarning(e);
      }
    }
  }

  public Object[] getChildren(Object parentElement) {

    return ((AbstractNlsTreeItem) parentElement).getChildren().toArray();
  }

  public Object getParent(Object element) {
    return null;
  }

  public boolean hasChildren(Object element) {
    if (element instanceof CompareableProject) {
      return ((CompareableProject) element).hasChildren();
    }
    CompareableFolder path = (CompareableFolder) element;
    ensureLoaded(path);
    return path.getChildren().size() > 0;
  }

  public Object[] getElements(Object inputElement) {

    return m_rootFolders.toArray();
  }

  public void dispose() {
    // TODO Auto-generated method stub

  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // TODO Auto-generated method stub

  }

  public Image getImage(Object element) {
    return ((AbstractNlsTreeItem) element).getImage();
  }

  public String getText(Object element) {
    return ((AbstractNlsTreeItem) element).getText();
  }

  public void addListener(ILabelProviderListener listener) {
    // TODO Auto-generated method stub

  }

  public boolean isLabelProperty(Object element, String property) {
    // TODO Auto-generated method stub
    return false;
  }

  public void removeListener(ILabelProviderListener listener) {
    // TODO Auto-generated method stub

  }

}
