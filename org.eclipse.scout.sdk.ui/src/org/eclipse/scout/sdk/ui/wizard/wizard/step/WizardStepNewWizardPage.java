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
package org.eclipse.scout.sdk.ui.wizard.wizard.step;

import java.util.Set;

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
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.WizardStepNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.SimpleJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.IStructuredType;
import org.eclipse.scout.sdk.workspace.type.IStructuredType.CATEGORIES;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * <h3>{@link WizardStepNewWizardPage}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 02.09.2010
 */
public class WizardStepNewWizardPage extends AbstractWorkspaceWizardPage {

  private final IType iWizardStep = TypeUtility.getType(IRuntimeClasses.IWizardStep);

  private static final String PROP_NLS_NAME = "nlsName";
  private static final String PROP_TYPE_NAME = "typeName";
  private static final String PROP_SUPER_TYPE = "superType";
  private static final String PROP_SIBLING = "sibling";
  private static final String PROP_FORM_TYPE = "formType";
  private static final String PROP_FORM_HANDLER_TYPE = "formHandlerType";

  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_siblingField;
  private ProposalTextField m_stepFormField;
  private ProposalTextField m_formHandlerField;

  // process members
  private final IType m_abstractWizardStep;
  private final IType m_declaringType;
  private IType m_createdWizardStep;

  public WizardStepNewWizardPage(IType declaringType) {
    super(WizardStepNewWizardPage.class.getName());
    setTitle(Texts.get("NewWizardStep"));
    setDescription(Texts.get("CreateANewWizardStep"));
    m_declaringType = declaringType;
    m_abstractWizardStep = RuntimeClasses.getSuperType(IRuntimeClasses.IWizardStep, m_declaringType.getJavaProject());
    // default values
    setSuperTypeInternal(m_abstractWizardStep);
    setSibling(SiblingProposal.SIBLING_END);
  }

  @Override
  protected void createContent(Composite parent) {

    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, ScoutTypeUtility.findNlsProject(m_declaringType), Texts.get("Name"));
    m_nlsNameField.acceptProposal(getNlsName());
    m_nlsNameField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          INlsEntry oldEntry = getNlsName();
          INlsEntry newEntry = (INlsEntry) event.proposal;
          setNlsNameInternal(newEntry);
          if (newEntry != null) {
            if (oldEntry == null || oldEntry.getKey().equals(m_typeNameField.getModifiableText()) || StringUtility.isNullOrEmpty(m_typeNameField.getModifiableText())) {
              m_typeNameField.setText(NamingUtility.toJavaCamelCase(newEntry.getKey(), false));
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_WIZARD_STEP);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iWizardStep, m_declaringType.getJavaProject(), m_abstractWizardStep));
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    m_siblingField = getFieldToolkit().createSiblingProposalField(parent, m_declaringType, iWizardStep);
    m_siblingField.acceptProposal(getSibling());
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSiblingInternal((SiblingProposal) event.proposal);
        pingStateChanging();
      }
    });

    Control formGroup = createFormGroup(parent);

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    formGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  /**
   * @param parent
   * @return
   */
  protected Control createFormGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText("Steps form");
    m_stepFormField = getFieldToolkit().createProposalField(group, Texts.get("Form"));
    AbstractJavaElementContentProvider contentProvider = new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        Set<IType> forms = TypeUtility.getClassesOnClasspath(TypeUtility.getType(IRuntimeClasses.IForm), m_declaringType.getJavaProject(), null);
        return new Object[][]{forms.toArray(new IType[forms.size()])};
      }
    };
    m_stepFormField.setContentProvider(contentProvider);
    m_stepFormField.setLabelProvider(contentProvider.getLabelProvider());
    m_stepFormField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          setFormType((IType) event.proposal);

          AbstractJavaElementContentProvider formHandlerProvider = null;
          IType formHandlerSelection = (IType) m_formHandlerField.getSelectedProposal();
          if (getFormType() != null) {
            Set<IType> formHandlers = ScoutTypeUtility.getFormHandlers(getFormType());
            if (formHandlers != null) {
              formHandlerProvider = new SimpleJavaElementContentProvider(formHandlers.toArray(new IType[formHandlers.size()]));
            }
            // assign null selection if the current selected form is not the declaring type of the form hanlder selection.
            if (formHandlerSelection != null && !getFormType().equals(formHandlerSelection.getDeclaringType())) {
              formHandlerSelection = null;
            }
            m_formHandlerField.setEnabled(true);
          }
          else {
            m_formHandlerField.setEnabled(false);
          }
          // backup
          m_formHandlerField.setContentProvider(formHandlerProvider);
          m_formHandlerField.setLabelProvider(formHandlerProvider == null ? null : formHandlerProvider.getLabelProvider());
          m_formHandlerField.acceptProposal(formHandlerSelection);
        }
        finally {
          setStateChanging(false);
        }

      }
    });

    m_formHandlerField = getFieldToolkit().createProposalField(group, Texts.get("FormHandler"));
    m_formHandlerField.setEnabled(false);
    m_formHandlerField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setFormHandlerTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    m_stepFormField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_formHandlerField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return group;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    WizardStepNewOperation operation = new WizardStepNewOperation(getTypeName(), m_declaringType, true);

    // write back members
    operation.setNlsEntry(getNlsName());
    IType superTypeProp = getSuperType();
    if (superTypeProp != null) {
      operation.setSuperTypeSignature(SignatureCache.createTypeSignature(superTypeProp.getFullyQualifiedName()));
    }
    if (getSibling() == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredWizard(m_declaringType);
      operation.setSibling(structuredType.getSibling(CATEGORIES.TYPE_WIZARD_STEP));
    }
    else {
      operation.setSibling(getSibling().getElement());
    }
    operation.setForm(getFormType());
    operation.setFormHandler(getFormHandlerType());
    operation.validate();
    operation.run(monitor, workingCopyManager);
    m_createdWizardStep = operation.getCreatedWizardStep();
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    IStatus javaFieldNameStatus = ScoutUtility.validateJavaName(getTypeName(), SdkProperties.SUFFIX_WIZARD_STEP);
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }
    if (TypeUtility.exists(m_declaringType.getType(getTypeName()))) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }
    return javaFieldNameStatus;
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  /**
   * @return the createdWizardStep
   */
  public IType getCreatedWizardStep() {
    return m_createdWizardStep;
  }

  public INlsEntry getNlsName() {
    return (INlsEntry) getProperty(PROP_NLS_NAME);
  }

  public void setNlsName(INlsEntry nlsName) {
    try {
      setStateChanging(true);
      setNlsNameInternal(nlsName);
      if (isControlCreated()) {
        m_nlsNameField.acceptProposal(nlsName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setNlsNameInternal(INlsEntry nlsName) {
    setProperty(PROP_NLS_NAME, nlsName);
  }

  public String getTypeName() {
    return getPropertyString(PROP_TYPE_NAME);
  }

  public void setTypeName(String typeName) {
    try {
      setStateChanging(true);
      setTypeNameInternal(typeName);
      if (isControlCreated()) {
        m_typeNameField.setText(typeName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setTypeNameInternal(String typeName) {
    setPropertyString(PROP_TYPE_NAME, typeName);
  }

  public IType getSuperType() {
    return (IType) getProperty(PROP_SUPER_TYPE);
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      setSuperTypeInternal(superType);
      if (isControlCreated()) {
        m_superTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setSuperTypeInternal(IType superType) {
    setProperty(PROP_SUPER_TYPE, superType);
  }

  public SiblingProposal getSibling() {
    return (SiblingProposal) getProperty(PROP_SIBLING);
  }

  public void setSibling(SiblingProposal sibling) {
    try {
      setStateChanging(true);
      setSiblingInternal(sibling);
      if (isControlCreated()) {
        m_siblingField.acceptProposal(sibling);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setSiblingInternal(SiblingProposal sibling) {
    setProperty(PROP_SIBLING, sibling);
  }

  public IType getFormType() {
    return (IType) getProperty(PROP_FORM_TYPE);
  }

  public void setFormType(IType superType) {
    try {
      setStateChanging(true);
      setFormTypeInternal(superType);
      if (isControlCreated()) {
        m_stepFormField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setFormTypeInternal(IType superType) {
    setProperty(PROP_FORM_TYPE, superType);
  }

  public IType getFormHandlerType() {
    return (IType) getProperty(PROP_FORM_HANDLER_TYPE);
  }

  public void setFormHandlerType(IType superType) {
    try {
      setStateChanging(true);
      setFormHandlerTypeInternal(superType);
      if (isControlCreated()) {
        m_formHandlerField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setFormHandlerTypeInternal(IType superType) {
    setProperty(PROP_FORM_HANDLER_TYPE, superType);
  }
}
