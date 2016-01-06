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
package org.eclipse.scout.sdk.s2e.nls.internal.ui.editor;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

public class NlsEditor extends MultiPageEditorPart {

  private int m_tablePageIndex = -1;

  public static final String EDITOR_ID = "org.eclipse.scout.nls.sdk.nlsEditor";

  @Override
  public void init(IEditorSite site, IEditorInput input) throws PartInitException {
    if (input instanceof JarEntryEditorInput) {
      IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input, EditorsUI.DEFAULT_TEXT_EDITOR_ID, true);
      setSite(null);
      setInput(null);
      return;
    }

    if (input instanceof IStorageEditorInput) {
      IStorage storage = null;
      try {
        storage = ((IStorageEditorInput) input).getStorage();
      }
      catch (CoreException e) {
        SdkLog.warning(e);
      }
      if (storage instanceof JarEntryFile) {
        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input, EditorsUI.DEFAULT_TEXT_EDITOR_ID, true);
        setSite(null);
        setInput(null);
        return;
      }
    }

    setSite(site);
    setInput(input);
  }

  @Override
  protected void createPages() {
    createTablePage();
  }

  private void createTablePage() {
    INlsProject nlsProjects = null;
    IEditorInput input = getEditorInput();
    try {
      if (input instanceof FileEditorInput) {
        nlsProjects = NlsCore.getNlsWorkspace().getNlsProject(new Object[]{((FileEditorInput) input).getFile()});
      }
      else if (input instanceof NlsTypeEditorInput) {
        nlsProjects = NlsCore.getNlsWorkspace().getNlsProject(new Object[]{((NlsTypeEditorInput) input).getType()});
      }

      if (nlsProjects != null) {
        setPartName(nlsProjects.getName());
      }
      else {
        setPartName("Translations");
      }
      NlsTablePage page = new NlsTablePage(getContainer(), nlsProjects);
      m_tablePageIndex = addPage(page);
      setPageText(m_tablePageIndex, "Translations");
    }
    catch (CoreException e) {
      SdkLog.error("could not load file: {}", input.getName(), e);
    }
  }

  @Override
  public void doSaveAs() {
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public void doSave(IProgressMonitor monitor) {
  }
}
