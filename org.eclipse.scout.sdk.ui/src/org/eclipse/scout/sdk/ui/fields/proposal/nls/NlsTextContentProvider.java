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
package org.eclipse.scout.sdk.ui.fields.proposal.nls;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IDialogSettingsProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.MoreElementsProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>{@link NlsTextContentProvider}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 07.02.2012
 */
public class NlsTextContentProvider extends ContentProposalProvider implements IDialogSettingsProvider {

  private final NlsTextLabelProvider m_labelProvider;

  public NlsTextContentProvider(NlsTextLabelProvider labelProvider) {
    m_labelProvider = labelProvider;
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    HashSet<NlsTextProposal> firstGroup = new HashSet<NlsTextProposal>();
    HashSet<NlsTextProposal> secondGroup = new HashSet<NlsTextProposal>();
    if (getLabelProvider().getNlsProject() != null) {
      if (!StringUtility.hasText(searchPattern)) {
        searchPattern = "*";
      }
      else {
        searchPattern = searchPattern.trim();
      }

      getLabelProvider().startRecordMatchRegions();
      NormalizedPattern pattern = createNormalizedSearchPattern(searchPattern);
      Language developmentLanguage = getLabelProvider().getNlsProject().getDevelopmentLanguage();
      for (INlsEntry entry : getLabelProvider().getNlsProject().getAllEntries()) {
        if (monitor.isCanceled()) {
          break;
        }

        NlsTextProposal candidate = new NlsTextProposal(entry, developmentLanguage);
        if (candidate.matches(pattern)) {
          int[] matchingRegions = candidate.getMatchingRegions(pattern, getLabelProvider().isFormatConcatString());
          if (candidate.getMatchKind() == NlsTextProposal.MATCH_DEV_LANG_TRANSLATION) {
            firstGroup.add(candidate);
          }
          else {
            secondGroup.add(candidate);
          }
          getLabelProvider().addMatchRegions(candidate, matchingRegions);
        }
      }

      boolean hasSecondGroupItems = secondGroup.size() > 0;
      int numAdditionalElements = hasSecondGroupItems ? 2 : 1;
      Object[] nlsEntryResult = new Object[firstGroup.size() + secondGroup.size() + numAdditionalElements];

      if (firstGroup.size() > 0) {
        Object[] firstPart = firstGroup.toArray(new Object[firstGroup.size()]);
        Arrays.sort(firstPart);
        System.arraycopy(firstPart, 0, nlsEntryResult, 0, firstPart.length);
      }

      nlsEntryResult[firstGroup.size()] = NlsTextProposal.NEW_NLS_TEXT_PROPOSAL;

      if (hasSecondGroupItems) {
        nlsEntryResult[firstGroup.size() + 1] = MoreElementsProposal.INSTANCE;
        Object[] secondPart = secondGroup.toArray(new Object[secondGroup.size()]);
        Arrays.sort(secondPart);
        System.arraycopy(secondPart, 0, nlsEntryResult, firstGroup.size() + numAdditionalElements, secondPart.length);
      }
      return nlsEntryResult;
    }
    else {
      return new Object[0];
    }
  }

  public NlsTextLabelProvider getLabelProvider() {
    return m_labelProvider;
  }

  @Override
  public IDialogSettings getDialogSettings() {
    return ScoutSdkUi.getDefault().getDialogSettingsSection(NlsTextLabelProvider.class.getName(), true);
  }

  @Override
  public int hashCode() {
    NlsTextLabelProvider labelProvider = getLabelProvider();
    if (labelProvider != null) {
      return labelProvider.hashCode();
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NlsTextContentProvider)) {
      return false;
    }
    NlsTextContentProvider ref = (NlsTextContentProvider) obj;
    return CompareUtility.equals(getLabelProvider(), ref.getLabelProvider());
  }
}
