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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;

/**
 * <h3>{@link ResourcesContentProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 15.02.2012
 */
public class ResourcesContentProvider extends ContentProposalProvider implements IStructuredContentProvider {

  private final ILabelProvider m_labelProvider;
  private List<Object> m_elements;

  public ResourcesContentProvider(ILabelProvider labelProvider) {
    this(labelProvider, null);
  }

  public ResourcesContentProvider(ILabelProvider labelProvider, Object[] elements) {
    m_labelProvider = labelProvider;
    m_elements = new ArrayList<Object>();
    if (elements != null) {
      m_elements.addAll(Arrays.asList(elements));
    }
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    if (!StringUtility.hasText(searchPattern)) {
      searchPattern = "*";
    }
    else {
      searchPattern = searchPattern.replaceAll("\\*$", "") + "*";
    }
    char[] pattern = CharOperation.toLowerCase(searchPattern.toCharArray());
    ArrayList<Object> collector = new ArrayList<Object>();
    for (Object proposal : getElements()) {
      if (CharOperation.match(pattern, getLabelProvider().getText(proposal).toCharArray(), false)) {
        collector.add(proposal);
      }
    }
    return collector.toArray(new Object[collector.size()]);
  }

  public ILabelProvider getLabelProvider() {
    return m_labelProvider;
  }

  @Override
  public final Object[] getElements(Object inputElement) {
    return getElements();
  }

  public Object[] getElements() {
    return m_elements.toArray(new Object[m_elements.size()]);
  }

  public void setElements(Object[] resources) {
    m_elements.clear();
    m_elements.addAll(Arrays.asList(resources));
  }

  public boolean remove(Object element) {
    return m_elements.remove(element);
  }

  public boolean add(Object element) {
    return m_elements.add(element);
  }

}
