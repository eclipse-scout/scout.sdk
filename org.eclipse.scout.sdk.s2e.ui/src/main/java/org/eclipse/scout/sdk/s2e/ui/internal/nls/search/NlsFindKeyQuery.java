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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.core.s.nls.query.TranslationKeysQuery;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;

/**
 * <h4>NlsKeySearchQuery</h4>
 */
public class NlsFindKeyQuery extends FileSearchQuery {

  private final String m_nlsKey;
  private final TranslationManager m_project;

  public NlsFindKeyQuery(TranslationManager project, String nlsKey) {
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
    var query = new TranslationKeysQuery(getLabel());
    EclipseWorkspaceWalker.executeQuerySync(query, monitor);
    queryResultToSearchResult(query.result(getNlsKey()), getSearchResult());
    return Status.OK_STATUS;
  }

  public String getNlsKey() {
    return m_nlsKey;
  }

  public TranslationManager getProject() {
    return m_project;
  }
}
