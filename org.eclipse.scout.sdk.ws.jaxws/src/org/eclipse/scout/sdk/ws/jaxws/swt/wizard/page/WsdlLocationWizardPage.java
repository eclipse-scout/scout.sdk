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
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.sdk.ui.fields.TextField;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.AdditionalResourcesWizard;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class WsdlLocationWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_FILE_SYSTEM = "fileSystem";
  public static final String PROP_PATH = "path";
  public static final String PROP_URL = "url";
  public static final String PROP_WSDL_FILE = "wsdlFile";
  public static final String PROP_WSDL_DEFINITION = "wsdlDefinition";
  public static final String PROP_REBUILD_STUB = "rebuilStub";
  public static final String PROP_ADDITIONAL_FILES = "additionalFiles";

  private BasicPropertySupport m_propertySupport;

  private IScoutBundle m_bundle;

  private Button m_fileSystemRadioButton;
  private Composite m_fileSystemContainer;
  private TextField m_pathField;
  private Button m_browseButton;
  private Hyperlink m_addFilesLink;

  private Button m_urlRadioButton;
  private Composite m_urlContainer;
  private TextField m_urlField;

  private Button m_rebuidStubButton;

  private boolean m_rebuildStubOptionVisible;

  public WsdlLocationWizardPage(IScoutBundle bundle) {
    super(WsdlLocationWizardPage.class.getName());
    setTitle(Texts.get("SpecifyWsdlLocation"));
    setDescription(Texts.get("ClickNextToContinue", JaxWsConstants.PATH_WSDL));

    m_bundle = bundle;
    m_propertySupport = new BasicPropertySupport(this);
    applyDefaults();
  }

  private void applyDefaults() {
    setFileSystem(true);
    setAdditionalFiles(new File[0]);
    if (isRebuildStubOptionVisible()) {
      setRebuildStub(true);
    }
  }

  @Override
  protected void createContent(Composite parent) {
    m_fileSystemRadioButton = new Button(parent, SWT.RADIO);
    m_fileSystemRadioButton.setText(Texts.get("ChooseWsdlFileFromFilesystem"));
    m_fileSystemRadioButton.setSelection(isFileSystem());
    m_fileSystemRadioButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          setStateChanging(true);
          setFileSystem(m_fileSystemRadioButton.getSelection());
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_fileSystemContainer = new Composite(parent, SWT.NONE);
    m_pathField = new TextField(m_fileSystemContainer);
    m_pathField.setLabelText("Path");
    m_pathField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setPathInternal(m_pathField.getText());
        pingStateChanging();
      }
    });

    m_browseButton = new Button(m_fileSystemContainer, SWT.PUSH);
    m_browseButton.setText(Texts.get("Browse"));
    m_browseButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        String[] filterNames = new String[]{"wsdl"};
        String[] filterExtensions = new String[]{"*.wsdl"};
        dialog.setFilterNames(filterNames);
        dialog.setFilterExtensions(filterExtensions);
        String path = dialog.open();
        if (path != null) {
          setPath(path);
        }
      }
    });

    m_addFilesLink = new Hyperlink(m_fileSystemContainer, SWT.NONE);
    m_addFilesLink.setText(Texts.get("AddRelatedFiles"));
    m_addFilesLink.setForeground(ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_BLUE));
    m_addFilesLink.setToolTipText(Texts.get("ClickToAddAdditionalResources"));
    m_addFilesLink.setUnderlined(true);
    m_addFilesLink.addHyperlinkListener(new HyperlinkAdapter() {

      @Override
      public void linkActivated(HyperlinkEvent event) {
        AdditionalResourcesWizard wizard = new AdditionalResourcesWizard(getAdditionalFiles());
        ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
        wizardDialog.setPageSize(650, 350);
        if (wizardDialog.open() == Window.OK) {
          setAdditionalFiles(wizard.getFiles());
        }
      }
    });

    m_urlRadioButton = new Button(parent, SWT.RADIO);
    m_urlRadioButton.setText(Texts.get("ChooseWsdlFromUrl", JaxWsConstants.PATH_WSDL));
    m_urlRadioButton.setSelection(!isFileSystem());

    m_urlContainer = new Composite(parent, SWT.NONE);

    m_urlField = new TextField(m_urlContainer);
    m_urlField.setLabelText("Url");
    m_urlField.setEnabled(!isFileSystem());
    m_urlField.addFocusListener(new FocusListener() {

      @Override
      public void focusGained(FocusEvent e) {
        // nop
      }

      @Override
      public void focusLost(FocusEvent e) {
        setUrlInternal(m_urlField.getText());
        pingStateChanging();
      }
    });

    if (isRebuildStubOptionVisible()) {
      m_rebuidStubButton = new Button(parent, SWT.CHECK);
      m_rebuidStubButton.setText(Texts.get("RebuildWebserviceStub"));
      m_rebuidStubButton.setSelection(isRebuildStub());
      m_rebuidStubButton.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          setRebuildStubInternal(m_rebuidStubButton.getSelection());
        }
      });
    }

    // layout
    parent.setLayout(new FormLayout());
    m_fileSystemContainer.setLayout(new FormLayout());
    m_urlContainer.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_fileSystemRadioButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_fileSystemRadioButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_fileSystemContainer.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 50);
    formData.right = new FormAttachment(100, -75);
    m_pathField.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(100, -70);
    formData.right = new FormAttachment(100, 0);
    m_browseButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_browseButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_addFilesLink.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_fileSystemContainer, 10, SWT.BOTTOM);
    formData.left = new FormAttachment(40, 5);
    formData.right = new FormAttachment(100, 0);
    m_urlRadioButton.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(m_urlRadioButton, 5, SWT.BOTTOM);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_urlContainer.setLayoutData(formData);

    formData = new FormData();
    formData.top = new FormAttachment(0, 0);
    formData.left = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_urlField.setLayoutData(formData);

    if (isRebuildStubOptionVisible()) {
      formData = new FormData();
      formData.top = new FormAttachment(m_urlContainer, 20, SWT.BOTTOM);
      formData.left = new FormAttachment(40, 5);
      formData.right = new FormAttachment(100, 0);
      m_rebuidStubButton.setLayoutData(formData);
    }

    JaxWsSdkUtility.setView(m_fileSystemContainer, isFileSystem());
    JaxWsSdkUtility.setView(m_urlContainer, !isFileSystem());
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (isFileSystem()) {
      validatePath(multiStatus);
    }
    else {
      validateUrl(multiStatus);
    }

    // check whether file already exists
    if (getWsdlFile() != null) {
      IFile conflictingFile = m_bundle.getProject().getFile(JaxWsConstants.PATH_WSDL + "/" + getWsdlFile().getName());
      if (conflictingFile != null && conflictingFile.exists()) {
        IPath conflictingFilePath = new Path(conflictingFile.getLocationURI().getRawPath());
        IPath wsdlFilePath = new Path(getWsdlFile().getAbsolutePath());

        if (!conflictingFilePath.equals(wsdlFilePath)) {
          multiStatus.add(new Status(IStatus.WARNING, JaxWsSdk.PLUGIN_ID, Texts.get("WSDLFileAlreadyExists", conflictingFile.getName(), JaxWsConstants.PATH_WSDL)));
        }
      }
    }
  }

  private boolean isFileFromWsdlDirectory(File wsdlFile) {
    IFile potentialSameFile = JaxWsSdkUtility.getFile(m_bundle, JaxWsConstants.PATH_WSDL + "/" + wsdlFile.getName(), false);
    return !(potentialSameFile != null && potentialSameFile.exists() && potentialSameFile.getFullPath().toFile().equals(wsdlFile));
  }

  public void setFileSystem(boolean fileSystem) {
    try {
      setStateChanging(true);
      setFileSystemInternal(fileSystem);

      if (isFileSystem()) {
        setUrl(null);
      }
      else {
        setPath(null);
      }

      if (isControlCreated()) {
        m_fileSystemRadioButton.setSelection(fileSystem);
        JaxWsSdkUtility.setView(m_fileSystemContainer, fileSystem);
        JaxWsSdkUtility.setView(m_urlContainer, !fileSystem);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setFileSystemInternal(boolean fileSystem) {
    m_propertySupport.setProperty(PROP_FILE_SYSTEM, fileSystem);
  }

  public boolean isFileSystem() {
    return m_propertySupport.getPropertyBool(PROP_FILE_SYSTEM);
  }

  private void setWsdlDefinition(Definition wsdlDefinition) {
    m_propertySupport.setProperty(PROP_WSDL_DEFINITION, wsdlDefinition);
  }

  public Definition getWsdlDefinition() {
    return (Definition) m_propertySupport.getProperty(PROP_WSDL_DEFINITION);
  }

  public void setPath(String path) {
    try {
      setStateChanging(true);
      setPathInternal(path);
      if (isControlCreated()) {
        m_pathField.setText(path);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setPathInternal(String path) {
    m_propertySupport.setPropertyString(PROP_PATH, path);
  }

  public String getPath() {
    return m_propertySupport.getPropertyString(PROP_PATH);
  }

  public void setAdditionalFiles(File[] files) {
    m_propertySupport.setProperty(PROP_ADDITIONAL_FILES, files);
  }

  public File[] getAdditionalFiles() {
    File[] files = (File[]) m_propertySupport.getProperty(PROP_ADDITIONAL_FILES);
    if (files == null) {
      files = new File[0];
    }
    return files;
  }

  public void setUrl(String url) {
    try {
      setStateChanging(true);
      setPathInternal(url);
      if (isControlCreated()) {
        m_urlField.setText(url);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setUrlInternal(String url) {
    m_propertySupport.setPropertyString(PROP_URL, url);
  }

  public String getUrl() {
    return m_propertySupport.getPropertyString(PROP_URL);
  }

  private void setWsdlFile(File wsdlFile) {
    m_propertySupport.setProperty(PROP_WSDL_FILE, wsdlFile);
  }

  public File getWsdlFile() {
    return (File) m_propertySupport.getProperty(PROP_WSDL_FILE);
  }

  public void setRebuildStub(boolean rebuildStub) {
    try {
      setStateChanging(true);
      setRebuildStubInternal(rebuildStub);
      if (isControlCreated()) {
        m_rebuidStubButton.setSelection(rebuildStub);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setRebuildStubInternal(boolean rebuildStub) {
    m_propertySupport.setPropertyBool(PROP_REBUILD_STUB, rebuildStub);
  }

  public boolean isRebuildStub() {
    return BooleanUtility.nvl(m_propertySupport.getPropertyBool(PROP_REBUILD_STUB), false);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
    super.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
    super.removePropertyChangeListener(listener);
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  private void validatePath(MultiStatus multiStatus) {
    if (StringUtility.isNullOrEmpty(getPath())) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("PleaseChooseFileFromFilesystem")));
      return;
    }

    File file = new File(getPath());
    if (!file.exists()) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("FileDoesNotExistOrIsCorrupt")));
      return;
    }

    Definition wsdlDefinition = loadWsdlDefinition(file);
    setWsdlDefinition(wsdlDefinition);
    if (wsdlDefinition == null) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("WsdlCorrupt", getPath())));
    }
    else {
      setWsdlFile(file);
    }
  }

  private void validateUrl(MultiStatus multiStatus) {
    if (StringUtility.isNullOrEmpty(getUrl())) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("PleaseEnterValidUrl")));
      return;
    }
    URL url;
    try {
      url = new URL(getUrl());
    }
    catch (MalformedURLException e) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("PleaseEnterValidUrl")));
      return;
    }

    InputStream is;
    // temporarily trust all HTTPS certificates
    SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
    HostnameVerifier defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
    try {
      HttpsURLConnection.setDefaultHostnameVerifier(new P_DummyHostnameVerifier());

      SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, new TrustManager[]{new P_DummyTrustManager()}, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

      URLConnection connection = url.openConnection();
      connection.setAllowUserInteraction(true); // for credentials prompt if required
      connection.setDoOutput(true);
      is = url.openStream();
    }
    catch (Exception e) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("URLCouldNotBeAccessed"), e));
      return;
    }
    finally {
      HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
      HttpsURLConnection.setDefaultHostnameVerifier(defaultHostnameVerifier);
    }

    byte[] content;
    try {
      content = IOUtility.getContent(is, true);
    }
    catch (ProcessingException e) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("CouldNotDownloadWSDLFile")));
      return;
    }

    File tempFile;
    try {
      IPath path = new Path(url.getPath());
      if (path.getFileExtension() == null || !path.getFileExtension().equalsIgnoreCase("wsdl")) {
        path = path.addFileExtension("wsdl");
      }
      String filename = path.lastSegment();
      tempFile = IOUtility.createTempFile(filename, content);
    }
    catch (ProcessingException e) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("CreationOfTemporyFileFailed")));
      return;
    }

    Definition wsdlDefinition = loadWsdlDefinition(tempFile);
    setWsdlDefinition(wsdlDefinition);
    if (wsdlDefinition == null) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("WsdlCorrupt", getPath())));
    }
    else {
      setWsdlFile(tempFile);
    }
  }

  private Definition loadWsdlDefinition(File file) {
    try {
      WSDLFactory factory = WSDLFactory.newInstance();
      WSDLReader reader = factory.newWSDLReader();
      return reader.readWSDL(file.getAbsolutePath());
    }
    catch (Exception e) {
      return null;
    }
  }

  public boolean isRebuildStubOptionVisible() {
    return m_rebuildStubOptionVisible;
  }

  public void setRebuildStubOptionVisible(boolean rebuildStubOptionVisible) {
    m_rebuildStubOptionVisible = rebuildStubOptionVisible;
  }

  private class P_DummyTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] ax509certificate, String s) throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }

  private class P_DummyHostnameVerifier implements HostnameVerifier {

    @Override
    public boolean verify(String s, SSLSession sslsession) {
      return true;
    }
  }
}
