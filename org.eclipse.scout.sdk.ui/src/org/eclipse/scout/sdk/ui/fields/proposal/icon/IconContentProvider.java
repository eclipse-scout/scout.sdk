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
package org.eclipse.scout.sdk.ui.fields.proposal.icon;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;

/**
 * <h3>{@link IconContentProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 14.02.2012
 */
public class IconContentProvider extends ContentProposalProvider implements IStructuredContentProvider {
  private final IIconProvider m_iconProvider;
  private ScoutIconDesc[] m_elements;
  private final ILabelProvider m_labelProvider;

  public IconContentProvider(IIconProvider iconProvider, ILabelProvider labelProvider) {
    m_iconProvider = iconProvider;
    m_labelProvider = labelProvider;
    //lazy load
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    if (!StringUtility.hasText(searchPattern)) {
      searchPattern = "*";
    }
    else {
      searchPattern = searchPattern.replaceAll("\\*$", "").toLowerCase() + "*";
    }

    char[] pattern = searchPattern.toCharArray();
    ArrayList<Object> accepted = new ArrayList<Object>();
    for (Object element : getElements(this)) {
      String iconName = m_labelProvider.getText(element);
      if (iconName != null && CharOperation.match(pattern, iconName.toCharArray(), false)) {
        accepted.add(element);
      }
    }
    return accepted.toArray(new Object[accepted.size()]);
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (m_elements == null) {
      if (m_iconProvider != null) {
        m_elements = m_iconProvider.getIcons();
      }
      else {
        m_elements = new ScoutIconDesc[0];
      }
    }
    return m_elements;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
}
