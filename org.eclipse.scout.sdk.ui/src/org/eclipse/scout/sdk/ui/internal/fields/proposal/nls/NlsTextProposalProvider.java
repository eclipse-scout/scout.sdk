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
package org.eclipse.scout.sdk.ui.internal.fields.proposal.nls;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalEx;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposal;

/**
 * <h3>NlsTextProposalProvider</h3> ...
 */
public class NlsTextProposalProvider implements IContentProposalProvider {

  private NlsProposal[] m_proposals;

  public NlsTextProposalProvider() {
    m_proposals = new NlsProposal[0];
  }

  public NlsTextProposalProvider(INlsEntry[] entries, Language language) {
    setNlsEntries(entries, language);
  }

  public void setNlsEntries(INlsEntry[] entries, Language language) {
    TreeMap<CompositeObject, NlsProposal> proposals = new TreeMap<CompositeObject, NlsProposal>();
    for (INlsEntry entry : entries) {
      NlsProposal p = new NlsProposal(entry, language);
      proposals.put(new CompositeObject(p.getLabel(false, false), entry.getKey()), p);
    }
    m_proposals = proposals.values().toArray(new NlsProposal[proposals.size()]);
  }

  @Override
  public IContentProposalEx[] getProposals(String content, int cursorPosition, IProgressMonitor monitor) {
    ArrayList<IContentProposalEx> props = new ArrayList<IContentProposalEx>();
    String matchString = content.substring(0, cursorPosition);
    matchString = matchString.toLowerCase() + "*";
    for (NlsProposal prop : m_proposals) {
      // check key
      if (CharOperation.match(matchString.toCharArray(), prop.getNlsEntry().getKey().toCharArray(), false)) {
        props.add(prop);
      }
      else {
        // also check all languages
        for (String s : prop.getNlsEntry().getAllTranslations().values()) {
          if (s != null && CharOperation.match(matchString.toCharArray(), s.toCharArray(), false)) {
            props.add(prop);
            break;
          }
        }
      }
    }
    props.add(new NlsNewProposal());
    return props.toArray(new IContentProposalEx[props.size()]);
  }

  @Override
  public IContentProposalEx[] getProposalsExpertMode(String content, int cursorPosition, IProgressMonitor monitor) {
    return null;
  }

  @Override
  public boolean supportsExpertMode() {
    return false;
  }

}
