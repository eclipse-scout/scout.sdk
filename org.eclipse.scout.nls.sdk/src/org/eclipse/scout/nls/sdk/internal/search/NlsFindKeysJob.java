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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.ui.text.Match;

/**
 * <h4>NlsFindReferencesJob</h4>
 */
@SuppressWarnings("restriction")
public class NlsFindKeysJob extends Job {

  private final Pattern m_searchPattern;
  private NlsKeySearchRequestor m_searchRequstor;

  public NlsFindKeysJob(String nlsKey, String jobTitle) {
    this(Pattern.compile("\\\"" + nlsKey + "\\\""), jobTitle);
  }

  /**
   * @param name
   */
  public NlsFindKeysJob(INlsProject project, String jobTitle) {
    this(createPatternForAllNlsKeys(project), jobTitle);
  }

  public NlsFindKeysJob(Pattern searchPattern, String jobName) {
    super(jobName);
    m_searchPattern = searchPattern;
    m_searchRequstor = new NlsKeySearchRequestor();
  }

  @Override
  public IStatus run(IProgressMonitor monitor) {
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    ArrayList<IResource> searchScopeRessources = new ArrayList<IResource>();
    TextSearchScope searchScope = null;
    try {
      for (IProject project : projects) {
        if (project.exists() && project.isOpen() && project.hasNature(JavaCore.NATURE_ID) && project.hasNature(PDE.PLUGIN_NATURE)) {
          searchScopeRessources.add(project);
        }
      }
      searchScope = TextSearchScope.newSearchScope(searchScopeRessources.toArray(new IResource[searchScopeRessources.size()]), Pattern.compile(".*\\.java"), false);
    }
    catch (CoreException e) {
      NlsCore.logError("Could not create java projects for nls search.");
    }
    try {
      TuningUtility.startTimer();
      TextSearchEngine.create().search(searchScope, m_searchRequstor, m_searchPattern, monitor);
    }
    finally {
      TuningUtility.stopTimer("Key search done...");
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

  public NlsKeySearchRequestor getSearchRequstor() {
    return m_searchRequstor;
  }

  public static Pattern createPatternForAllNlsKeys(INlsProject project) {
    StringBuilder patternBuilder = new StringBuilder();
    String[] allKeys = project.getAllKeys();
    if (allKeys.length > 0) {
      patternBuilder.append("\\\"").append(allKeys[0]).append("\\\"");
    }
    for (int i = 1; i < allKeys.length; i++) {
      patternBuilder.append("|\\\"").append(allKeys[i]).append("\\\"");
    }
    return Pattern.compile(patternBuilder.toString());
  }
}
