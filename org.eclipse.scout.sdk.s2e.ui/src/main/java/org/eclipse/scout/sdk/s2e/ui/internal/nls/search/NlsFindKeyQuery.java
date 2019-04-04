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
package org.eclipse.scout.sdk.s2e.ui.internal.nls.search;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.text.Match;

/**
 * <h4>NlsKeySearchQuery</h4>
 */
public class NlsFindKeyQuery extends FileSearchQuery {

  private final String m_nlsKey;
  private final TranslationStoreStack m_project;

  public NlsFindKeyQuery(TranslationStoreStack project, String nlsKey) {
    super("", false, false, null);
    m_project = project;
    m_nlsKey = nlsKey;
  }

  @Override
  public String getResultLabel(int matches) {
    return "References to the text key '" + getNlsKey() + "' (" + matches + ").";
  }

  @Override
  public String getLabel() {
    return "Find references to the text key '" + getNlsKey() + "'...";
  }

  @Override
  public FileSearchResult getSearchResult() {
    return (FileSearchResult) super.getSearchResult();
  }

  @Override
  public IStatus run(IProgressMonitor monitor) {
    NlsFindKeysJob nlsFindReferencesJob = new NlsFindKeysJob(getNlsKey(), getLabel());
    IStatus result = nlsFindReferencesJob.run(monitor);
    if (result != null && result.isOK()) {
      List<Match> matches = nlsFindReferencesJob.getMatches(getNlsKey());
      FileSearchResult searchResult = getSearchResult();
      searchResult.removeAll();
      searchResult.addMatches(matches.toArray(new Match[0]));
    }
    return result;
  }

  public String getNlsKey() {
    return m_nlsKey;
  }

  public TranslationStoreStack getProject() {
    return m_project;
  }
}
