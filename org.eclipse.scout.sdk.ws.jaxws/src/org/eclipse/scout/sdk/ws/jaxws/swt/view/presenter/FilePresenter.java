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
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.ExternalFileCopyOperation;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility.SeparatorType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class FilePresenter extends AbstractPropertyPresenter<IFile> {

  private Composite m_composite;
  private Text m_textField;
  private Button m_button;
  private String m_fileExtension;
  private String m_fileDirectory;
  private boolean m_showBrowseButton;

  public FilePresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    super(parent, toolkit, false);
    setLabel(Texts.get("File"));
    setUseLinkAsLabel(true);
    setShowBrowseButton(true);
    callInitializer();
  }

  @Override
  protected final Control createContent(Composite parent) {
    m_composite = getToolkit().createComposite(parent, SWT.NONE);
    m_textField = getToolkit().createText(m_composite, "", SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
    m_textField.setEditable(false);
    m_textField.setBackground(JaxWsSdkUtility.getColorLightGray());

    m_button = new Button(m_composite, SWT.PUSH | SWT.FLAT);
    m_button.setText(getConfiguredBrowseButtonLabel());
    m_button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IFile file = execBrowseAction();
        if (file != null) {
          setInputInternal(file);
          setValueFromUI(file);
        }
      }
    });

    // layout
    GridLayout layout = new GridLayout();
    layout.horizontalSpacing = 0;
    layout.marginWidth = 0;
    layout.numColumns = 2;
    layout.marginBottom = 0;
    layout.marginRight = 0;
    layout.marginLeft = 0;
    layout.marginTop = 0;
    layout.verticalSpacing = 0;
    layout.marginHeight = 0;
    layout.makeColumnsEqualWidth = false;
    m_composite.setLayout(layout);

    GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    gd.grabExcessHorizontalSpace = true;
    m_textField.setLayoutData(gd);

    gd = new GridData();
    gd.grabExcessHorizontalSpace = false;
    gd.widthHint = 50;
    gd.horizontalIndent = 5;
    gd.exclude = !isShowBrowseButton();
    m_button.setLayoutData(gd);

    return m_composite;
  }

  protected String getConfiguredBrowseButtonLabel() {
    return Texts.get("Browse");
  }

  @Override
  protected void setInputInternal(IFile input) {
    if (input != null) {
      m_textField.setText(input.getName());
      m_textField.setToolTipText(input.getName());
    }
    else {
      m_textField.setText("");
      m_textField.setToolTipText(null);
    }

    setUseLinkAsLabel(input != null && input.exists());
  }

  @Override
  protected void execLinkAction() throws CoreException {
    IFile file = getValue();
    if (file != null && file.exists()) {
      IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, true);
    }
  }

  protected IFile execBrowseAction() {
    FileDialog dialog = new FileDialog(ScoutSdkUi.getShell(), SWT.OPEN);
    if (StringUtility.hasText(m_fileExtension)) {
      dialog.setFilterNames(new String[]{m_fileExtension});
      dialog.setFilterExtensions(new String[]{"*." + m_fileExtension});
    }
    String path = dialog.open();
    if (path == null) {
      return null;
    }
    File file = IOUtility.toFile(path);

    // check whether to copy external file into workspace
    if (isCopyRequired(file)) {
      ExternalFileCopyOperation op = new ExternalFileCopyOperation();
      op.setBundle(m_bundle);
      op.setOverwrite(true);
      op.setExternalFile(file);
      op.setWorkspacePath(new Path(m_fileDirectory));
      OperationJob job = new OperationJob(op);
      job.schedule();
      try {
        job.join();
      }
      catch (InterruptedException e) {
        JaxWsSdk.logError("unexpected error occured while waiting for operation to complete", e);
        return null;
      }
    }

    return JaxWsSdkUtility.getFile(m_bundle, JaxWsSdkUtility.normalizePath(m_fileDirectory, SeparatorType.BothType) + file.getName(), false);
  }

  public String getFileExtension() {
    return m_fileExtension;
  }

  public void setFileExtension(String fileExtension) {
    m_fileExtension = fileExtension;
  }

  public String getFileDirectory() {
    return m_fileDirectory;
  }

  public void setFileDirectory(String fileDirectory) {
    m_fileDirectory = fileDirectory;
  }

  public boolean isShowBrowseButton() {
    return m_showBrowseButton;
  }

  public void setShowBrowseButton(boolean showBrowseButton) {
    if (m_showBrowseButton == showBrowseButton) {
      return;
    }
    m_showBrowseButton = showBrowseButton;

    m_showBrowseButton = showBrowseButton;
    if (isControlCreated()) {
      ((GridData) m_button.getLayoutData()).exclude = !showBrowseButton;
      m_composite.layout();
    }
  }

  private boolean isCopyRequired(File file) {
    IFile potentialSameFile = JaxWsSdkUtility.getFile(m_bundle, JaxWsSdkUtility.normalizePath(m_fileDirectory, SeparatorType.BothType) + file.getName(), false);

    if (potentialSameFile != null && potentialSameFile.exists()) {
      IPath potentialSameFilePath = new Path(potentialSameFile.getLocationURI().getRawPath());
      IPath filePath = new Path(file.getAbsolutePath());

      if (potentialSameFilePath.equals(filePath)) {
        return false;
      }
    }

    return true;
  }
}
