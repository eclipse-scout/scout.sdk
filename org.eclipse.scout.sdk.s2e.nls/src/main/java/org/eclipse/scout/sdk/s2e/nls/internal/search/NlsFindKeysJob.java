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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.model.INlsEntry;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.search.ui.text.Match;

/**
 * <h4>NlsFindReferencesJob</h4>
 */
public class NlsFindKeysJob extends Job {

  private final List<String> m_searchKeys;
  private final Map<String, List<Match>> m_matches;

  public NlsFindKeysJob(String nlsKey, String jobTitle) {
    super(jobTitle);
    m_searchKeys = new ArrayList<>(1);
    m_searchKeys.add("\"" + nlsKey + "\"");
    m_matches = new HashMap<>(m_searchKeys.size());
  }

  /**
   * @param name
   */
  public NlsFindKeysJob(INlsProject project, String jobTitle) {
    super(jobTitle);
    m_searchKeys = new ArrayList<>();
    for (INlsEntry e : project.getAllEntries()) {
      if (e.getType() == INlsEntry.TYPE_LOCAL) {
        m_searchKeys.add("\"" + e.getKey() + "\"");
      }
    }
    m_matches = new HashMap<>(m_searchKeys.size());
  }

  @Override
  public IStatus run(IProgressMonitor monitor) {
    try {
      m_matches.clear();
      IJavaProject[] javaProjects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
      monitor.beginTask("Searching for NLS keys", javaProjects.length);
      for (IJavaProject root : javaProjects) {
        monitor.setTaskName("Searching in '" + root.getElementName() + "'.");

        IProject p = root.getProject();
        Path outputLocation = Paths.get(new java.io.File(p.getLocation().toOSString(), root.getOutputLocation().removeFirstSegments(1).toOSString()).toURI());
        searchInFolder(Paths.get(p.getLocation().toFile().toURI()), p.getDefaultCharset(), outputLocation, monitor);

        if (monitor.isCanceled()) {
          return Status.OK_STATUS;
        }
        monitor.worked(1);
      }
    }
    catch (Exception e) {
      SdkLog.error("Could not create java projects for nls search.", e);
    }
    finally {
      monitor.done();
    }
    return Status.OK_STATUS;
  }

  protected void searchInFolder(Path folder, final String charset, final Path outputFolder, final IProgressMonitor monitor) throws IOException {
    Files.walkFileTree(folder,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (monitor.isCanceled()) {
              return FileVisitResult.TERMINATE;
            }
            if (dir.equals(outputFolder)) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            boolean isHiddenDir = dir.getFileName() != null && dir.getFileName().toString().startsWith(".");
            if (isHiddenDir) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path path = file.getFileName();
            if (path == null) {
              return FileVisitResult.CONTINUE;
            }
            String fileName = path.toString().toLowerCase();
            if (attrs.isRegularFile() && (fileName.endsWith(".java") || fileName.endsWith(".html"))) {
              searchInFile(file, charset, monitor);
            }
            if (monitor.isCanceled()) {
              return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
          }
        });
  }

  protected void searchInFile(Path file, String charset, IProgressMonitor monitor) throws IOException {
    String content = new String(Files.readAllBytes(file), charset);
    IFile[] workspaceFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toUri());
    if (workspaceFiles.length < 1) {
      return;
    }
    IFile workspaceFile = workspaceFiles[0];
    if (!workspaceFile.exists()) {
      return;
    }

    for (String search : m_searchKeys) {
      int pos = 0;
      int index = -1;
      while ((index = content.indexOf(search, pos)) >= 0) {
        if (monitor.isCanceled()) {
          return;
        }

        Match match = new Match(workspaceFile, index, search.length());
        String key = search.substring(1, search.length() - 1); // remove starting and ending double quotes
        acceptNlsKeyMatch(key, match);
        pos = index + search.length();
      }
    }
  }

  protected void acceptNlsKeyMatch(String nlsKey, Match match) {
    List<Match> list = m_matches.get(nlsKey);
    if (list == null) {
      list = new ArrayList<>();
      m_matches.put(nlsKey, list);
    }
    list.add(match);
  }

  public List<Match> getMatches(String nlsKey) {
    List<Match> list = m_matches.get(nlsKey);
    if (list == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(list);
  }

  public Map<String, List<Match>> getAllMatches() {
    return new HashMap<>(m_matches);
  }
}
