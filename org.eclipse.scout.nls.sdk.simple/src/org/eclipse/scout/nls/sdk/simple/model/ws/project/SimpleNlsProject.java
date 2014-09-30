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
package org.eclipse.scout.nls.sdk.simple.model.ws.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.WeakEventListener;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.project.AbstractNlsProject;
import org.eclipse.scout.nls.sdk.model.workspace.project.NlsProjectEvent;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.AbstractTranslationResource;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.ITranslationResource;
import org.eclipse.scout.nls.sdk.simple.internal.NlsSdkSimple;
import org.eclipse.scout.nls.sdk.simple.model.ws.NlsType;
import org.eclipse.scout.nls.sdk.simple.model.ws.translationfile.PlatformTranslationFile;
import org.eclipse.scout.nls.sdk.simple.model.ws.translationfile.WorkspaceTranslationFile;
import org.eclipse.scout.nls.sdk.simple.ui.dialog.language.TranslationFileNewDialog;
import org.eclipse.scout.nls.sdk.simple.ui.dialog.language.TranslationFileNewModel;
import org.eclipse.scout.nls.sdk.ui.action.INewLanguageContext;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * nls project that can handle properties files.
 */
@SuppressWarnings("restriction")
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
  public ITranslationResource[] loadTranslationResources() throws CoreException {
    if (getNlsType().getType().isReadOnly()) {
      IPackageFragmentRoot r = (IPackageFragmentRoot) getNlsType().getType().getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
      if (r == null) {
        NlsCore.logWarning("Could not find text resource for type '" + getNlsType().getType().getFullyQualifiedName() + "'.");
        return new ITranslationResource[]{};
      }
      return loadTranslationFilesFromPlatform(getNlsType(), r);
    }
    else {
      return loadTranslationFilesWorkspace(getNlsType());
    }
  }

  private ITranslationResource[] loadTranslationFilesWorkspace(NlsType nlsType) throws CoreException {
    // try to find all translation files
    ArrayList<ITranslationResource> translationFiles = new ArrayList<ITranslationResource>();
    if (nlsType != null && TypeUtility.exists(nlsType.getType()) && nlsType.getTranslationsFolderName() != null && nlsType.getTranslationsPrefix() != null) {
      Path translationPath = new Path(nlsType.getTranslationsFolderName());
      for (IFile file : NlsSdkSimple.getAllTranslations(nlsType.getJavaProject().getProject(), translationPath, nlsType.getTranslationsPrefix())) {
        translationFiles.add(new WorkspaceTranslationFile(file));
      }
      List<IProject> workspaceFragments = NlsSdkSimple.getWorkspaceFragments(nlsType.getHostPluginId());
      for (IProject fragment : workspaceFragments) {
        for (IFile file : NlsSdkSimple.getAllTranslations(fragment, translationPath, nlsType.getTranslationsPrefix())) {
          translationFiles.add(new WorkspaceTranslationFile(file));
        }
      }
    }
    return translationFiles.toArray(new ITranslationResource[translationFiles.size()]);
  }

  private ITranslationResource[] loadTranslationFilesFromPlatform(NlsType nlsType, IPackageFragmentRoot r) throws CoreException {
    ArrayList<ITranslationResource> translationFiles = new ArrayList<ITranslationResource>();
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
          InputStream is = null;
          try {
            is = f.getContents();
            translationFiles.add(new PlatformTranslationFile(is, NlsSdkSimple.getLanguage(f.getName())));
          }
          catch (Exception e) {
            NlsCore.logError("Could not load NLS files of bundle '" + r.getElementName() + "'.", e);
          }
          finally {
            if (is != null) {
              try {
                is.close();
              }
              catch (Exception e) {
              }
            }
          }
        }
      }
    }
    return translationFiles.toArray(new AbstractTranslationResource[translationFiles.size()]);
  }

  private void createTranslationFile(Language language, INlsFolder folder, IProgressMonitor monitor) throws CoreException {
    String fileName = NlsSdkSimple.getLocalizedPropertiesFileName(getNlsType().getTranslationsPrefix(), language);
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
        return dialog.open() == Dialog.OK;
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

  private class P_NlsClassPropertyChangeListener implements PropertyChangeListener, WeakEventListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (NlsType.PROP_TRANSLATION_FILE_PREFIX.equals(evt.getPropertyName())
          || NlsType.PROP_TRANSLATION_FOLDER_NAME.equals(evt.getPropertyName())) {
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
