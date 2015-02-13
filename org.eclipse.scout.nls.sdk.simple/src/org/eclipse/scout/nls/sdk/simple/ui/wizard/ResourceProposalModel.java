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
package org.eclipse.scout.nls.sdk.simple.ui.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.sdk.util.resources.IResourceFilter;
import org.eclipse.scout.sdk.util.resources.ResourceProxy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ResourceProposalModel extends LabelProvider implements IContentProposalProvider {

  private final Set<String> m_proposalSorting;
  private final Map<String, P_ResourceProposal> m_proposals;
  private IProject[] m_projects;
  private IResourceFilter m_filter;

  public ResourceProposalModel() {
    m_proposalSorting = new TreeSet<>();
    m_proposals = new HashMap<>();
    m_filter = null;
  }

  public void setProjects(IProject[] projects) {
    m_projects = projects;
    refreshProposals();
  }

  private void refreshProposals() {
    if (m_projects == null) {
      return;
    }
    m_proposals.clear();
    m_proposalSorting.clear();
    for (IProject project : m_projects) {
      try {
        List<P_ResourceProposal> proposals = new ArrayList<>();
        getResources(project, proposals);
        for (P_ResourceProposal prop : proposals) {
          m_proposals.put(prop.getContent(), prop);
          m_proposalSorting.add(prop.getContent());
        }

      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
      }
    }
  }

  private List<P_ResourceProposal> getResources(IProject project, List<P_ResourceProposal> resources) throws CoreException {
    for (IResource resource : project.members()) {
      if (m_filter == null || m_filter.accept(new ResourceProxy(resource))) {
        P_ResourceProposal p = new P_ResourceProposal(resource);
        resources.add(p);
        if (resource instanceof IFolder) {
          getResources(project, (IFolder) resource, resources);
        }
      }
    }
    return resources;
  }

  private List<P_ResourceProposal> getResources(IProject project, IFolder parentResouce, List<P_ResourceProposal> resources) throws CoreException {
    for (IResource resource : parentResouce.members()) {
      if (m_filter == null || m_filter.accept(new ResourceProxy(resource))) {
        P_ResourceProposal p = new P_ResourceProposal(resource);
        resources.add(p);
        if (resource instanceof IFolder) {
          getResources(project, (IFolder) resource, resources);
        }
      }
    }
    return resources;
  }

  public void setResourceFilter(IResourceFilter filter) {
    m_filter = filter;
    refreshProposals();
  }

  public IResourceFilter getResourceFilter() {
    return m_filter;
  }

  @Override
  public IContentProposal[] getProposals(String contents, int position) {
    ArrayList<P_ResourceProposal> list = new ArrayList<>();
    for (String proptext : m_proposalSorting) {
      if (proptext.startsWith(contents)) {
        list.add(m_proposals.get(proptext));
      }
    }
    return list.toArray(new IContentProposal[list.size()]);
  }

  @Override
  public Image getImage(Object element) {
    P_ResourceProposal prop = (P_ResourceProposal) element;
    return prop.getImage();
  }

  @Override
  public String getText(Object element) {
    P_ResourceProposal prop = (P_ResourceProposal) element;
    return prop.getContent();
  }

  private class P_ResourceProposal implements IContentProposal {
    private IResource m_resource;

    public P_ResourceProposal(IResource resource) {
      m_resource = resource;
    }

    @Override
    public String getContent() {

      return m_resource.getProjectRelativePath().toPortableString();
    }

    @Override
    public int getCursorPosition() {
      return getContent().length();
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public String getLabel() {
      return getContent();
    }

    public Image getImage() {
      if (m_resource instanceof IFolder) {
        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
      }
      else if (m_resource instanceof IFile) {
        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
      }
      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
    }

  }

}
