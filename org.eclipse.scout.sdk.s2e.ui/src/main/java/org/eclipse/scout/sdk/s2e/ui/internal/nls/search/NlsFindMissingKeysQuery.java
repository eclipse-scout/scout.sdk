/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.search;

import static org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils.queryResultToSearchResult;

import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.s.nls.query.MissingTranslationQuery;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;

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
    return "References to missing translations (" + matches + ").";
  }

  @Override
  public String getLabel() {
    return "Find references to missing translations...";
  }

  @Override
  public FileSearchResult getSearchResult() {
    return (FileSearchResult) super.getSearchResult();
  }

  @Override
  public IStatus run(IProgressMonitor monitor) {
    var query = EclipseWorkspaceWalker.executeQuerySync(MissingTranslationQuery::new, monitor);
    queryResultToSearchResult(query
        .result()
        .filter(r -> r.severity() >= Level.WARNING.intValue()), getSearchResult());
    return Status.OK_STATUS;
  }
}
