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
package org.eclipse.scout.nls.sdk.internal.model.workspace;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.TypeHandler;
import org.eclipse.scout.nls.sdk.internal.model.workspace.nlsfile.AbstractNlsFile;
import org.eclipse.scout.nls.sdk.internal.model.workspace.nlsfile.PlatformNlsFile;
import org.eclipse.scout.nls.sdk.internal.model.workspace.nlsfile.WorkspaceNlsFile;
import org.eclipse.scout.nls.sdk.internal.model.workspace.project.NlsProject;
import org.eclipse.scout.nls.sdk.internal.model.workspace.translationfile.AbstractTranslationFile;
import org.eclipse.scout.nls.sdk.internal.model.workspace.translationfile.PlatformTranslationFile;
import org.eclipse.scout.nls.sdk.internal.model.workspace.translationfile.WorkspaceTranslationFile;
import org.eclipse.scout.nls.sdk.model.workspace.INlsWorkspace;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFile;

/**
 * <h4>NlsWorkspace</h4> access this class by {@link NlsCore#getNlsWorkspace()}.
 */
public class NlsWorkspace implements INlsWorkspace {

  private HashMap<String, NlsProject> m_projects = new HashMap<String, NlsProject>();

  /**
   * do not instantiate this class
   */
  public NlsWorkspace() {
  }

  public NlsProject findNlsProject(IProject project, IProgressMonitor monitor) throws CoreException {
    return findNlsProject(project, "translation.nls", monitor);
  }

  public NlsProject findNlsProject(IProject project, String fileName, IProgressMonitor monitor) throws CoreException {
    IFile nlsResource = project.getFile(fileName);
    NlsProject nlsProject = findNlsProject(nlsResource, monitor);
    return nlsProject;
  }

  public NlsProject findNlsProject(IFile nlsResource, IProgressMonitor monitor) throws CoreException {
    AbstractNlsFile nlsFile = loadNlsFile(nlsResource);
    if (nlsFile == null) {
      return null;
    }
    IType type = TypeHandler.getType(nlsFile.getNlsTypeName());
    return findNlsProject(type, monitor);
  }

  public NlsProject findNlsProject(IType type, IProgressMonitor monitor) throws CoreException {
    if (type == null || !type.exists()) {
      NlsCore.logError("nls type does not exist", new Exception());
      return null;
    }
    String hostPluginId = getPluginId(type);
    NlsProject nlsProject = m_projects.get(type.getFullyQualifiedName());
    if (nlsProject == null) {
      nlsProject = new NlsProject(type, hostPluginId);
      if (!type.isBinary()) {
        // TODO Marker Builder for NLS Keys
        // new NlsMarkerBuilder(nlsProject);
      }
      m_projects.put(type.getFullyQualifiedName(), nlsProject);
    }
    return nlsProject;
  }

  private AbstractNlsFile loadNlsFile(IFile file) throws CoreException {
    AbstractNlsFile nlsFile = null;
    if (file != null && file.exists()) {
      if (file.isReadOnly()) {
        nlsFile = new PlatformNlsFile(file.getContents(), file.getName());
      }
      else {
        nlsFile = new WorkspaceNlsFile(file);
      }
    }
    return nlsFile;
  }

  public ITranslationFile[] loadTranslationFiles(NlsType nlsType, IProgressMonitor monitor) throws CoreException {
    if (nlsType.getType().isReadOnly()) {
      // find bundle
      String bundleId = getPluginId(nlsType.getType());
      if (bundleId == null) {
        NlsCore.logWarning("could not find bundle: " + bundleId);
        return new ITranslationFile[]{};
      }
      return loadTranslationFilesFromPlatform(nlsType, bundleId);

    }
    else {
      return loadTranslationFilesWorkspace(nlsType, monitor);
    }
  }

  private ITranslationFile[] loadTranslationFilesWorkspace(NlsType nlsType, IProgressMonitor monior) throws CoreException {
    // try to find all translation files
    ArrayList<ITranslationFile> translationFiles = new ArrayList<ITranslationFile>();
    if (nlsType != null && nlsType.getTranslationsFolderName() != null && nlsType.getTranslationsPrefix() != null) {
      Path translationPath = new Path(nlsType.getTranslationsFolderName());
      for (IFile file : NlsCore.getAllTranslations(nlsType.getJavaProject().getProject(), translationPath, nlsType.getTranslationsPrefix())) {
        translationFiles.add(new WorkspaceTranslationFile(file));
      }
      List<IProject> workspaceFragments = NlsCore.getWorkspaceFragments(nlsType.getHostPluginId());
      for (IProject fragment : workspaceFragments) {
        for (IFile file : NlsCore.getAllTranslations(fragment, translationPath, nlsType.getTranslationsPrefix())) {
          translationFiles.add(new WorkspaceTranslationFile(file));
        }
      }
    }
    return translationFiles.toArray(new ITranslationFile[translationFiles.size()]);
  }

  @SuppressWarnings("unchecked")
  private ITranslationFile[] loadTranslationFilesFromPlatform(NlsType nlsType, String bundleId) throws CoreException {
    ArrayList<ITranslationFile> translationFiles = new ArrayList<ITranslationFile>();
    Enumeration eee = Platform.getBundle(bundleId).findEntries(nlsType.getTranslationsFolderName(), nlsType.getTranslationsPrefix() + "*.properties", false);
    if (eee != null) {
      while (eee.hasMoreElements()) {
        Object o = eee.nextElement();
        if (o instanceof URL) {
          try {
            translationFiles.add(new PlatformTranslationFile(((URL) o).openStream(), NlsCore.getLanguage(((URL) o).getFile())));
          }
          catch (IOException e) {
            NlsCore.logError("could not load NLS files of bundle '" + bundleId + "'", e);
          }
        }
      }
    }
    return translationFiles.toArray(new AbstractTranslationFile[translationFiles.size()]);
  }

  private String getPluginId(IJavaElement type) {
    if (type.isReadOnly()) {
      // find bundle

      IJavaElement root = type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
      String bundleId = null;
      if (root != null) {
        Matcher matcher = Pattern.compile("^([^_]*)_[0-9]+\\.[0-9]+\\.[0-9]+(\\.[^.]+)?\\.jar$").matcher(root.getElementName());
        if (matcher.matches()) {
          bundleId = matcher.group(1);
        }
      }
      return bundleId;
    }
    else {
      return type.getAncestor(IJavaElement.JAVA_PROJECT).getElementName();
    }
  }

}
