/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.search;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.nls.internal.search.JavaProjectsWalker.WorkspaceFile;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.search.ui.text.Match;

/**
 * <h4>NlsFindReferencesJob</h4>
 */
public class NlsFindKeysJob extends AbstractJob {

  private final List<char[]> m_searchKeys;
  private final Map<String, List<Match>> m_matches;

  public NlsFindKeysJob(final String nlsKey, final String jobTitle) {
    this(singletonList(nlsKey), jobTitle);
  }

  public NlsFindKeysJob(final INlsProject project, final String jobTitle) {
    this(getLocalKeys(project), jobTitle);
  }

  protected NlsFindKeysJob(final Collection<String> searchKeys, final String jobTitle) {
    super(jobTitle);
    m_searchKeys = new ArrayList<>(searchKeys.size() * 2);
    for (final String key : searchKeys) {
      m_searchKeys.add(('"' + key + '"').toCharArray());
      m_searchKeys.add(('\'' + key + '\'').toCharArray()); // e.g. for search in .js files
    }
    m_matches = new HashMap<>();
  }

  private static List<String> getLocalKeys(final INlsProject project) {
    return project.getAllEntries().stream()
        .filter(entry -> entry.getType() == INlsEntry.TYPE_LOCAL)
        .map(INlsEntry::getKey)
        .collect(Collectors.toList());
  }

  @Override
  public void execute(final IProgressMonitor monitor) throws CoreException {
    m_matches.clear();
    new JavaProjectsWalker(getName())
        .withExtensionsAccepted(SuffixConstants.EXTENSION_java, "js", "html", "less", "json", "xml", "sql", "css", "svg", "txt", "jsp")
        .walk(this::searchInFile, monitor);
  }

  protected void searchInFile(final WorkspaceFile file) {
    for (final char[] search : m_searchKeys) {
      int pos = 0;
      int index;
      while ((index = CharOperation.indexOf(search, file.content(), true, pos)) >= 0) {
        if (file.inWorkspace().isPresent()) {
          final Match match = new Match(file.inWorkspace().get(), index, search.length);
          final String key = String.valueOf(search, 1, search.length - 2); // remove starting and ending quotes
          acceptNlsKeyMatch(key, match);
        }
        pos = index + search.length;
      }
    }
  }

  protected void acceptNlsKeyMatch(final String nlsKey, final Match match) {
    m_matches
        .computeIfAbsent(nlsKey, key -> new ArrayList<>())
        .add(match);
  }

  public List<Match> getMatches(final String nlsKey) {
    final List<Match> list = m_matches.get(nlsKey);
    if (list == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(list);
  }

  public Map<String, List<Match>> getAllMatches() {
    return new HashMap<>(m_matches);
  }
}
