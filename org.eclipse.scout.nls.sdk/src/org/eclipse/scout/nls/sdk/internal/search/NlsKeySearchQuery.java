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
package org.eclipse.scout.nls.sdk.internal.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.text.Match;

/** <h4>NlsKeySearchQuery</h4> */
@SuppressWarnings("restriction")
public class NlsKeySearchQuery extends FileSearchQuery {

  private final String m_nlsKey;
  private final INlsProject m_project;

  public NlsKeySearchQuery(INlsProject project, String nlsKey) {
    super("", false, false, null);
    m_project = project;
    m_nlsKey = nlsKey;
  }

  @Override
  public String getResultLabel(int matches) {
    return "References to the NLS key '" + getNlsKey() + "' (" + matches + ")";
  }

  @Override
  public String getLabel() {
    return "Find references to the NLS key '" + getNlsKey() + "'...";
  }

  @Override
  public FileSearchResult getSearchResult() {

    return (FileSearchResult) super.getSearchResult();
  }

  @Override
  public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
    NlsFindReferencesJob nlsFindReferencesJob = new NlsFindReferencesJob(getProject(), getLabel());
    nlsFindReferencesJob.run(monitor);
    Match[] matches = nlsFindReferencesJob.getMatches(getNlsKey());
    getSearchResult().removeAll();
    getSearchResult().addMatches(matches);
    return Status.OK_STATUS;
  }

  public String getNlsKey() {
    return m_nlsKey;
  }

  public INlsProject getProject() {
    return m_project;
  }
}
