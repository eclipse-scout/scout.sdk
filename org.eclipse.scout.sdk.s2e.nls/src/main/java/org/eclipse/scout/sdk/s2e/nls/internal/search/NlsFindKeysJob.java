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
package org.eclipse.scout.sdk.s2e.nls.internal.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.search.ui.text.Match;

/**
 * <h4>NlsFindReferencesJob</h4>
 */
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
    if (m_searchPattern == null) {
      return Status.OK_STATUS;
    }

    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    ArrayList<IResource> searchScopeRessources = new ArrayList<>();
    TextSearchScope searchScope = null;
    try {
      for (IProject project : projects) {
        if (project.exists() && project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
          searchScopeRessources.add(project);
        }
      }
      searchScope = TextSearchScope.newSearchScope(searchScopeRessources.toArray(new IResource[searchScopeRessources.size()]), Pattern.compile(".*\\.java"), false);
    }
    catch (CoreException e) {
      NlsCore.logError("Could not create java projects for nls search.", e);
    }
    TextSearchEngine.create().search(searchScope, m_searchRequstor, m_searchPattern, monitor);
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
    Set<String> allKeys = project.getAllKeys();
    Iterator<String> iterator = allKeys.iterator();
    if (iterator.hasNext()) {
      String key = iterator.next();
      StringBuilder patternBuilder = new StringBuilder();
      patternBuilder.append("\\\"").append(key).append("\\\"");
      while (iterator.hasNext()) {
        patternBuilder.append("|\\\"").append(iterator.next()).append("\\\"");
      }
      return Pattern.compile(patternBuilder.toString());
    }
    return null;
  }
}
