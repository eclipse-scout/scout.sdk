/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.nls.internal.search;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;

import java.nio.CharBuffer;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.ui.formatter.InputValidator;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker.WorkspaceFile;
import org.eclipse.search.ui.text.Match;

/**
 * <h3>{@link NlsFindMissingKeysJob}</h3>
 * <p>
 * Searches for NLS keys that are used in the code but do not exist.
 *
 * @since 7.0.100
 */
public class NlsFindMissingKeysJob extends AbstractJob {

  private final Map<String, Collection<Pattern>> m_patternsByFileType;
  private final Map<IJavaProject, Set<String>> m_existingKeys;
  private final List<Match> m_matches;
  private final List<Match> m_errorMatches;
  private final String m_textClassName;
  private final char[] m_ignorePattern;

  public NlsFindMissingKeysJob() {
    super("Search for text keys that are used but do not exist.");
    m_existingKeys = new HashMap<>();
    m_matches = new ArrayList<>();
    m_errorMatches = new ArrayList<>();
    m_patternsByFileType = new HashMap<>();

    final String nlsKeyPattern = InputValidator.REGEX_NLS_KEY_NAME.pattern();
    final Pattern jsonTextKeyPat = Pattern.compile("\\$\\{textKey:(" + nlsKeyPattern + ')');
    final Pattern jsTextKeyPat = Pattern.compile("session\\.text\\(('?)(" + nlsKeyPattern + ")('?)");
    m_patternsByFileType.put(SuffixConstants.EXTENSION_java, Collections.singletonList(Pattern.compile("TEXTS\\.get\\((\"?)(" + nlsKeyPattern + ")(\"?)")));
    m_patternsByFileType.put("json", Collections.singletonList(jsonTextKeyPat));
    m_patternsByFileType.put("js", Arrays.asList(jsTextKeyPat, jsonTextKeyPat));
    m_patternsByFileType.put("html", Arrays.asList(Pattern.compile("\\<scout:message key=\"(" + nlsKeyPattern + ")\"\\s*/?\\>"), jsTextKeyPat, jsonTextKeyPat));

    m_ignorePattern = "NO-NLS-CHECK".toCharArray();
    m_textClassName = Signature.getSimpleName(IScoutRuntimeTypes.TEXTS) + SuffixConstants.SUFFIX_STRING_java;
  }

  private static Set<String> keysVisibleFrom(final IJavaProject jp) {
    if (!S2eUtils.exists(jp)) {
      return emptySet();
    }

    final INlsProject nlsProject = NlsCore.getNlsWorkspace().getNlsProject(jp);
    if (nlsProject == null) {
      return emptySet();
    }
    return nlsProject.getAllKeys();
  }

  @Override
  protected void execute(final IProgressMonitor monitor) throws CoreException {
    m_matches.clear();
    m_errorMatches.clear();
    new EclipseWorkspaceWalker(getName())
        .withExtensionsAccepted(m_patternsByFileType.keySet())
        .withFilter(this::isInterestingPath)
        .walk(this::searchInFile, monitor);
  }

  protected boolean isInterestingPath(final Path p, final BasicFileAttributes attrs) {
    if (attrs.isDirectory()) {
      // skip tests && archetype resources
      return !p.endsWith("src/test") && !p.endsWith("archetype-resources") && !p.endsWith("generated-resources");
    }
    return !p.endsWith(m_textClassName);
  }

  protected void searchInFile(final WorkspaceFile file) {
    if (!file.inWorkspace().isPresent()) {
      SdkLog.warning("File '{}' could not be found in the current Eclipse Workspace.", file.path());
      return;
    }

    final Path lastSegment = file.path().getFileName();
    if (lastSegment == null) {
      return;
    }

    final String fileName = lastSegment.toString().toLowerCase();
    final int lastDotPos = fileName.lastIndexOf('.');
    final String extension = fileName.substring(lastDotPos + 1);
    final Collection<Pattern> patterns = m_patternsByFileType.get(extension);
    if (patterns == null || patterns.isEmpty()) {
      throw new SdkException("Unexpected: no pattern for file: " + file.path());
    }

    final CharBuffer fileContent = CharBuffer.wrap(file.content());
    for (final Pattern p : patterns) {
      final Matcher matcher = p.matcher(fileContent);
      while (matcher.find()) {
        final int keyGroup;
        if (matcher.groupCount() > 1) {
          // pattern with optional literal delimiter: '"' for java, ''' for js
          // check if the literal delimiter is present. if not present: not possible to detect the real key -> add to error list.
          keyGroup = 2;
          final boolean noLiteral = StringUtils.isEmpty(matcher.group(1)) || StringUtils.isEmpty(matcher.group(3));
          if (noLiteral) {
            registerMatch(file, matcher, m_errorMatches, keyGroup);
            continue;
          }
        }
        else {
          // pattern without literal delimiter
          keyGroup = 1;
        }

        final String key = matcher.group(keyGroup);
        if (!keyExists(file.inWorkspace().get().getProject(), key)) {
          registerMatch(file, matcher, m_matches, keyGroup);
        }
      }
    }
  }

  protected boolean keyExists(final IProject context, final String key) {
    return m_existingKeys
        .computeIfAbsent(JavaCore.create(context), NlsFindMissingKeysJob::keysVisibleFrom)
        .contains(key);
  }

  protected boolean isIgnored(final char[] content, final int offset) {
    int nlPos = CharOperation.indexOf('\n', content, offset);
    if (nlPos < m_ignorePattern.length) {
      return false;
    }
    if (content[nlPos - 1] == '\r') {
      nlPos--;
    }
    return CharOperation.fragmentEquals(m_ignorePattern, content, nlPos - m_ignorePattern.length, false);
  }

  protected void registerMatch(final WorkspaceFile file, final MatchResult matcher, final Collection<Match> targetList, final int keyGroup) {
    final int index = matcher.start(keyGroup);
    if (isIgnored(file.content(), index)) {
      return;
    }
    final Match match = new Match(file.inWorkspace().get(), index, matcher.end(keyGroup) - index);
    targetList.add(match);
  }

  public List<Match> matches() {
    return unmodifiableList(m_matches);
  }

  public List<Match> errors() {
    return unmodifiableList(m_errorMatches);
  }
}
