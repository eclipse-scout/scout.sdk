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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.search.ui.text.Match;

/**
 * <h4>NlsFindReferencesJob</h4>
 */
@SuppressWarnings("restriction")
public class NlsFindReferencesJob extends Job {

  private final INlsProject m_project;
  private IType m_nlsType;
  // private HashMap<String, List<Match>> m_matches;
  private DefaultNlsKeySearchRequestor m_searchRequstor;

  /**
   * @param name
   */
  public NlsFindReferencesJob(INlsProject project, String text) {
    super(text);
    m_project = project;
    m_nlsType = project.getNlsAccessorType();
    m_searchRequstor = new DefaultNlsKeySearchRequestor(getProject());
  }

  @Override
  public IStatus run(IProgressMonitor monitor) {
    SearchEngine.getDefaultSearchParticipant();
    List<IJavaProject> jProjects = new LinkedList<IJavaProject>();
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    try {
      for (IProject project : projects) {
        if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID) && project.hasNature(PDE.PLUGIN_NATURE)) {
          jProjects.add(JavaCore.create(project));
        }
      }
    }
    catch (CoreException e) {
      NlsCore.logError("Could not create java projects for nls search.");
    }
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(jProjects.toArray(new IJavaElement[jProjects.size()]), true);

    SearchPattern pattern = SearchPattern.createPattern(m_nlsType, IJavaSearchConstants.REFERENCES);
    SearchEngine engine = new SearchEngine();

    try {

      engine.search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, scope, m_searchRequstor, monitor);

    }
    catch (CoreException e) {
      NlsCore.logError("NLS search failed.", e);
    }
    return Status.OK_STATUS;
  }

  public Map<String, List<Match>> getMatches() {
    if (getState() != Job.NONE) {
      throw new IllegalAccessError("job has not finished yet.");
    }
    return m_searchRequstor.getAllMatches();
  }

  public Match[] getMatches(String key) {
    if (getState() != Job.NONE) {
      throw new IllegalAccessError("job has not finished yet.");
    }
    return m_searchRequstor.getMatches(key);
  }

  public INlsProject getProject() {
    return m_project;
  }

  public DefaultNlsKeySearchRequestor getSearchRequstor() {
    return m_searchRequstor;
  }

  public IType getNlsType() {
    return m_nlsType;
  }
}
