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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class SourceFolderNewWizardPage extends AbstractWorkspaceWizardPage {

  public static final String PROP_SOURCE_FOLDER = "sourceFolder";

  private BasicPropertySupport m_propertySupport;

  private IScoutBundle m_bundle;
  private StyledTextField m_textField;

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public SourceFolderNewWizardPage() {
    super(SourceFolderNewWizardPage.class.getName());
    m_propertySupport = new BasicPropertySupport(this);
    setTitle(Texts.get("CreateNewSourceFolder"));
    setDescription(Texts.get("ByClickingFinishSourceFolderIsCreated"));
  }

  @Override
  protected void createContent(Composite parent) {
    m_textField = new StyledTextField(parent);
    m_textField.setReadOnlyPrefix(m_bundle.getJavaProject().getPath().toString() + "/");
    m_textField.setLabelText(Texts.get("SourceFolder"));
    m_textField.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        setSourceFolderInternal(m_textField.getText());
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new FormLayout());

    FormData formData = new FormData();
    formData.left = new FormAttachment(0, 0);
    formData.top = new FormAttachment(0, 0);
    formData.right = new FormAttachment(100, 0);
    m_textField.setLayoutData(formData);
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (StringUtility.isNullOrEmpty(getSourceFolder())) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("EnterSourceFolder")));
      return;
    }
    IWorkspace workspace = m_bundle.getProject().getWorkspace();
    IStatus status = workspace.validatePath(getSourceFolder(), IResource.FOLDER);
    if (status.matches(IStatus.ERROR)) {
      multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("InvalidSourceFolder")));
      return;
    }

    Path candidatePath = new Path(getSourceFolder());
    for (IPath path : getExistingSourcePaths()) {
      if (candidatePath.equals(path)) {
        multiStatus.add(new Status(IStatus.ERROR, JaxWsSdk.PLUGIN_ID, Texts.get("SourceFolderAlreadyExsists")));
        return;
      }
    }
  }

  private Set<IPath> getExistingSourcePaths() {
    Set<IPath> sourceFolders = new HashSet<IPath>();
    try {
      for (IClasspathEntry classpathEntry : m_bundle.getJavaProject().getRawClasspath()) {
        if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
          sourceFolders.add(classpathEntry.getPath());
        }
      }
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("Error occured while fetching source folders.", e);
    }
    return sourceFolders;
  }

  public void setSourceFolder(String sourceFolder) {
    try {
      setStateChanging(true);
      setSourceFolderInternal(sourceFolder);
      if (isControlCreated()) {
        m_textField.setText(sourceFolder);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setSourceFolderInternal(String sourceFolder) {
    m_propertySupport.setPropertyString(PROP_SOURCE_FOLDER, sourceFolder);
  }

  public String getSourceFolder() {
    return m_propertySupport.getPropertyString(PROP_SOURCE_FOLDER);
  }

  public Path getSourceFolderPath() {
    return new Path(m_propertySupport.getPropertyString(PROP_SOURCE_FOLDER));
  }
}
