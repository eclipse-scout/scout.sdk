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
package org.eclipse.scout.nls.sdk.simple.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsFolder;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.sdk.util.log.SdkLogManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class NlsSdkSimple extends AbstractUIPlugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "org.eclipse.scout.nls.sdk.simple"; //$NON-NLS-1$
  private static final Pattern PATTERN = Pattern.compile("^[^_.]*(_([A-Za-z]{2}))?(_([A-Za-z]{2}))?(_([A-Za-z]{2}))?\\.properties$");

  // The shared instance
  private static NlsSdkSimple plugin;
  private static SdkLogManager logManager;

  /**
   * The constructor
   */
  public NlsSdkSimple() {
  }

  public static Language getLanguage(String simpleFileName) {
    Matcher matcher = PATTERN.matcher(simpleFileName);
    if (matcher.matches()) {
      if (matcher.group(2) == null) {
        // default
        return Language.LANGUAGE_DEFAULT;
      }
      else {
        String languageIso = matcher.group(2);
        if (languageIso == null) {
          languageIso = "";
        }
        String countryIso = matcher.group(4);
        if (countryIso == null) {
          countryIso = "";
        }
        String variantIso = matcher.group(5);
        if (variantIso == null) {
          variantIso = "";
        }
        return new Language(new Locale(languageIso, countryIso, variantIso));
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    logManager = new SdkLogManager(this);
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    logManager = null;
    super.stop(context);

  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static NlsSdkSimple getDefault() {
    return plugin;
  }

  /**
   * finds all translations of the passed file in the toLookAt project. e.g. if a file
   * /lib/translations/messages.properties is passed all files /lib/translations/messages_[de|en|..].properties will be
   * returned.
   *
   * @param toLookAt
   *          the project to take a look at usually a fragment of the project the passed file is in.
   * @param file
   *          the definition of the translation file.
   * @return
   * @throws CoreException
   */
  public static List<IFile> getAllTranslations(IProject toLookAt, IPath path, String fileNamePrefix) throws CoreException {
    List<IFolder> folders = new LinkedList<>();
    List<INlsFolder> nlsFolders = getFoldersOfProject(toLookAt, path);
    for (INlsFolder folder : nlsFolders) {
      folders.add(folder.getFolder());
    }

    return getAllTranslations(folders, fileNamePrefix);
  }

  public static List<IFile> getAllTranslations(List<IFolder> folders, String fileNamePrefix) throws CoreException {
    List<IFile> files = new LinkedList<>();
    for (IFolder folder : folders) {
      if (folder.exists()) {
        IResource[] resources = folder.members(IResource.NONE);
        for (IResource resource : resources) {
          if (resource instanceof IFile && resource.getName().matches(fileNamePrefix + "(_[a-zA-Z]{2}){0,3}" + "\\.properties")) {
            files.add((IFile) resource);
          }
        }
      }
    }
    return files;
  }

  public static List<INlsFolder> getFoldersOfProject(IProject project, IPath path) throws CoreException {
    List<INlsFolder> folders = new LinkedList<>();
    if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
      // check runtime dir
      IJavaProject jp = JavaCore.create(project);
      IClasspathEntry[] clEntries = jp.getRawClasspath();
      for (IClasspathEntry entry : clEntries) {
        if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
          IPath toCheck = entry.getPath().append(path);
          IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(toCheck);
          if (folder.exists()) {
            folders.add(new NlsFolder(folder, INlsFolder.TYPE_PACKAGE_FOLDER));
          }
        }
      }

      // check path relative to project
      IFolder foundFolder = project.getFolder(path);
      if (foundFolder != null && foundFolder.exists()) {
        folders.add(new NlsFolder(foundFolder, INlsFolder.TYPE_SIMPLE_FOLDER));
      }
    }
    return folders;
  }

  public static String getLocalizedPropertiesFileName(String prefix, Language language) {
    String fileName = prefix;
    Locale locale = language.getLocale();
    fileName = fileName + "_" + locale.toString() + ".properties";
    return fileName;
  }

  public static void logInfo(Throwable t) {
    logManager.logInfo(t);
  }

  public static void logInfo(String message) {
    logManager.logInfo(message);
  }

  public static void logInfo(String message, Throwable t) {
    logManager.logInfo(message, t);
  }

  public static void logWarning(String message) {
    logManager.logWarning(message);
  }

  public static void logWarning(Throwable t) {
    logManager.logWarning(t);
  }

  public static void logWarning(String message, Throwable t) {
    logManager.logWarning(message, t);
  }

  public static void logError(Throwable t) {
    logManager.logError(t);
  }

  public static void logError(String message) {
    logManager.logError(message);
  }

  public static void logError(String message, Throwable t) {
    logManager.logError(message, t);
  }

}
