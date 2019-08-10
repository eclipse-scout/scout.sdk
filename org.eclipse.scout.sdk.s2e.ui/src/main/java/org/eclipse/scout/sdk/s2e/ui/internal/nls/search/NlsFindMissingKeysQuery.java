/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.search;

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
  public String getResultLabel(int matches) {
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
  public IStatus run(IProgressMonitor monitor) {
    NlsFindMissingKeys nlsFindMissingKeysJob = new NlsFindMissingKeys();
    nlsFindMissingKeysJob.search();
    List<Match> missingKeys = nlsFindMissingKeysJob.matches();
    List<Match> unableToDetect = nlsFindMissingKeysJob.errors();
    FileSearchResult searchResult = getSearchResult();
    searchResult.removeAll();
    searchResult.addMatches(missingKeys.toArray(new Match[0]));
    searchResult.addMatches(unableToDetect.toArray(new Match[0]));

    return Status.OK_STATUS;
  }
}
