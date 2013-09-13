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
package org.eclipse.scout.sdk.ui.wizard.outline;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.outline.OutlineNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>OutlineNewWizardPage</h3> ...
 */
public class OutlineNewWizardPage extends AbstractWorkspaceWizardPage {
  private final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);

  private INlsEntry m_nlsName;
  private String m_typeName;
  private IType m_superType;
  private boolean m_addToDesktop;
  private boolean m_addToDesktopEnabled;
  private String m_packageName;

  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private Button m_addToDesktopField;
  private EntityTextField m_entityField;

  // process members
  private final IType m_abstractOutline;
  private final IScoutBundle m_clientBundle;

  private final IType m_desktopType;

  public OutlineNewWizardPage(IScoutBundle clientBundle, IType desktopType) {
    super(OutlineNewWizardPage.class.getName());
    m_clientBundle = clientBundle;
    m_desktopType = desktopType;
    setTitle(Texts.get("NewOutline"));
    setDescription(Texts.get("CreateANewOutline"));
    setTargetPackage(DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_OUTLINES));
    // default values
    m_abstractOutline = RuntimeClasses.getSuperType(RuntimeClasses.IOutline, m_clientBundle.getJavaProject());
    m_superType = m_abstractOutline;
    setAddToDesktop(TypeUtility.exists(getDesktopType()));
    setAddToDesktopEnabled(TypeUtility.exists(getDesktopType()));
  }

  @Override
  protected void createContent(Composite parent) {
    int labelColWidthPercent = 20;
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, getClientBundle().getNlsProject(), Texts.get("Name"), labelColWidthPercent);
    m_nlsNameField.acceptProposal(m_nlsName);
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          INlsEntry oldEntry = getNlsName();
          m_nlsName = (INlsEntry) event.proposal;
          if (m_nlsName != null) {
            if (oldEntry == null || oldEntry.getKey().equals(m_typeNameField.getModifiableText()) || StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(m_nlsName.getKey());
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"), labelColWidthPercent);
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_OUTLINE);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iOutline, getClientBundle().getJavaProject(), m_abstractOutline), labelColWidthPercent);
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_superType = (IType) event.proposal;
        pingStateChanging();
      }
    });

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(parent, Texts.get("EntityTextField"), m_clientBundle, labelColWidthPercent);
      m_entityField.setText(getTargetPackage());
      m_entityField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          setTargetPackageInternal((String) m_entityField.getText());
          pingStateChanging();
        }
      });
      m_entityField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    }

    m_addToDesktopField = new Button(parent, SWT.CHECK);
    m_addToDesktopField.setSelection(isAddToDesktop());
    m_addToDesktopField.setText("Add to Desktop");
    m_addToDesktopField.setEnabled(isAddToDesktopEnabled());
    m_addToDesktopField.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_addToDesktop = m_addToDesktopField.getSelection();
        pingStateChanging();
      }
    });
    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_addToDesktopField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    OutlineNewOperation op = new OutlineNewOperation(getTypeName(), getClientBundle().getPackageName(getTargetPackage()), getClientBundle().getJavaProject());
    // write back members
    op.setFormatSource(true);
    op.setNlsEntry(getNlsName());
    IType superTypeProp = getSuperType();
    if (superTypeProp != null) {
      op.setSuperTypeSignature(SignatureCache.createTypeSignature(superTypeProp.getFullyQualifiedName()));
    }
    if (isAddToDesktop()) {
      op.setDesktopType(getDesktopType());
    }

    op.run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
      multiStatus.add(getStatusTargetPackge());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public IType getDesktopType() {
    return m_desktopType;
  }

  protected IStatus getStatusTargetPackge() {
    return ScoutUtility.validatePackageName(getTargetPackage());
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_OUTLINE)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_className"));
    }
    // check not allowed names
    if (TypeUtility.existsType(getClientBundle().getPackageName(getTargetPackage()) + "." + getTypeName())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    if (Regex.REGEX_WELLFORMD_JAVAFIELD.matcher(getTypeName()).matches()) {
      return Status.OK_STATUS;
    }
    else if (Regex.REGEX_JAVAFIELD.matcher(getTypeName()).matches()) {
      return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_invalidFieldX", getTypeName()));
    }
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  public INlsEntry getNlsName() {
    return m_nlsName;
  }

  public void setNlsName(INlsEntry nlsName) {
    try {
      setStateChanging(true);
      m_nlsName = nlsName;
      if (isControlCreated()) {
        m_nlsNameField.acceptProposal(nlsName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getTypeName() {
    return m_typeName;
  }

  public void setTypeName(String typeName) {
    try {
      setStateChanging(true);
      m_typeName = typeName;
      if (isControlCreated()) {
        m_typeNameField.setText(typeName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      m_superType = superType;
      if (isControlCreated()) {
        m_superTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public void setAddToDesktop(boolean addToDesktop) {
    try {
      setStateChanging(true);
      m_addToDesktop = addToDesktop;
      if (isControlCreated()) {
        m_addToDesktopField.setSelection(addToDesktop);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public boolean isAddToDesktop() {
    return m_addToDesktop;
  }

  public void setAddToDesktopEnabled(boolean addToDesktopEnabled) {
    try {
      setStateChanging(true);
      m_addToDesktopEnabled = addToDesktopEnabled;
      if (isControlCreated()) {
        if (!addToDesktopEnabled) {
          setAddToDesktop(false);
        }
        m_addToDesktopField.setEnabled(addToDesktopEnabled);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public boolean isAddToDesktopEnabled() {
    return m_addToDesktopEnabled;
  }

  public String getTargetPackage() {
    return m_packageName;
  }

  public void setTargetPackage(String targetPackage) {
    try {
      setStateChanging(true);
      setTargetPackageInternal(targetPackage);
      if (isControlCreated() && m_entityField != null) {
        m_entityField.setText(targetPackage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTargetPackageInternal(String targetPackage) {
    m_packageName = targetPackage;
  }
}
