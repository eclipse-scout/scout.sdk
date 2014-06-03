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
package org.eclipse.scout.sdk.ui.wizard.form.fields.radiobutton;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.form.field.ButtonFieldNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureSubTypeProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
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
 * <h3>WizardStepNewWizardPage</h3> ...
 */
public class RadioButtonNewWizardPage extends AbstractWorkspaceWizardPage {

  private final IType iRadioButton = TypeUtility.getType(IRuntimeClasses.IRadioButton);

  private INlsEntry m_nlsName;
  private String m_typeName;
  private IType m_superType;
  private String m_genericSignature;
  private SiblingProposal m_sibling;

  private ProposalTextField m_nlsNameField;
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;
  private ProposalTextField m_genericTypeField;
  private ProposalTextField m_siblingField;

  // process members
  private final IType m_declaringType;
  private IType m_createdRadioButton;
  private IType m_radioButtonGroupValueType;
  private String m_radioButtonGroupValueTypeSig;

  public RadioButtonNewWizardPage(IType declaringType) {
    super(RadioButtonNewWizardPage.class.getName());
    setTitle(Texts.get("NewRadioButtonNoPopup"));
    setDescription(Texts.get("CreateANewRadioButton"));
    m_declaringType = declaringType;

    // default values
    m_sibling = SiblingProposal.SIBLING_END;
  }

  @Override
  protected void createContent(Composite parent) {
    m_nlsNameField = getFieldToolkit().createNlsProposalTextField(parent, ScoutTypeUtility.findNlsProject(m_declaringType), Texts.get("Name"));
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
              m_typeNameField.setText(NamingUtility.toJavaCamelCase(m_nlsName.getKey(), false));
            }
          }
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    ITypeFilter filter = null;
    try {
      ITypeHierarchy radioGroupSuperTypeHierarchy = ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(m_declaringType);
      m_radioButtonGroupValueTypeSig = SignatureUtility.resolveGenericParameterInSuperHierarchy(m_declaringType, radioGroupSuperTypeHierarchy, IRuntimeClasses.IRadioButtonGroup, IRuntimeClasses.TYPE_PARAM_RADIOBUTTONGROUP__VALUE_TYPE);
      if (m_radioButtonGroupValueTypeSig != null) {
        m_radioButtonGroupValueType = TypeUtility.getTypeBySignature(m_radioButtonGroupValueTypeSig);
        filter = TypeFilters.getTypeParamSubTypeFilter(m_radioButtonGroupValueTypeSig, IRuntimeClasses.IRadioButton, IRuntimeClasses.TYPE_PARAM_RADIOBUTTON__VALUE_TYPE);
      }
    }
    catch (CoreException e1) {
      ScoutSdkUi.logWarning("Cannot resolve RadioButtonGroup generic type.", e1);
    }

    // default super type
    IType defaultSuperType = RuntimeClasses.getSuperType(IRuntimeClasses.IRadioButton, m_declaringType.getJavaProject());
    if (filter == null || filter.accept(defaultSuperType)) {
      m_superType = defaultSuperType;
      if (m_radioButtonGroupValueTypeSig != null) {
        setGenericSignature(m_radioButtonGroupValueTypeSig);
      }
    }

    final SignatureSubTypeProposalProvider genericProposalProvider = new SignatureSubTypeProposalProvider(getGenericTypeOfSuperClass(), m_declaringType.getJavaProject());
    IType genericTypeOfSuperClass = getGenericTypeOfSuperClass();
    try {
      genericProposalProvider.setBaseType(TypeUtility.getMoreSpecificType(genericTypeOfSuperClass, m_radioButtonGroupValueType));
    }
    catch (JavaModelException e1) {
      ScoutSdkUi.logWarning("Cannot resolve generic base type.", e1);
    }

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_BUTTON);
    m_typeNameField.setText(m_typeName);
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_typeName = m_typeNameField.getText();
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(iRadioButton, m_declaringType.getJavaProject(), filter, defaultSuperType));
    m_superTypeField.acceptProposal(m_superType);
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_superType = (IType) event.proposal;
        pingStateChanging();

        IType gtosc = getGenericTypeOfSuperClass();
        try {
          if (TypeUtility.exists(gtosc)) {
            boolean acceptProp = false;

            if (getGenericSignature() == null) {
              acceptProp = true;
            }
            else {
              IType t = TypeUtility.getTypeBySignature(getGenericSignature());
              if (TypeUtility.exists(t)) {
                acceptProp = !ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(t).contains(gtosc);
              }
              else {
                acceptProp = true;
              }
            }

            if (acceptProp) {
              m_genericTypeField.acceptProposal(SignatureCache.createTypeSignature(gtosc.getFullyQualifiedName()));
            }
          }
          genericProposalProvider.setBaseType(TypeUtility.getMoreSpecificType(gtosc, m_radioButtonGroupValueType));
        }
        catch (JavaModelException e) {
          ScoutSdkUi.logError(e);
        }
      }
    });

    m_genericTypeField = getFieldToolkit().createProposalField(parent, Texts.get("ValueType"), ProposalTextField.STYLE_DEFAULT);
    m_genericTypeField.setContentProvider(genericProposalProvider);
    m_genericTypeField.setLabelProvider(genericProposalProvider.getLabelProvider());
    m_genericTypeField.acceptProposal(getGenericSignature());
    m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
    m_genericTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_genericSignature = (String) event.proposal;
        pingStateChanging();
      }
    });

    m_siblingField = getFieldToolkit().createFormFieldSiblingProposalField(parent, m_declaringType);
    m_siblingField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_sibling = (SiblingProposal) event.proposal;
        pingStateChanging();
      }
    });
    m_sibling = (SiblingProposal) m_siblingField.getSelectedProposal();

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_nlsNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_genericTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_siblingField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ButtonFieldNewOperation op = new ButtonFieldNewOperation(getTypeName(), getDeclaringType(), true);
    if (getNlsName() != null) {
      op.setNlsEntry(getNlsName());
    }
    if (getSuperType() != null) {
      String sig = null;
      if (getGenericSignature() != null) {
        sig = SignatureCache.createTypeSignature(getSuperType().getFullyQualifiedName() + "<" + Signature.toString(getGenericSignature()) + ">");
      }
      else {
        sig = SignatureCache.createTypeSignature(getSuperType().getFullyQualifiedName());
      }
      op.setSuperTypeSignature(sig);
    }
    SiblingProposal siblingProposal = getSibling();
    if (siblingProposal == SiblingProposal.SIBLING_END) {
      IStructuredType structuredType = ScoutTypeUtility.createStructuredCompositeField(m_declaringType);
      op.setSibling(structuredType.getSibling(CATEGORIES.TYPE_FORM_FIELD));
    }
    else if (siblingProposal != null) {
      op.setSibling(siblingProposal.getElement());
    }

    op.validate();
    op.run(monitor, workingCopyManager);
    m_createdRadioButton = op.getCreatedButton();
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusNameField());
    multiStatus.add(getStatusSuperType());
    multiStatus.add(getStatusGenericType());
    multiStatus.add(getStatusGenericTypeToSuperClass());
    if (isControlCreated()) {
      m_genericTypeField.setEnabled(TypeUtility.isGenericType(getSuperType()));
    }
  }

  protected IStatus getStatusNameField() {
    return ScoutUtility.validateFormFieldName(getTypeName(), SdkProperties.SUFFIX_BUTTON, m_declaringType);
  }

  protected IStatus getStatusSuperType() {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusGenericType() {
    if (TypeUtility.isGenericType(getSuperType())) {
      if (getGenericSignature() == null) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeCanNotBeNull"));
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusGenericTypeToSuperClass() {
    if (getGenericSignature() != null) {
      IType superType = getGenericTypeOfSuperClass();
      if (TypeUtility.exists(superType)) {
        IType generic = TypeUtility.getTypeBySignature(getGenericSignature());
        if (TypeUtility.exists(generic) && !TypeUtility.getSupertypeHierarchy(generic).contains(superType)) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("GenericTypeDoesNotMatchSuperClass"));
        }
      }
    }
    return Status.OK_STATUS;
  }

  protected IType getGenericTypeOfSuperClass() {
    if (TypeUtility.exists(getSuperType())) {
      try {
        ITypeHierarchy superHierarchy = ScoutSdkCore.getHierarchyCache().getSupertypeHierarchy(getSuperType());
        String typeParamSig = SignatureUtility.resolveGenericParameterInSuperHierarchy(getSuperType(), superHierarchy, IRuntimeClasses.IRadioButton, IRuntimeClasses.TYPE_PARAM_RADIOBUTTON__VALUE_TYPE);
        if (typeParamSig != null) {
          return TypeUtility.getTypeBySignature(typeParamSig);
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logError(e);
      }
    }
    return null;
  }

  /**
   * @return the declaringType
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }

  /**
   * @return the createdWizardStep
   */
  public IType getCreatedRadioButton() {
    return m_createdRadioButton;
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

  public void setGenericSignature(String genericSignature) {
    try {
      setStateChanging(true);
      m_genericSignature = genericSignature;
      if (isControlCreated()) {
        m_genericTypeField.acceptProposal(genericSignature);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public String getGenericSignature() {
    return m_genericSignature;
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
