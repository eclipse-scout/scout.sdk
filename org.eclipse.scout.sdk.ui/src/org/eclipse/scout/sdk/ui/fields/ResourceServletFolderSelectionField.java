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
package org.eclipse.scout.sdk.ui.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.sdk.ui.dialog.CheckableTreeSelectionDialog;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>ProposalTextField</h3> ...
 */
public class ResourceServletFolderSelectionField extends TextField {

  private Button m_popupButton;
  private IFolder m_folder;

  private final EventListenerList m_eventListeners;
  private final OptimisticLock m_inputLock = new OptimisticLock();
  private final ITreeNode m_rootNode;

  public ResourceServletFolderSelectionField(Composite parent, IScoutBundle scoutProject) {
    super(parent);
    m_eventListeners = new EventListenerList();
    m_rootNode = new ResourceServletFolderTree(scoutProject).getRootNode();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    m_popupButton.setEnabled(enabled);
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    Label label = getLabelComponent();
    StyledText text = getTextComponent();
    m_popupButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_popupButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolDropdown));
    m_popupButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showFolderChooser();
      }
    });

    text.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          if (m_inputLock.acquire()) {
            String input = getText();
            Path p = new Path(input);
            if (p.segmentCount() > 1) {
              IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(p);
              if (folder.exists()) {
                if (TreeUtility.findNode(getRootNode(), NodeFilters.getByData(folder)) != null) {
                  setFolderInternal(folder);
                  return;
                }
              }
            }
            setFolderInternal(null);
          }
        }
        finally {
          m_inputLock.release();
        }

      }
    });
    parent.setTabList(new Control[]{text});

    // layout
    parent.setLayout(new FormLayout());
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 4);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(getLabelPercentage(), 0);
    labelData.bottom = new FormAttachment(100, 0);
    label.setLayoutData(labelData);

    FormData textData = new FormData();
    textData.top = new FormAttachment(0, 0);
    textData.left = new FormAttachment(label, 5);
    textData.right = new FormAttachment(m_popupButton, -2);
    textData.bottom = new FormAttachment(100, 0);
    text.setLayoutData(textData);

    FormData buttonData = new FormData(SdkProperties.TOOL_BUTTON_SIZE, SdkProperties.TOOL_BUTTON_SIZE);
    buttonData.top = new FormAttachment(0, 0);
    buttonData.right = new FormAttachment(100, 0);
    buttonData.bottom = new FormAttachment(100, 0);
    m_popupButton.setLayoutData(buttonData);
  }

  private void showFolderChooser() {
    CheckableTreeSelectionDialog dialog = new CheckableTreeSelectionDialog(getShell(), m_rootNode);
    dialog.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (CheckableTreeSelectionDialog.PROP_SELECTED_NODE.equals(evt.getPropertyName())) {
          boolean complete = false;
          if (evt.getNewValue() instanceof ITreeNode) {
            ITreeNode selNode = (ITreeNode) evt.getNewValue();
            complete = selNode.getType() == ResourceServletFolderTree.NODE_TYPE_FOLDER;
          }
          ((CheckableTreeSelectionDialog) evt.getSource()).setComplete(complete);
        }
      }
    });
    if (dialog.open() == Dialog.OK) {
      ITreeNode selectedNode = dialog.getSelectedNode();
      String text = "";
      IFolder selectedFolder = null;
      if (selectedNode.getType() == ResourceServletFolderTree.NODE_TYPE_FOLDER) {
        selectedFolder = (IFolder) selectedNode.getData();
        text = selectedFolder.getProject().getName() + "/" + selectedFolder.getProjectRelativePath();
      }
      setFolderInternal(selectedFolder);
      try {
        m_inputLock.acquire();
        getTextComponent().setText(text);
      }
      finally {
        m_inputLock.release();
      }
    }
  }

  /**
   * @return the rootNode
   */
  public ITreeNode getRootNode() {
    return m_rootNode;
  }

  public void addProductSelectionListener(IFolderSelectedListener listener) {
    m_eventListeners.add(IFolderSelectedListener.class, listener);
  }

  public void removeProductSelectionListener(IFolderSelectedListener listener) {
    m_eventListeners.remove(IFolderSelectedListener.class, listener);
  }

  private void fireProductSelected(IFolder folder) {
    for (IFolderSelectedListener l : m_eventListeners.getListeners(IFolderSelectedListener.class)) {
      l.handleFolderSelection(folder);
    }
  }

  public void setFolder(IFolder folder) {
    m_folder = folder;
    if (!isDisposed()) {
      try {
        if (m_inputLock.acquire()) {
          String text = "";
          if (folder != null) {
            text = folder.getProject().getName() + "/" + folder.getProjectRelativePath();
          }
          getTextComponent().setText(text);
        }
      }
      finally {
        m_inputLock.release();
      }
    }
  }

  private void setFolderInternal(IFolder folder) {
    if (!CompareUtility.equals(m_folder, folder)) {
      setFolder(folder);
      fireProductSelected(folder);
    }
  }

  /**
   * @return the folder
   */
  public IFolder getFolder() {
    return m_folder;
  }

}
