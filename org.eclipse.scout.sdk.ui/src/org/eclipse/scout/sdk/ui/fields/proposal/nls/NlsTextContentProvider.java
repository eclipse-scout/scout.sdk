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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IDialogSettingsProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.ISeparatorProposal;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>{@link NlsTextContentProvider}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 07.02.2012
 */
public class NlsTextContentProvider extends ContentProposalProvider implements IDialogSettingsProvider {

  public static final ISeparatorProposal NLS_NEW_PROPOSAL = new ISeparatorProposal() {
  };
  private NlsTextLabelProvider m_labelProvider;

  public NlsTextContentProvider(NlsTextLabelProvider labelProvider) {
    m_labelProvider = labelProvider;
  }

  @Override
  public Object[] getProposals(String searchPattern, IProgressMonitor monitor) {
    HashSet<INlsEntry> entries = new HashSet<INlsEntry>();
    if (getLabelProvider().getNlsProject() != null) {
      if (searchPattern == null) {
        searchPattern = "*";
      }

      getLabelProvider().startRecordMatchRegions();
      NormalizedPattern pattern = createNormalizedSearchPattern(searchPattern);
      Language developmentLanguage = getLabelProvider().getNlsProject().getDevelopmentLanguage();
      for (INlsEntry entry : getLabelProvider().getNlsProject().getAllEntries()) {
        if (monitor.isCanceled()) {
          break;
        }
        // development language
        int[] matchingRegions = getMatchingRegions(entry, entry.getTranslation(developmentLanguage), pattern);
        if (matchingRegions != null) {
          entries.add(entry);
          getLabelProvider().addMatchRegions(entry, matchingRegions);
        }
        else {
          // check key
          if (getMatchingRegions(entry, entry.getKey(), pattern) != null) {
            entries.add(entry);
          }
          // also check all languages
          for (Entry<Language, String> e : entry.getAllTranslations().entrySet()) {
            if (!developmentLanguage.equals(e.getKey())) {
              if (getMatchingRegions(entry, e.getValue(), pattern) != null) {
                entries.add(entry);
                break;
              }
            }
          }
        }
      }
      INlsEntry[] nlsEntryResult = entries.toArray(new INlsEntry[entries.size()]);
      Arrays.sort(nlsEntryResult, new P_NlsEntryComparator());
      Object[] result = new Object[nlsEntryResult.length + 1];
      System.arraycopy(nlsEntryResult, 0, result, 0, nlsEntryResult.length);
      result[result.length - 1] = NLS_NEW_PROPOSAL;
      return result;
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
  public boolean equals(Object obj) {
    if (!(obj instanceof NlsTextContentProvider)) {
      return false;
    }
    NlsTextContentProvider ref = (NlsTextContentProvider) obj;
    return CompareUtility.equals(getLabelProvider(), ref.getLabelProvider());
  }

  private class P_NlsEntryComparator implements Comparator<INlsEntry> {
    @Override
    public int compare(INlsEntry o1, INlsEntry o2) {
      if (o1 == null) {
        return -1;
      }
      if (o2 == null) {
        return 1;
      }
      Language developmentLanguage = o1.getProject().getDevelopmentLanguage();
      return CompareUtility.compareTo(new CompositeObject(o1.getTranslation(developmentLanguage), o1.getKey()), new CompositeObject(o2.getTranslation(developmentLanguage), o2.getKey()));
    }
  }
}
