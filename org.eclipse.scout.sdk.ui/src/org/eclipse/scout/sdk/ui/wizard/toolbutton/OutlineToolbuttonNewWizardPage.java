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
package org.eclipse.scout.sdk.ui.wizard.toolbutton;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.OutlineToolbuttonNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>CalendarItemProviderNewWizardPage</h3> ...
 */
public class OutlineToolbuttonNewWizardPage extends AbstractWorkspaceWizardPage {

  final IType iToolButton = TypeUtility.getType(RuntimeClasses.IToolButton);
  final IType iOutline = TypeUtility.getType(RuntimeClasses.IOutline);

  private IType m_outline;
  private String m_typeName;
  private SiblingProposal m_sibling;

  private ProposalTextField m_outlineField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_siblingField;

  // process members
  private final IType m_declaringType;

  public OutlineToolbuttonNewWizardPage(IType declaringType) {
    super(OutlineToolbuttonNewWizardPage.class.getName());
    setTitle(Texts.get("NewOutlineToolButton"));
    setDescription(Texts.get("CreateANewOutlineToolButton"));
    m_declaringType = declaringType;
    // default
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  protected void createContent(Composite parent) {

    m_outlineField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("Outline"), new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        return new Object[][]{ScoutTypeUtility.getClassesOnClasspath(iOutline, m_declaringType.getJavaProject())};
      }
    });
    m_outlineField.acceptProposal(getOutline());
    m_outlineField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_outline = (IType) event.proposal;
          if (getOutline() != null && StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText())) {
            m_typeNameField.setText(getOutline().getElementName());
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_TOOL);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    m_siblingField = getFieldToolkit().createSiblingProposalField(parent, m_declaringType, TypeUtility.getType(RuntimeClasses.IToolButton));
    m_siblingField.acceptProposal(m_sibling);
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_sibling = (SiblingProposal) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_outlineField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    OutlineToolbuttonNewOperation operation = new OutlineToolbuttonNewOperation(getTypeName(), m_declaringType, true);

    // write back members

    ToolbuttonNewWizardPage1 previousPage = (ToolbuttonNewWizardPage1) getWizard().getPage(ToolbuttonNewWizardPage1.class.getName());
    if (previousPage.getSuperType() != null) {
      operation.setSuperTypeSignature(SignatureCache.createTypeSignature(previousPage.getSuperType().getFullyQualifiedName()));
    }
    if (getOutline() != null) {
      operation.setOutlineType(getOutline());
    }
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredOutline(m_declaringType);
      operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_TOOL_BUTTON));
    }
    else {
      operation.setSibling(getSibling().getElement());
    }
    operation.run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusOutline());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(SdkProperties.SUFFIX_TOOL)) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_className"));
    }
    // check not allowed names
    if (TypeUtility.exists(m_declaringType.getType(getTypeName()))) {
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

  protected IStatus getStatusOutline() {
    if (getOutline() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("AnOutlineMustBeSelected"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public IWizardPage getNextPage() {
    return null;
  }

  public void setOutline(IType outline) {
    try {
      setStateChanging(true);
      m_outline = outline;
      if (isControlCreated()) {
        m_outlineField.acceptProposal(outline);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public IType getOutline() {
    return m_outline;
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

  public SiblingProposal getSibling() {
    return m_sibling;
  }

  public void setSibling(SiblingProposal sibling) {
    try {
      setStateChanging(true);
      m_sibling = sibling;
      if (isControlCreated()) {
        m_siblingField.acceptProposal(sibling);
      }
    }
    finally {
      setStateChanging(false);
    }
  }
}
