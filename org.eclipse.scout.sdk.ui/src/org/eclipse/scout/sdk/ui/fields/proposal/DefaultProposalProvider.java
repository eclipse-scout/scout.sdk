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
package org.eclipse.scout.sdk.ui.fields.proposal;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.scout.commons.CompareUtility;

public class DefaultProposalProvider implements IContentProposalProvider {

  private IContentProposalEx[] m_shortList;
  private IContentProposalEx[] m_expertList;

  public DefaultProposalProvider() {
    this(new IContentProposalEx[0]);
  }

  public DefaultProposalProvider(IContentProposalEx[] shortList) {
    this(shortList, null);
  }

  public DefaultProposalProvider(IContentProposalEx[] shortList, IContentProposalEx[] expertList) {
    setShortList(shortList);
    setExpertList(expertList);

  }

  @Override
  public IContentProposalEx[] getProposalsExpertMode(String content, int cursorPosition, IProgressMonitor monitor) {
    ArrayList<IContentProposalEx> props = new ArrayList<IContentProposalEx>();
    String matchString = content.substring(0, cursorPosition);
    matchString = matchString.toLowerCase() + "*";
    for (IContentProposalEx prop : m_expertList) {
      if (CharOperation.match(matchString.toCharArray(), prop.getLabel(false, true).toCharArray(), false)) {
        props.add(prop);
      }
    }
    return props.toArray(new IContentProposalEx[props.size()]);
  }

  @Override
  public IContentProposalEx[] getProposals(String content, int cursorPosition, IProgressMonitor monitor) {
    ArrayList<IContentProposalEx> props = new ArrayList<IContentProposalEx>();
    String matchString = content.substring(0, cursorPosition);
    matchString = matchString.toLowerCase() + "*";

    for (IContentProposalEx prop : m_shortList) {
      if (CharOperation.match(matchString.toCharArray(), prop.getLabel(false, false).toCharArray(), false)) {
        props.add(prop);
      }
    }
    return props.toArray(new IContentProposalEx[props.size()]);
  }

  @Override
  public boolean supportsExpertMode() {
    return m_expertList != null && m_expertList.length > 0;
  }

  public IContentProposalEx[] getShortList() {
    return m_shortList;
  }

  public void setShortList(IContentProposalEx[] shortList) {
    if (shortList == null) {
      shortList = new IContentProposalEx[0];
    }
    m_shortList = shortList;
  }

  public IContentProposalEx[] getExpertList() {
    return m_expertList;
  }

  public void setExpertList(IContentProposalEx[] expertList) {
    if (expertList == null) {
      expertList = new IContentProposalEx[0];
    }
    m_expertList = expertList;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DefaultProposalProvider) {
      DefaultProposalProvider compTo = (DefaultProposalProvider) obj;
      if (CompareUtility.equals(getShortList(), compTo.getShortList())) {
        if (CompareUtility.equals(getExpertList(), compTo.getExpertList())) {
          return true;
        }
      }
    }
    return false;
  }

}
