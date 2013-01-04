/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ErrorDialog;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ElementBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ResourceSelectionWizardPage;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class PhantomJarFilesDeleteWizard extends AbstractWorkspaceWizard {

  private ResourceSelectionWizardPage m_wizardPage;
  private IScoutBundle m_bundle;
  private IFile[] m_phantomJarFiles;
  private IFile[] m_phantomJarFilesToBeDeleted;

  public PhantomJarFilesDeleteWizard(IScoutBundle bundle, IFile[] phantomJarFiles) {
    setWindowTitle(Texts.get("CleanupPhantomStubFiles"));
    m_bundle = bundle;
    m_phantomJarFiles = phantomJarFiles;
  }

  @Override
  public void addPages() {
    m_wizardPage = new ResourceSelectionWizardPage(Texts.get("CleanupPhantomStubFiles"), Texts.get("QuestionDeletion"));
    m_wizardPage.setElements(getPhantomElements());
    addPage(m_wizardPage);
  }

  private List<ElementBean> getPhantomElements() {
    List<ElementBean> elements = new LinkedList<ElementBean>();
    for (IFile file : m_phantomJarFiles) {
      elements.add(new ElementBean(0, file.getFullPath().toString(), JaxWsSdk.getImageDescriptor(JaxWsIcons.Jar), file, false));
    }
    return elements;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    List<IFile> filesToBeDeleted = new LinkedList<IFile>();
    for (ElementBean candidate : m_wizardPage.getElements()) {
      if (candidate.isChecked() || candidate.isMandatory()) {
        filesToBeDeleted.add((IFile) candidate.getResource());
      }
    }

    m_phantomJarFilesToBeDeleted = filesToBeDeleted.toArray(new IFile[filesToBeDeleted.size()]);
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    for (final IFile jarFile : m_phantomJarFilesToBeDeleted) {
      try {
        jarFile.delete(true, true, monitor);
        JaxWsSdkUtility.registerJarLib(m_bundle, jarFile, true, monitor);
      }
      catch (final Exception e) {
        ScoutSdkUi.getDisplay().asyncExec(new Runnable() {

          @Override
          public void run() {
            ErrorDialog dialog = new ErrorDialog(Texts.get("FailedToDeleteFile"));
            dialog.setError(Texts.get("FailedToDeleteFileX", jarFile.getProjectRelativePath().toString()), e);
            dialog.open();
          }
        });
        return false;
      }
    }
    return true;
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }
}
