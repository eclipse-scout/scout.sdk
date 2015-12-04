/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.ui.proposal;

import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.scout.sdk.s2e.nls.INlsIcons;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.swt.graphics.Image;

/**
 * <h4>TextCompletionProposalComputer</h4>
 */
public class NlsTextCompletionProposalComputer extends AbstractNlsTextCompletionComputer {

  private final Image m_image = NlsCore.getImage(INlsIcons.COMMENT);

  public NlsTextCompletionProposalComputer() {

  }

  @Override
  protected void collectProposals(List<ICompletionProposal> proposals, INlsProject nlsProject, String prefix, int offset) {
    for (INlsEntry e : nlsProject.getEntries(prefix, false)) {
      proposals.add(new NlsProposal(e, prefix, offset, m_image));
    }
    proposals.add(new NewNlsProposal(nlsProject, null, prefix, offset));
  }

}
