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
package org.eclipse.scout.nls.sdk.internal.ui.dialog;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

public class ResourceDialog extends TitleAreaDialog {

  private IContainer m_root;
  private String m_dialogTitle;
  private TreeViewer m_viewer;
  private List<ViewerFilter> m_filters = new LinkedList<>();
  private IResource[] m_result;
  private IResource[] m_initialSelection;
  private IResource[] m_initialExpansion;

  public ResourceDialog(Shell parentShell, String title, IContainer root) {
    super(parentShell);
    setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE | getDefaultOrientation());
    m_dialogTitle = title;
    m_root = root;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_dialogTitle);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite rootArea = new Composite(parent, SWT.NONE);
    Tree tree = new Tree(rootArea, SWT.SINGLE);
    m_viewer = new TreeViewer(tree);
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        StructuredSelection selection = (StructuredSelection) event.getSelection();
        if (!selection.isEmpty()) {
          Button ok = getButton(Window.OK);
          if (ok != null && !ok.isDisposed()) {
            ok.setEnabled(true);
          }
          m_result = new IResource[selection.size()];
          int i = 0;
          for (Iterator<?> it = selection.iterator(); it.hasNext(); i++) {
            Object o = it.next();
            if (o instanceof IResource) {
              m_result[i] = (IResource) o;
            }
          }
        }
      }
    });
    P_TreeContentProvider model = new P_TreeContentProvider();

    m_viewer.setLabelProvider(model);
    m_viewer.setContentProvider(model);
    m_viewer.setInput(model);
    for (ViewerFilter filter : m_filters) {
      m_viewer.addFilter(filter);
    }
    if (m_initialSelection != null) {
      m_viewer.setSelection(new StructuredSelection(m_initialSelection));
    }
    if (m_initialExpansion != null) {
      m_viewer.setExpandedElements(m_initialExpansion);
    }

    // layout
    GridData data = new GridData();
    data.horizontalAlignment = SWT.FILL;
    data.grabExcessHorizontalSpace = true;
    rootArea.setLayoutData(data);
    rootArea.setLayout(new FormLayout());
    FormData fdata = new FormData(SWT.DEFAULT, 200);
    fdata.top = new FormAttachment(0, 5);
    fdata.left = new FormAttachment(0, 5);
    fdata.right = new FormAttachment(100, -5);
    fdata.bottom = new FormAttachment(100, -5);
    tree.setLayoutData(fdata);
    return rootArea;
  }

  public void setInitialSelection(IResource[] resource) {
    m_initialSelection = resource;
  }

  public void setInitialExpansion(IResource[] resources) {
    m_initialExpansion = resources;
  }

  public void addViewerFilter(ViewerFilter filter) {
    m_filters.add(filter);
  }

  public void removeViewerFilter(ViewerFilter filter) {
    m_filters.remove(filter);
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    super.createButtonsForButtonBar(parent);
    getButton(Dialog.OK).setEnabled(false);
    getButton(Dialog.CANCEL).setEnabled(true);
  }

  public IResource getFirstResult() {
    if (m_result != null && m_result.length > 0) {
      return m_result[0];
    }
    return null;
  }

  private class P_TreeContentProvider implements ITreeContentProvider, ILabelProvider {

    @Override
    public Object[] getElements(Object inputElement) {
      return new Object[]{m_root};
    }

    @Override
    public Object[] getChildren(Object parentElement) {
      try {
        if (parentElement instanceof IContainer) {
          return ((IContainer) parentElement).members();
        }
        else {
          return new Object[]{};
        }
      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
        return new Object[]{};
      }
    }

    @Override
    public Object getParent(Object element) {
      if (element instanceof IResource) {
        return ((IResource) element).getParent();
      }
      else {
        return null;
      }
    }

    @Override
    public boolean hasChildren(Object element) {

      try {
        return ((element instanceof IContainer) && (((IContainer) element).members().length > 0));
      }
      catch (CoreException e) {
        NlsCore.logWarning(e);
        return false;
      }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public String getText(Object element) {
      if (element instanceof IResource) {
        return ((IResource) element).getName();
      }
      return "";
    }

    @Override
    public Image getImage(Object element) {
      return null;
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }
  } // end class P_TreeContentProvider

}
