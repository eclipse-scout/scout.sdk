/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.search;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.runInEclipseEnvironment;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.toScoutProgress;

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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.EclipseProgress;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.TranslationInputValidator;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker.WorkspaceFile;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.search.ui.text.Match;

/**
 * <h3>{@link NlsFindMissingKeys}</h3>
 * <p>
 * Searches for NLS keys that are used in the code but do not exist.
 *
 * @since 7.0.100
 */
public class NlsFindMissingKeys {

  private final Map<String, Collection<Pattern>> m_patternsByFileType;
  private final Map<IJavaProject, Set<String>> m_existingKeys;
  private final List<Match> m_matches;
  private final List<Match> m_errorMatches;
  private final String m_textClassName;
  private final char[] m_ignorePattern;

  public NlsFindMissingKeys() {
    m_existingKeys = new HashMap<>();
    m_matches = new ArrayList<>();
    m_errorMatches = new ArrayList<>();
    m_patternsByFileType = new HashMap<>();

    String nlsKeyPattern = TranslationInputValidator.REGEX_NLS_KEY_NAME.pattern();
    Pattern jsonTextKeyPat = Pattern.compile("\\$\\{textKey:(" + nlsKeyPattern + ')');
    Pattern jsTextKeyPat = Pattern.compile("session\\.text\\(('?)(" + nlsKeyPattern + ")('?)");
    m_patternsByFileType.put(JavaTypes.JAVA_FILE_EXTENSION, Collections.singletonList(Pattern.compile("TEXTS\\.get\\((?:[a-zA-Z0-9_]+,\\s*)?(\"?)(" + nlsKeyPattern + ")(\"?)")));
    m_patternsByFileType.put("json", Collections.singletonList(jsonTextKeyPat));
    m_patternsByFileType.put("js", Arrays.asList(jsTextKeyPat, jsonTextKeyPat));
    m_patternsByFileType.put("html", Arrays.asList(Pattern.compile("<scout:message key=\"(" + nlsKeyPattern + ")\"\\s*/?>"), jsTextKeyPat, jsonTextKeyPat));

    m_textClassName = JavaTypes.simpleName(IScoutRuntimeTypes.TEXTS) + JavaTypes.JAVA_FILE_SUFFIX;
    m_ignorePattern = "NO-NLS-CHECK".toCharArray();
  }

  private static Set<String> keysVisibleFrom(IJavaProject jp, IEnvironment env) {
    if (!JdtUtils.exists(jp) || !jp.getProject().isAccessible()) {
      return emptySet();
    }

    return TranslationStoreStack.create(jp.getProject().getLocation().toFile().toPath(), env, toScoutProgress((IProgressMonitor) null))
        .map(stack -> stack.allEntries()
            .map(ITranslation::key)
            .collect(toSet()))
        .orElse(emptySet());
  }

  public void search() {
    m_matches.clear();
    m_errorMatches.clear();

    runInEclipseEnvironment(this::searchInWorkspace).awaitDoneThrowingOnErrorOrCancel();
  }

  protected void searchInWorkspace(IEnvironment e, EclipseProgress p) {
    try {
      new EclipseWorkspaceWalker("Search for text keys that are used but do not exist.")
          .withExtensionsAccepted(m_patternsByFileType.keySet())
          .withFilter(this::isInterestingPath)
          .walk(file -> searchInFile(file, e), p.monitor());
    }
    catch (CoreException ex) {
      throw new SdkException(ex);
    }
  }

  protected boolean isInterestingPath(Path p, BasicFileAttributes attrs) {
    if (attrs.isDirectory()) {
      // skip tests && archetype resources
      return !p.endsWith("src/test") && !p.endsWith("archetype-resources") && !p.endsWith("generated-resources");
    }
    return !p.endsWith(m_textClassName);
  }

  protected void searchInFile(WorkspaceFile file, IEnvironment env) {
    if (!file.inWorkspace().isPresent()) {
      SdkLog.warning("File '{}' could not be found in the current Eclipse Workspace.", file.path());
      return;
    }

    Path lastSegment = file.path().getFileName();
    if (lastSegment == null) {
      return;
    }

    String fileName = lastSegment.toString().toLowerCase();
    int lastDotPos = fileName.lastIndexOf('.');
    String extension = fileName.substring(lastDotPos + 1);
    Collection<Pattern> patterns = m_patternsByFileType.get(extension);
    if (patterns == null || patterns.isEmpty()) {
      throw new SdkException("Unexpected: no pattern for file: {}.", file.path());
    }

    CharBuffer fileContent = CharBuffer.wrap(file.content());
    for (Pattern p : patterns) {
      Matcher matcher = p.matcher(fileContent);
      while (matcher.find()) {
        int keyGroup;
        if (matcher.groupCount() > 1) {
          // pattern with optional literal delimiter: '"' for java, ''' for js
          // check if the literal delimiter is present. if not present: not possible to detect the real key -> add to error list.
          keyGroup = 2;
          boolean noLiteral = Strings.isEmpty(matcher.group(1)) || Strings.isEmpty(matcher.group(3));
          if (noLiteral) {
            registerMatch(file, matcher, m_errorMatches, keyGroup);
            continue;
          }
        }
        else {
          // pattern without literal delimiter
          keyGroup = 1;
        }

        String key = matcher.group(keyGroup);
        if (!keyExists(file.inWorkspace().get().getProject(), key, env)) {
          registerMatch(file, matcher, m_matches, keyGroup);
        }
      }
    }
  }

  protected boolean keyExists(IProject context, String key, IEnvironment env) {
    return m_existingKeys
        .computeIfAbsent(JavaCore.create(context), jp -> keysVisibleFrom(jp, env))
        .contains(key);
  }

  protected boolean isIgnored(char[] content, int offset) {
    int nlPos = CharOperation.indexOf('\n', content, offset);
    if (nlPos < m_ignorePattern.length) {
      return false;
    }
    if (content[nlPos - 1] == '\r') {
      nlPos--;
    }
    return CharOperation.fragmentEquals(m_ignorePattern, content, nlPos - m_ignorePattern.length, false);
  }

  protected void registerMatch(WorkspaceFile file, MatchResult matcher, Collection<Match> targetList, int keyGroup) {
    int index = matcher.start(keyGroup);
    if (isIgnored(file.content(), index)) {
      return;
    }

    Match match = new Match(file.inWorkspace().get(), index, matcher.end(keyGroup) - index);
    targetList.add(match);
  }

  public List<Match> matches() {
    return unmodifiableList(m_matches);
  }

  public List<Match> errors() {
    return unmodifiableList(m_errorMatches);
  }
}
