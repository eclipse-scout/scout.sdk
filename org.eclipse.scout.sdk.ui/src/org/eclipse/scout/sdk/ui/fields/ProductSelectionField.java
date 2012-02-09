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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.sdk.ui.dialog.ProductSelectionDialog;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.SdkProperties;
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
public class ProductSelectionField extends TextField {

  private Button m_popupButton;

  private IFile m_productFile;
  private EventListenerList m_eventListeners;
  private OptimisticLock m_inputLock = new OptimisticLock();

  private final ITreeNode m_productTreeRoot;

  public ProductSelectionField(Composite parent, ITreeNode productTreeRoot) {
    super(parent);
    m_productTreeRoot = productTreeRoot;
    m_eventListeners = new EventListenerList();
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
    m_popupButton = new Button(parent, SWT.PUSH);
    m_popupButton.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ToolDropdown));
    m_popupButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        ProductSelectionDialog dialog = new ProductSelectionDialog(getShell(), getProductTreeRoot());
        dialog.setMultiSelectionMode(false);
        dialog.setProductSelectionRequired(true);
        if (dialog.open() == Dialog.OK) {
          IFile newFile = dialog.getSelectedProductFile();
          if (!CompareUtility.equals(newFile, getProductFile())) {
            if (m_inputLock.acquire()) {
              try {
                String representationStr = "";
                if (newFile != null)
                {
                  representationStr = newFile.getProject().getName() + "/" + newFile.getProjectRelativePath();
                }
                getTextComponent().setText(representationStr);
              }
              finally {
                m_inputLock.release();
              }
            }
            setProductFileInternal(newFile);
          }
        }
      }
    });

    text.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          if (m_inputLock.acquire()) {
            // try to find product
            String input = getText();
            Path p = new Path(input);
            if (p.segmentCount() > 1) {
              IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(p);
              if (file.exists()) {
                setProductFileInternal(file);
                return;
              }
            }
            setProductFileInternal(null);
          }
        }
        catch (Exception ex) {
          ex.printStackTrace();
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
    labelData.top = new FormAttachment(0, 0);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(40, 0);
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

  /**
   * @return the productTreeRoot
   */
  public ITreeNode getProductTreeRoot() {
    return m_productTreeRoot;
  }

  public void addProductSelectionListener(IProductSelectionListener listener) {
    m_eventListeners.add(IProductSelectionListener.class, listener);
  }

  public void removeProductSelectionListener(IProductSelectionListener listener) {
    m_eventListeners.remove(IProductSelectionListener.class, listener);
  }

  private void fireProductSelected(IFile productFile) {
    for (IProductSelectionListener l : m_eventListeners.getListeners(IProductSelectionListener.class)) {
      try {
        l.productSelected(productFile);
      }
      catch (Throwable t) {
        ScoutSdkUi.logError("error during listener notification.", t);
      }
    }
  }

  public void setProductFile(IFile productFile) {
    m_productFile = productFile;
    if (!isDisposed()) {
      String text = "";
      if (productFile != null) {
        text = productFile.getProject().getName() + "/" + productFile.getProjectRelativePath();
      }
      try {
        if (m_inputLock.acquire()) {
          getTextComponent().setText(text);
        }
      }
      finally {
        m_inputLock.release();
      }
    }
  }

  private void setProductFileInternal(IFile productFile) {
    if (!CompareUtility.equals(m_productFile, productFile)) {
      setProductFile(productFile);
      fireProductSelected(m_productFile);
    }
  }

  /**
   * @return the productFile
   */
  public IFile getProductFile() {
    return m_productFile;
  }

}
