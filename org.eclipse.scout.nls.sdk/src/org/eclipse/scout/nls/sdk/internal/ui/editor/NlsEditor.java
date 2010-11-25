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
package org.eclipse.scout.nls.sdk.internal.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.workspace.project.NlsProject;
import org.eclipse.scout.nls.sdk.ui.editor.INlsEditor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

public class NlsEditor extends MultiPageEditorPart implements INlsEditor {

  private int m_tablePageIndex = -1;

  @Override
  public void init(IEditorSite site, IEditorInput input) throws PartInitException {
    if (input instanceof JarEntryEditorInput) {
      // JarEntryEditorInput jarInput = (JarEntryEditorInput)input;
      // IStorage storage = jarInput.getStorage();
      // try {
      // InputStream stram = storage.getContents();
      // } catch (CoreException e) {
      // // TODO Auto-generated catch block
      // NlsCore.logWarning(e);
      // }
      IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input, EditorsUI.DEFAULT_TEXT_EDITOR_ID
          , true);
      setSite(null);
      setInput(null);
      return;
    }
    IJavaElement elem = JavaUI.getEditorInputJavaElement(input);
    if (input instanceof IStorageEditorInput) {
      IStorage storage = null;
      try {
        storage = ((IStorageEditorInput) input).getStorage();
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        NlsCore.logWarning(e);
      }
      if (storage instanceof JarEntryFile) {
        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input, EditorsUI.DEFAULT_TEXT_EDITOR_ID
            , true);
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
    // createOptionsPage();
  }

  private void createTablePage() {
    // Composite comp = m_group.createComponent(getContainer(),getSite());

    NlsProject nlsProject = null;
    IEditorInput input = getEditorInput();
    try {

      if (input instanceof FileEditorInput) {
        IFile file = ((FileEditorInput) input).getFile();
        if (PluginRegistry.findModel(file.getProject()) != null) {
          nlsProject = NlsCore.getNlsWorkspace().findNlsProject(file, new NullProgressMonitor());

          setPartName(nlsProject.getName());
          NlsTablePage page = new NlsTablePage(getContainer(), nlsProject, file);
          m_tablePageIndex = addPage(page);
          setPageText(m_tablePageIndex, "Translations");
        }
      }
    }
    catch (CoreException e) {

      NlsCore.logError("could not load file: " + input.getName(), e);
    }

  }

  // private void createOptionsPage(){
  // m_optionsModel = new NlsOptionModel(m_nlsProject);
  // m_optionsModel.startChangeLog();
  // m_controlPage = new NlsControlPage(getContainer(),m_optionsModel);
  // m_controlPageIndex = addPage(m_controlPage);
  // setPageText(m_controlPageIndex,"Options");
  // }

  @Override
  protected void pageChange(int newPageIndex) {
    // if(m_activePageIndex == m_controlPageIndex && m_optionsModel.hasChanges()){
    // MessageDialog dialog = new MessageDialog(getContainer().getShell(),"Save changes",getContainer().getDisplay().getSystemImage(SWT.ICON_QUESTION),
    // "Save changes?",MessageDialog.QUESTION,new String[]{"&Yes","&No"},0);
    // if(dialog.open() == 0){
    // m_controlPage.saveChanges();
    // }
    // m_optionsModel.commitChanges(null);
    // }
    super.pageChange(newPageIndex);
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
