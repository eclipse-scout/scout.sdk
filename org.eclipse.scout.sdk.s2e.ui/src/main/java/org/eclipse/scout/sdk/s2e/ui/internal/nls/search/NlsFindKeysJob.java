/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.search;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.s2e.environment.AbstractJob;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker.WorkspaceFile;
import org.eclipse.search.ui.text.Match;

/**
 * <h4>NlsFindReferencesJob</h4>
 */
public class NlsFindKeysJob extends AbstractJob {

  private final List<char[]> m_searchKeys;
  private final Map<String, List<Match>> m_matches;

  public NlsFindKeysJob(String nlsKey, String jobTitle) {
    this(singletonList(nlsKey), jobTitle);
  }

  public NlsFindKeysJob(TranslationStoreStack project, String jobTitle) {
    this(getEditableKeys(project), jobTitle);
  }

  protected NlsFindKeysJob(Collection<String> searchKeys, String jobTitle) {
    super(jobTitle);
    m_searchKeys = new ArrayList<>(searchKeys.size() * 2);
    for (String key : searchKeys) {
      m_searchKeys.add(('"' + key + '"').toCharArray());
      m_searchKeys.add(('\'' + key + '\'').toCharArray()); // e.g. for search in .js files
    }
    m_matches = new HashMap<>();
  }

  protected static List<String> getEditableKeys(TranslationStoreStack project) {
    return project.allEntries()
        .filter(e -> e.store().isEditable())
        .map(ITranslationEntry::key)
        .collect(toList());
  }

  @Override
  public void execute(IProgressMonitor monitor) throws CoreException {
    m_matches.clear();
    new EclipseWorkspaceWalker(getName())
        .withExtensionsAccepted(SuffixConstants.EXTENSION_java, "js", "html", "less", "json", "xml", "sql", "css", "svg", "txt", "jsp")
        .walk(this::searchInFile, monitor);
  }

  protected void searchInFile(WorkspaceFile file) {
    if (!file.inWorkspace().isPresent()) {
      SdkLog.warning("Cannot find file '{}' in the current Eclipse workspace.", file.path());
      return;
    }

    for (char[] search : m_searchKeys) {
      int pos = 0;
      int index;
      while ((index = CharOperation.indexOf(search, file.content(), true, pos)) >= 0) {
        Match match = new Match(file.inWorkspace().get(), index, search.length);
        String key = String.valueOf(search, 1, search.length - 2); // remove starting and ending quotes
        acceptNlsKeyMatch(key, match);
        pos = index + search.length;
      }
    }
  }

  protected void acceptNlsKeyMatch(String nlsKey, Match match) {
    m_matches
        .computeIfAbsent(nlsKey, key -> new ArrayList<>())
        .add(match);
  }

  public List<Match> getMatches(String nlsKey) {
    List<Match> list = m_matches.get(nlsKey);
    if (list == null) {
      return emptyList();
    }
    return unmodifiableList(list);
  }

  public Map<String, List<Match>> getAllMatches() {
    return unmodifiableMap(m_matches);
  }
}
