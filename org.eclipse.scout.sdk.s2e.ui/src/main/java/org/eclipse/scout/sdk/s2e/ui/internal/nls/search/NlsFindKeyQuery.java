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
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.s.nls.query.TranslationKeysQuery;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;

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
    var query = EclipseWorkspaceWalker.executeQuerySync(new TranslationKeysQuery(getNlsKey(), getLabel()), monitor);
    queryResultToSearchResult(query.result(), getSearchResult());
    return Status.OK_STATUS;
  }

  public String getNlsKey() {
    return m_nlsKey;
  }

  public TranslationStoreStack getProject() {
    return m_project;
  }
}
