/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.fields.resource;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.swing.event.EventListenerList;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.util.OptimisticLock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;

/**
 * <h3>ResourceTextField</h3> For selecting folders and files on the local file system or URLs in general.
 *
 * @since 5.1.0
 * @see IResourceChangedListener
 */
public class ResourceTextField extends TextField {

  private Button m_popupButton;
  private URL m_url;

  private boolean m_folderMode;
  private String[] m_filterExtensions;
  private String m_fileName;

  private final EventListenerList m_eventListeners;
  private final OptimisticLock m_inputLock;

  /**
   * @see TextField#TextField(Composite)
   */
  public ResourceTextField(Composite parent) {
    this(parent, TYPE_LABEL);
  }

  /**
   * @see TextField#TextField(Composite, int)
   */
  public ResourceTextField(Composite parent, int type) {
    this(parent, type, DEFAULT_LABEL_WIDTH);
  }

  /**
   * @see TextField#TextField(Composite, int, int)
   */
  public ResourceTextField(Composite parent, int type, int labelWidth) {
    super(parent, type, labelWidth);
    m_inputLock = new OptimisticLock();
    m_eventListeners = new EventListenerList();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      m_popupButton.setEnabled(enabled);
    }
  }

  @Override
  public void setEditable(boolean editable) {
    super.setEditable(editable);
    if (m_popupButton != null && !m_popupButton.isDisposed()) {
      m_popupButton.setEnabled(editable);
    }
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    m_popupButton = new Button(parent, SWT.PUSH | SWT.FLAT);
    m_popupButton.setText("Browse...");
    m_popupButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        showFileChooserDialog();
      }
    });

    StyledText text = getTextComponent();
    text.addFocusListener(new FocusListener() {
      @Override
      public void focusLost(FocusEvent e) {
        try {
          if (m_inputLock.acquire()) {
            setUrl(toUrl(getText()));
          }
        }
        finally {
          m_inputLock.release();
        }
      }

      @Override
      public void focusGained(FocusEvent e) {
        // nop
      }

      @SuppressWarnings("squid:S1166")
      private URL toUrl(String path) {
        if (Strings.isBlank(path)) {
          return null;
        }

        try {
          var newFile = Paths.get(path).toRealPath(); // fails on invalid path
          return newFile.toUri().toURL();
        }
        catch (Exception ex) {
          // the supplied string is no valid path for this OS. Try as URL
          try {
            return new URL(path);
          }
          catch (MalformedURLException e1) {
            SdkLog.debug(e1);
            return null;
          }
        }
      }
    });

    // layout
    var textData = (FormData) text.getLayoutData();
    textData.right = new FormAttachment(m_popupButton, -2);

    var buttonData = new FormData();
    buttonData.width = 70;
    buttonData.top = new FormAttachment(0, -1);
    buttonData.right = new FormAttachment(100, 0);
    buttonData.bottom = new FormAttachment(100, 0);
    m_popupButton.setLayoutData(buttonData);
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void showFileChooserDialog() {
    String fileName;
    if (isFolderMode()) {
      var dialog = new DirectoryDialog(getShell());
      var urlAsPath = getFile();
      if (urlAsPath != null) {
        dialog.setFilterPath(urlAsPath.toString());
      }
      fileName = dialog.open();
    }
    else {
      var dialog = new FileDialog(getShell());
      if (getFileName() != null) {
        dialog.setFileName(getFileName());
      }
      dialog.setOverwrite(true);
      if (getFilterExtensions() != null) {
        dialog.setFilterExtensions(getFilterExtensions());
      }
      fileName = dialog.open();

      if (!Strings.isEmpty(fileName) && getFilterExtensions() != null && getFilterExtensions().length > 0) {
        var m = Pattern.compile("\\.([^.]*)$").matcher(fileName);
        String extension = null;
        if (m.find()) {
          var fileNameExt = m.group(1);
          extension = Arrays.stream(getFilterExtensions())
              .anyMatch(("*." + fileNameExt)::equalsIgnoreCase) ? fileNameExt : null;
        }

        var extIndex = dialog.getFilterIndex();
        if (extension == null && extIndex > -1 && extIndex < getFilterExtensions().length) {
          extension = getFilterExtensions()[extIndex];
          extension = Pattern.compile("\\**").matcher(extension).replaceFirst("");
          fileName += extension;
        }
      }
    }

    Path newFile = null;
    if (Strings.hasText(fileName)) {
      newFile = Paths.get(fileName);
      try {
        if (m_inputLock.acquire()) {
          getTextComponent().setText(newFile.toString());
        }
      }
      finally {
        m_inputLock.release();
      }
    }
    setFile(newFile);
  }

  public void addResourceChangedListener(IResourceChangedListener listener) {
    m_eventListeners.add(IResourceChangedListener.class, listener);
  }

  public void removeResourceChangedListener(IResourceChangedListener listener) {
    m_eventListeners.remove(IResourceChangedListener.class, listener);
  }

  private void fireValueChanged() {
    var newFile = getFile();
    var newUrl = getUrl();
    for (var l : m_eventListeners.getListeners(IResourceChangedListener.class)) {
      l.resourceChanged(newUrl, newFile);
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

  public Path getFile() {
    var url = getUrl();
    if (url == null) {
      return null;
    }

    try {
      var uri = url.toURI();
      if (!"file".equalsIgnoreCase(uri.getScheme())) {
        return null;
      }
      return Paths.get(uri);
    }
    catch (URISyntaxException e) {
      SdkLog.debug("Ignoring invalid file URI.", e);
      return null;
    }
  }

  public URL getUrl() {
    return m_url;
  }

  public void setUrl(URL url) {
    if (Objects.equals(url, m_url)) {
      return;
    }

    m_url = url;
    fireValueChanged();
  }

  /**
   * @param file
   *          the file to set
   */
  public void setFile(Path file) {
    String text;
    if (file == null) {
      text = "";
      setUrl(null);
    }
    else {
      text = file.toAbsolutePath().toString();
      try {
        setUrl(file.toUri().toURL());
      }
      catch (MalformedURLException e) {
        SdkLog.warning("Unable convert File to URL", e);
      }
    }

    if (!isDisposed()) {
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
}
