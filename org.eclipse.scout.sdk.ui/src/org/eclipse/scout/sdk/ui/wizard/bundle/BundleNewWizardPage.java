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
package org.eclipse.scout.sdk.ui.wizard.bundle;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.KeyStrokeNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3> {@link BundleNewWizardPage}</h3> ...
 */
public class BundleNewWizardPage extends AbstractWorkspaceWizardPage {

  private StyledTextField m_bundleNameField;

  private String m_bundleName;

  // process members
  private KeyStrokeNewOperation m_operation;
  private IScoutBundle m_parentBundle;

  public BundleNewWizardPage(IScoutBundle bundle) {
    super(BundleNewWizardPage.class.getName());
    m_parentBundle = bundle;
    setTitle("New Scout Bundle");
    setDefaultMessage("Create a new Scout Bundle");
  }

  @Override
  protected void createContent(Composite parent) {
    m_bundleNameField = getFieldToolkit().createStyledTextField(parent, "Bundle Name");
    m_bundleNameField.setReadOnlyPrefix(getParentBundle().getScoutProject().getProjectName());
    m_bundleNameField.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_bundleName = m_bundleNameField.getText();
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_bundleNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    // write back members
    // m_operation.setTypeName(getTypeName());
    // IBCTypeProposal superTypeProp=getSuperType();
    // if(superTypeProp!=null){
    // m_operation.setSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getBcType().getFullyQualifiedName(), true));
    // }
    // m_operation.setKeyStroke(getKeyStroke());
    // m_operation.setSibling(getSibling());
    // m_operation.run(monitor, workingCopyManager);
    return true;
  }

  public KeyStrokeNewOperation getOperation() {
    return m_operation;
  }

  public void setOperation(KeyStrokeNewOperation operation) {
    m_operation = operation;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getBundleName()) || getBundleName().equals(getParentBundle().getScoutProject().getProjectName())) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (ResourcesPlugin.getWorkspace().getRoot().getProject(getBundleName()) != null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    if (!getBundleName().matches("[a-zA-Z0-9\\._-]*")) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_invalidFieldX", getBundleName()));
    }
    else {
      return Status.OK_STATUS;
    }
  }

  public void setParentBundle(IScoutBundle bundle) {
    if (isControlCreated()) {
      throw new IllegalStateException("wizard page already created.");
    }
    m_parentBundle = bundle;
  }

  public IScoutBundle getParentBundle() {
    return m_parentBundle;
  }

  public void setBundleName(String bundleName) {
    try {
      setStateChanging(true);
      m_bundleName = bundleName;
      if (isControlCreated()) {
        m_bundleNameField.setText(bundleName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getBundleName() {
    return m_bundleName;
  }

}
