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
package org.eclipse.scout.sdk.ui.fields.proposal.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;

/**
 * <h3>{@link ResourcesContentProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 15.02.2012
 */
public class ResourcesContentProvider extends ContentProposalProvider {

  private final List<IResource> m_resources;
  private final ILabelProvider m_labelProvider;

  public ResourcesContentProvider(List<IResource> resources, ILabelProvider labelProvider) {
    m_resources = resources;
    m_labelProvider = labelProvider;
  }

  public ResourcesContentProvider(IResource[] resources, ILabelProvider labelProvider) {
    this(Arrays.asList(resources), labelProvider);
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    if (searchPattern == null) {
      searchPattern = "*";
    }
    else {
      searchPattern = searchPattern.replaceAll("\\*$", "") + "*";
    }
    char[] pattern = CharOperation.toLowerCase(searchPattern.toCharArray());
    ArrayList<Object> collector = new ArrayList<Object>();
    for (Object proposal : getAllResources()) {
      if (CharOperation.match(pattern, getLabelProvider().getText(proposal).toCharArray(), false)) {
        collector.add(proposal);
      }
    }
    return collector.toArray(new Object[collector.size()]);
  }

  public List<IResource> getAllResources() {
    return m_resources;
  }

  public ILabelProvider getLabelProvider() {
    return m_labelProvider;
  }
}
