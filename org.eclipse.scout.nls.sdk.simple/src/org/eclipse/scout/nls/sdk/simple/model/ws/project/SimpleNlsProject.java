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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.model.util.Language;
import org.eclipse.scout.nls.sdk.model.workspace.project.AbstractNlsProject;
import org.eclipse.scout.nls.sdk.model.workspace.project.NlsProjectEvent;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.AbstractTranslationResource;
import org.eclipse.scout.nls.sdk.model.workspace.translationResource.ITranslationResource;
import org.eclipse.scout.nls.sdk.simple.NlsSdkSimple;
import org.eclipse.scout.nls.sdk.simple.model.ws.NlsType;
import org.eclipse.scout.nls.sdk.simple.model.ws.translationfile.PlatformTranslationFile;
import org.eclipse.scout.nls.sdk.simple.model.ws.translationfile.WorkspaceTranslationFile;
import org.eclipse.scout.nls.sdk.simple.ui.dialog.language.TranslationFileNewDialog;
import org.eclipse.scout.nls.sdk.simple.ui.dialog.language.TranslationFileNewModel;
import org.eclipse.scout.nls.sdk.ui.action.INewLanguageContext;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
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
      // find bundle
      String bundleId = getPluginId(getNlsType().getType());
      if (bundleId == null) {
        NlsCore.logWarning("could not find bundle: " + bundleId);
        return new ITranslationResource[]{};
      }
      return loadTranslationFilesFromPlatform(getNlsType(), bundleId);

    }
    else {
      return loadTranslationFilesWorkspace(getNlsType());
    }
  }

  private String getPluginId(IJavaElement type) {
    if (type.isReadOnly()) {
      IJavaElement root = type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
      String bundleId = null;
      if (root != null) {
        Matcher matcher = Pattern.compile("^([^_]*)_[0-9]+\\.[0-9]+\\.[0-9]+(\\.[^.]+)?\\.jar$").matcher(root.getElementName());
        if (matcher.matches()) {
          bundleId = matcher.group(1);
        }
        else if ("bin".equals(root.getElementName())) {
          // special case when debugging
          bundleId = root.getPath().toFile().getParentFile().getName();
        }
      }
      return bundleId;
    }
    else {
      return type.getAncestor(IJavaElement.JAVA_PROJECT).getElementName();
    }
  }

  private ITranslationResource[] loadTranslationFilesWorkspace(NlsType nlsType) throws CoreException {
    // try to find all translation files
    ArrayList<ITranslationResource> translationFiles = new ArrayList<ITranslationResource>();
    if (nlsType != null && nlsType.getTranslationsFolderName() != null && nlsType.getTranslationsPrefix() != null) {
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

  @SuppressWarnings("unchecked")
private ITranslationResource[] loadTranslationFilesFromPlatform(NlsType nlsType, String bundleId) throws CoreException {
    ArrayList<ITranslationResource> translationFiles = new ArrayList<ITranslationResource>();
    Enumeration<Object> eee = Platform.getBundle(bundleId).findEntries(nlsType.getTranslationsFolderName(), nlsType.getTranslationsPrefix() + "*.properties", false);
    if (eee != null) {
      while (eee.hasMoreElements()) {
        Object o = eee.nextElement();
        if (o instanceof URL) {
          try {
            translationFiles.add(new PlatformTranslationFile(((URL) o).openStream(), NlsSdkSimple.getLanguage(((URL) o).getFile())));
          }
          catch (IOException e) {
            NlsCore.logError("could not load NLS files of bundle '" + bundleId + "'", e);
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
    };
  }

  private void setSuperType(IType superType) {
    // find parent
    if (superType != null) {
      try {
        setParent(NlsCore.getNlsWorkspace().getNlsProject(new Object[]{superType}));
      }
      catch (CoreException e) {
        NlsCore.logError("parent of NLS project could not be found. Looked for type '" + superType.getFullyQualifiedName() + "'");
      }
    }
  }

  public NlsType getNlsType() {
    return m_nlsClass;
  }

  private class P_NlsClassPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (NlsType.PROP_TRANSLATION_FILE_PREFIX.equals(evt.getPropertyName()) ||
          NlsType.PROP_TRANSLATION_FOLDER_NAME.equals(evt.getPropertyName())) {
        // reset cache
        resetCache();
        // find files
        updateTranslationResourceLocation();
        // fire full reload
        fireNlsProjectEvent(new NlsProjectEvent(SimpleNlsProject.this, NlsProjectEvent.TYPE_FULL_REFRESH));
      }
      else if (NlsType.PROP_SUPER_TYPE.equals(evt.getPropertyName())) {
        resetCache();
        setSuperType(getNlsType().getSuperType());
        fireNlsProjectEvent(new NlsProjectEvent(SimpleNlsProject.this, NlsProjectEvent.TYPE_FULL_REFRESH));
      }
    }

  } // end class P_NlsClassPropertyChangeListener
}
