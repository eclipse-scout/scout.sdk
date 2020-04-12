/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.search;

import static org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils.queryResultToSearchResult;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.s.nls.query.MissingTranslationQuery;
import org.eclipse.scout.sdk.core.s.util.search.IFileQueryResult;
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
    IFileQueryResult query = EclipseWorkspaceWalker.executeQuerySync(new MissingTranslationQuery(), monitor);
    queryResultToSearchResult(query, getSearchResult());
    return Status.OK_STATUS;
  }
}
