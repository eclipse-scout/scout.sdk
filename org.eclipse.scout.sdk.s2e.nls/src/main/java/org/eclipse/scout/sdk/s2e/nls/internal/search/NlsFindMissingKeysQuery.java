/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.search;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.text.Match;

/**
 * <h3>{@link NlsFindMissingKeysQuery}</h3>
 *
 * @since 7.0.100
 */
public class NlsFindMissingKeysQuery extends FileSearchQuery {

  public NlsFindMissingKeysQuery() {
    super("", false, false, null);
  }

  @Override
  public String getResultLabel(final int matches) {
    return "References to missing text keys (" + matches + ").";
  }

  @Override
  public String getLabel() {
    return "Find references to missing text keys...";
  }

  @Override
  public FileSearchResult getSearchResult() {
    return (FileSearchResult) super.getSearchResult();
  }

  @Override
  public IStatus run(final IProgressMonitor monitor) {
    final NlsFindMissingKeysJob nlsFindMissingKeysJob = new NlsFindMissingKeysJob();
    final IStatus result = nlsFindMissingKeysJob.run(monitor);
    if (result != null && result.getSeverity() == IStatus.CANCEL) {
      return result;
    }

    final List<Match> missingKeys = nlsFindMissingKeysJob.matches();
    final List<Match> unableToDetect = nlsFindMissingKeysJob.errors();
    final FileSearchResult searchResult = getSearchResult();
    searchResult.removeAll();
    searchResult.addMatches(missingKeys.toArray(new Match[missingKeys.size()]));
    searchResult.addMatches(unableToDetect.toArray(new Match[unableToDetect.size()]));

    return Status.OK_STATUS;
  }
}
