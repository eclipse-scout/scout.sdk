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
package org.eclipse.scout.sdk.s2e.nls.internal.simpleproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.core.util.WeakEventListener;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleproject.model.TranslationFileNewModel;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleproject.ui.TranslationFileNewDialog;
import org.eclipse.scout.sdk.s2e.nls.model.INewLanguageContext;
import org.eclipse.scout.sdk.s2e.nls.model.Language;
import org.eclipse.scout.sdk.s2e.nls.project.AbstractNlsProject;
import org.eclipse.scout.sdk.s2e.nls.project.NlsProjectEvent;
import org.eclipse.scout.sdk.s2e.nls.resource.ITranslationResource;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.swt.widgets.Shell;

public class SimpleNlsProject extends AbstractNlsProject {
  private NlsType m_nlsClass;
  private PropertyChangeListener m_nlsClassPropertyListener;

  public SimpleNlsProject(NlsType type) {
    super(type.getType());
    m_nlsClassPropertyListener = new P_NlsClassPropertyChangeListener();
    m_nlsClass = type;
    getNlsType().addPropertyChangeListener(m_nlsClassPropertyListener);
    updateTranslationResourceLocation();
    setSuperType(m_nlsClass.getSuperType());
  }

  @Override
  protected List<ITranslationResource> loadTranslationResources() throws CoreException {
    if (getNlsType().getType().isReadOnly()) {
      IPackageFragmentRoot r = (IPackageFragmentRoot) getNlsType().getType().getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
      if (r == null) {
        NlsCore.logWarning("Could not find text resource for type '" + getNlsType().getType().getFullyQualifiedName() + "'.");
        return new ArrayList<>(0);
      }
      return loadTranslationFilesFromPlatform(getNlsType(), r);
    }
    return loadTranslationFilesWorkspace(getNlsType());
  }

  private static List<ITranslationResource> loadTranslationFilesWorkspace(NlsType nlsType) throws CoreException {
    // try to find all translation files
    List<ITranslationResource> translationFiles = new ArrayList<>();
    if (nlsType != null && JdtUtils.exists(nlsType.getType()) && nlsType.getTranslationsFolderName() != null && nlsType.getTranslationsPrefix() != null) {
      Path translationPath = new Path(nlsType.getTranslationsFolderName());
      for (IFile file : getAllTranslations(nlsType.getType().getJavaProject().getProject(), translationPath, nlsType.getTranslationsPrefix())) {
        translationFiles.add(new WorkspaceTranslationFile(file));
      }
    }
    return translationFiles;
  }

  private static List<ITranslationResource> loadTranslationFilesFromPlatform(NlsType nlsType, IPackageFragmentRoot r) throws CoreException {
    List<ITranslationResource> translationFiles = new ArrayList<>();
    char delim = '.';
    String path = nlsType.getTranslationsFolderName().replace(NlsType.FOLDER_SEGMENT_SEPARATOR, delim);
    String d = "" + delim;
    if (path.startsWith(d)) {
      path = path.substring(d.length());
    }

    IPackageFragment textFolder = r.getPackageFragment(path);
    if (textFolder == null) {
      NlsCore.logWarning("Folder '" + nlsType.getTranslationsFolderName() + "' could not be found in '" + r.getElementName() + "'. Will be ignored.");
    }
    else {
      for (Object o : textFolder.getNonJavaResources()) {
        if (o instanceof JarEntryFile) {
          JarEntryFile f = (JarEntryFile) o;
          try (InputStream is = f.getContents()) {
            translationFiles.add(new PlatformTranslationFile(is, getLanguage(f.getName())));
          }
          catch (Exception e) {
            NlsCore.logError("Could not load NLS files of bundle '" + r.getElementName() + "'.", e);
          }
        }
      }
    }
    return translationFiles;
  }

  private void createTranslationFile(Language language, INlsFolder folder, IProgressMonitor monitor) throws CoreException {
    String fileName = getLocalizedPropertiesFileName(getNlsType().getTranslationsPrefix(), language);
    IFile file = folder.getFolder().getFile(new Path(fileName));
    if (!file.exists()) {
      file.create(new ByteArrayInputStream("".getBytes()), true, monitor);
    }
    addTranslationResource(new WorkspaceTranslationFile(file), monitor);
  }

  @Override
  public INewLanguageContext getTranslationCreationContext() {
    return new INewLanguageContext() {
      private final TranslationFileNewModel m_model = new TranslationFileNewModel(SimpleNlsProject.this);

      @Override
      public boolean interactWithUi(Shell s) {
        TranslationFileNewDialog dialog = new TranslationFileNewDialog(s, m_model);
        return dialog.open() == Window.OK;
      }

      @Override
      public void execute(IProgressMonitor monitor) {
        try {
          createTranslationFile(m_model.getLanguage(), m_model.getFolder(), new NullProgressMonitor());
        }
        catch (CoreException e) {
          NlsCore.logError("Unable to create new language.", e);
        }
      }

      @Override
      public Object getModel() {
        return m_model;
      }
    };
  }

  private void setSuperType(IType superType) {
    // find parent
    if (superType != null) {
      try {
        setParent(NlsCore.getNlsWorkspace().getNlsProject(new Object[]{superType}));
      }
      catch (CoreException e) {
        NlsCore.logError("parent of NLS project could not be found. Looked for type '" + superType.getFullyQualifiedName() + "'", e);
      }
    }
  }

  public NlsType getNlsType() {
    return m_nlsClass;
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

  public static Language getLanguage(String simpleFileName) {
    Matcher matcher = Pattern.compile("^[^_.]*(_([A-Za-z]{2}))?(_([A-Za-z]{2}))?(_([A-Za-z]{2}))?\\.properties$").matcher(simpleFileName);
    if (matcher.matches()) {
      if (matcher.group(2) == null) {
        // default
        return Language.LANGUAGE_DEFAULT;
      }
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
    return null;
  }

  public static String getLocalizedPropertiesFileName(String prefix, Language language) {
    String fileName = prefix;
    Locale locale = language.getLocale();
    fileName = fileName + "_" + locale.toString() + ".properties";
    return fileName;
  }

  private class P_NlsClassPropertyChangeListener implements PropertyChangeListener, WeakEventListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (NlsType.PROP_TRANSLATION_FILE_PREFIX.equals(evt.getPropertyName()) || NlsType.PROP_TRANSLATION_FOLDER_NAME.equals(evt.getPropertyName())) {
        refresh();
      }
      else if (NlsType.PROP_SUPER_TYPE.equals(evt.getPropertyName())) {
        resetCache();
        setSuperType(getNlsType().getSuperType());
        fireNlsProjectEvent(new NlsProjectEvent(SimpleNlsProject.this, NlsProjectEvent.TYPE_FULL_REFRESH));
      }
    }
  } // end class P_NlsClassPropertyChangeListener
}