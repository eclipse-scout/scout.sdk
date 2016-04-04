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
package org.eclipse.scout.sdk.s2e.ui.fields.file;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.EventListenerList;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.util.OptimisticLock;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

/**
 * <h3>FileSelectionField</h3> For selecting folders or files on the local disk.
 *
 * @author Andreas Hoegger
 * @since 5.1.0
 * @see IFileSelectionListener
 */
public class FileSelectionField extends TextField {

  private Button m_popupButton;
  private File m_file;

  private boolean m_folderMode;
  private String[] m_filterExtensions;
  private String m_fileName;

  private final EventListenerList m_eventListeners;
  private final OptimisticLock m_inputLock;

  public FileSelectionField(Composite parent) {
    this(parent, DEFAULT_LABEL_PERCENTAGE);
  }

  public FileSelectionField(Composite parent, int labelPercentage) {
    super(parent, labelPercentage);
    m_inputLock = new OptimisticLock();
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
    m_popupButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_popupButton.setText("Browse...");
    m_popupButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showFileChooserDialog();
      }
    });

    text.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        try {
          if (m_inputLock.acquire()) {
            String input = getText();
            if (StringUtils.isBlank(input)) {
              setFileInternal(null);
            }
            else {
              File newFile = new File(input);

              // validate path
              try {
                newFile.getCanonicalPath(); // fails on invalid path
                setFileInternal(newFile);
              }
              catch (Exception ex) {
                // the supplied string is no valid path for this OS.
                setFileInternal(null);
              }
            }
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

    FormData buttonData = new FormData();
    buttonData.width = 70;
    buttonData.top = new FormAttachment(0, 0);
    buttonData.right = new FormAttachment(100, 0);
    buttonData.bottom = new FormAttachment(100, 0);
    m_popupButton.setLayoutData(buttonData);
  }

  private void showFileChooserDialog() {
    String fileName = null;
    if (isFolderMode()) {
      DirectoryDialog dialog = new DirectoryDialog(getShell());
      if (getFile() != null) {
        dialog.setFilterPath(getFile().getAbsolutePath());
      }
      fileName = dialog.open();
    }
    else {
      FileDialog dialog = new FileDialog(getShell());
      if (getFileName() != null) {
        dialog.setFileName(getFileName());
      }
      dialog.setOverwrite(true);
      if (getFilterExtensions() != null) {
        dialog.setFilterExtensions(getFilterExtensions());
      }
      fileName = dialog.open();
      if (StringUtils.isNotEmpty(fileName) && getFilterExtensions() != null && getFilterExtensions().length > 0) {
        int extIndex = dialog.getFilterIndex();
        Matcher m = Pattern.compile("\\.([^\\.]*)$").matcher(fileName);
        String extension = null;
        if (m.find()) {
          for (String fExt : getFilterExtensions()) {
            if (StringUtils.equalsIgnoreCase(fExt, "*." + m.group(1))) {
              extension = m.group(1);
              break;
            }
          }
        }
        if (extension == null && extIndex > -1 && extIndex < getFilterExtensions().length) {
          extension = getFilterExtensions()[extIndex];
          extension = extension.replaceFirst("\\**", "");
          fileName = fileName + extension;
        }
      }
    }
    File newFile = null;
    if (StringUtils.isNotEmpty(fileName)) {
      newFile = new File(fileName);
      try {
        if (m_inputLock.acquire()) {
          getTextComponent().setText(newFile.getAbsolutePath());
        }
      }
      finally {
        m_inputLock.release();
      }
    }
    setFileInternal(newFile);

  }

  public void addFileSelectionListener(IFileSelectionListener listener) {
    m_eventListeners.add(IFileSelectionListener.class, listener);
  }

  public void removeFileSelectionListener(IFileSelectionListener listener) {
    m_eventListeners.remove(IFileSelectionListener.class, listener);
  }

  @Override
  public void addTraverseListener(TraverseListener listener) {
    getTextComponent().addTraverseListener(listener);
  }

  @Override
  public void removeTraverseListener(TraverseListener listener) {
    getTextComponent().removeTraverseListener(listener);
  }

  private void fireFileSelected(File file) {
    for (IFileSelectionListener l : m_eventListeners.getListeners(IFileSelectionListener.class)) {
      try {
        l.fileSelected(file);
      }
      catch (Exception t) {
        SdkLog.error("error during listener notification.", t);
      }
    }
  }

  public boolean isFolderMode() {
    return m_folderMode;
  }

  public void setFolderMode(boolean folderMode) {
    m_folderMode = folderMode;
  }

  /**
   * @param fileName
   *          the fileName to set
   */
  public void setFileName(String fileName) {
    m_fileName = fileName;
  }

  /**
   * @return the fileName
   */
  public String getFileName() {
    return m_fileName;
  }

  /**
   * @param filterExtensions
   *          the filterExtensions to set
   */
  public void setFilterExtensions(String[] filterExtensions) {
    m_filterExtensions = filterExtensions;
  }

  /**
   * @return the filterExtensions
   */
  public String[] getFilterExtensions() {
    return m_filterExtensions;
  }

  /**
   * @return the file
   */
  public File getFile() {
    return m_file;
  }

  /**
   * @param file
   *          the file to set
   */
  public void setFile(File file) {
    m_file = file;
    if (!isDisposed()) {
      String text = "";
      if (file != null) {
        text = file.getAbsolutePath();
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

  private void setFileInternal(File file) {
    if (!Objects.equals(getFile(), file)) {
      setFile(file);
      fireFileSelected(file);
    }
  }
}
