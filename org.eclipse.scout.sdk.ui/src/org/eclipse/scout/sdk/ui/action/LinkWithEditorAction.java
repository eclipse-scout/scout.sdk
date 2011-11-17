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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.TuningUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.EditorSelectionVisitor;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;

/**
 * <h3>LinkWithEditorAction</h3> This action is used for enable or disable the selection in the compilation unit
 * editor to the bsi case outline.
 */
public class LinkWithEditorAction extends Action {
  private static final long BRIEF_DELAY = 100;
  private P_PropertyListener m_propertyListener;
  private P_JavaEditorSelectionListener m_javaEditorSelectionListener;
  private final ScoutExplorerPart m_viewPart;
  private P_IcuSelection m_currentIcuSelection;
  private P_UpdateSelectionJob m_updateSelectionJob;

  public LinkWithEditorAction(ScoutExplorerPart viewPart) {
    super(null, AS_CHECK_BOX);
    m_viewPart = viewPart;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolSynchronize));
    setToolTipText(Texts.get("LinkWithEditor"));
    m_updateSelectionJob = new P_UpdateSelectionJob();
    init();
  }

  protected void init() {
    if (m_propertyListener == null) {
      m_propertyListener = new P_PropertyListener();
    }
    m_viewPart.addPropertyListener(m_propertyListener);
    // java file selection listener
    if (m_javaEditorSelectionListener == null) {
      m_javaEditorSelectionListener = new P_JavaEditorSelectionListener();
    }
    m_viewPart.getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(m_javaEditorSelectionListener);
  }

  public void dispose() {
    m_viewPart.removePropertyListener(m_propertyListener);
    m_propertyListener = null;
  }

  @Override
  public void run() {
    m_viewPart.setLinkingEnabled(isChecked());
  }

  public P_IcuSelection getCurrentIcuSelection() {
    return m_currentIcuSelection;
  }

  public void setCurrentIcuSelection(P_IcuSelection currentIcuSelection) {
    m_currentIcuSelection = currentIcuSelection;
    if (isChecked()) {
      m_updateSelectionJob.schedule(BRIEF_DELAY);
    }
  }

  public void updateLinkingEnabled(boolean linkingEnabled) {
    setChecked(linkingEnabled);
    if (linkingEnabled) {
      m_updateSelectionJob.schedule(BRIEF_DELAY);
    }
  }

  private class P_PropertyListener implements IPropertyListener {
    @Override
    public void propertyChanged(Object aSource, int propId) {
      switch (propId) {
        case ScoutExplorerPart.IS_LINKING_ENABLED_PROPERTY:
          updateLinkingEnabled(((ScoutExplorerPart) aSource).isLinkingEnabled());
      }

    }
  } // end class P_PropertyListener

  private class P_JavaEditorSelectionListener implements ISelectionListener {
    private ISelection m_lastSelection;

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
      if (JavaUI.ID_CU_EDITOR.equals(part.getSite().getId())) {
        if (part instanceof IEditorPart) {
          IEditorInput editorInput = ((IEditorPart) part).getEditorInput();
          if (editorInput instanceof IFileEditorInput && selection instanceof ITextSelection) {
            if (CompareUtility.equals(m_lastSelection, selection)) {
              return;
            }
            m_lastSelection = selection;
            setCurrentIcuSelection(new P_IcuSelection(((IFileEditorInput) editorInput).getFile(), (ITextSelection) selection));
          }
        }
      }
    }
  }// end class P_JavaEditorSelectionListener

  private class P_IcuSelection {
    private final ITextSelection m_selection;
    private final IFile m_javaFile;

    public P_IcuSelection(IFile javaFile, ITextSelection selection) {
      m_javaFile = javaFile;
      m_selection = selection;
    }

    public ITextSelection getSelection() {
      return m_selection;
    }

    public IFile getJavaFile() {
      return m_javaFile;
    }
  } // end class P_IcuSelection

  private class P_UpdateSelectionJob extends UIJob {

    public P_UpdateSelectionJob() {
      super("Link with outline...");
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
      if (!m_viewPart.getTreeViewer().getTree().isDisposed() && m_currentIcuSelection != null) {
        SafeRunner.run(new ISafeRunnable() {
          @Override
          public void run() throws Exception {
            IPage startPage = m_viewPart.getRoot();
            IStructuredSelection selection = (IStructuredSelection) m_viewPart.getTreeViewer().getSelection();
            if (selection != null && selection.size() > 0) {
              startPage = (IPage) selection.getFirstElement();
            }
            IJavaElement element = JdtUtility.findJavaElement(m_currentIcuSelection.getJavaFile(), m_currentIcuSelection.getSelection());
            EditorSelectionVisitor visitor = new EditorSelectionVisitor(element);
            IPage nodeToSelect = null;
            try {
              nodeToSelect = visitor.findPageToSelect(startPage);
            }
            finally {
              TuningUtility.stopTimer("visit nodes to find selection");
            }
            if (nodeToSelect != null) {
              StructuredSelection outlineSelection = new StructuredSelection(nodeToSelect);
              m_viewPart.getSite().getSelectionProvider().setSelection(outlineSelection);
            }
          }

          @Override
          public void handleException(Throwable e) {
            ScoutSdkUi.logError(e);
          }
        });
      }
      return Status.OK_STATUS;
    }
  }
}
