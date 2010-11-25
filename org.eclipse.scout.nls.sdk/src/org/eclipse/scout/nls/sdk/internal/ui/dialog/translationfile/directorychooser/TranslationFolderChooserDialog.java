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
package org.eclipse.scout.nls.sdk.internal.ui.dialog.translationfile.directorychooser;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.fields.IInputChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

public class TranslationFolderChooserDialog extends Dialog {

  private String m_title;
  private Tree m_tree;
  private TreeViewer m_viewer;
  private NlsTranlationFolderChooserDialogModel m_model;
  private IProject m_project;
  private IFolder m_selection;
  private List<IInputChangedListener<IFolder>> m_listeners = new LinkedList<IInputChangedListener<IFolder>>();

  public TranslationFolderChooserDialog(Shell parentShell, String title, IProject project) {
    super(parentShell);
    m_title = title;
    m_project = project;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  @Override
  protected Control createDialogArea(Composite parent) {

    Composite rootPane = new Composite(parent, SWT.NONE);
    m_tree = new Tree(rootPane, SWT.SINGLE);
    m_viewer = new TreeViewer(m_tree);
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        StructuredSelection selection = (StructuredSelection) event.getSelection();
        if (selection != null && selection.getFirstElement() instanceof CompareableFolder) {
          handleSelectionChanged((StructuredSelection) event.getSelection());
        }
      }
    });
    try {
      setModel(new NlsTranlationFolderChooserDialogModel(NlsCore.getProjectGroup(m_project)));
    }
    catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      NlsCore.logWarning(e);
    }
    catch (CoreException e) {
      // TODO Auto-generated catch block
      NlsCore.logWarning(e);
    }

    // layout

    rootPane.setLayout(new GridLayout(1, true));
    m_tree.setLayoutData(new GridData(300, 300));
    return rootPane;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    super.createButtonsForButtonBar(parent);
    getButton(IDialogConstants.OK_ID).setEnabled(false);
  }

  public IFolder openDialog() {
    if (super.open() == Dialog.OK) {
      return m_selection;
    }
    return null;
  }

  private void setModel(NlsTranlationFolderChooserDialogModel model) {
    m_model = model;
    m_viewer.setContentProvider(m_model);
    m_viewer.setLabelProvider(m_model);
    m_viewer.setInput(m_model);
  }

  public void addInputChangeListener(IInputChangedListener<IFolder> listener) {
    m_listeners.add(listener);
  }

  public void removeInputChangeListener(IInputChangedListener<IFolder> listener) {
    m_listeners.remove(listener);
  }

  private void handleSelectionChanged(StructuredSelection selection) {
    if (selection.getFirstElement() != null) {
      IFolder path = ((CompareableFolder) selection.getFirstElement()).getFolder();
      getButton(IDialogConstants.OK_ID).setEnabled(true);
      m_selection = path;
      for (IInputChangedListener<IFolder> listener : m_listeners) {
        listener.inputChanged(path);
      }
    }
    else {
      getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
  }

}
