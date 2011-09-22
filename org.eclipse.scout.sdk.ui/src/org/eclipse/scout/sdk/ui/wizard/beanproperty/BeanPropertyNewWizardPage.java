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
package org.eclipse.scout.sdk.ui.wizard.beanproperty;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.IBeanPropertyNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SignatureProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.SignatureProposalProvider;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class BeanPropertyNewWizardPage extends AbstractWorkspaceWizardPage {

  // fields
  private String m_beanName;
  private SignatureProposal m_beanSignature;

  // ui fields
  private StyledTextField m_beanNameField;
  private ProposalTextField m_beanTypeField;

  // other fields
  private IBeanPropertyNewOperation m_operation;
  private Set<String> m_notAllowedNames;

  private final IJavaSearchScope m_searchScope;

  public BeanPropertyNewWizardPage(IJavaSearchScope searchScope) {
    super(Texts.get("NewPropertyBean"));
    m_searchScope = searchScope;
    setTitle(Texts.get("NewPropertyBean"));
    setDefaultMessage(Texts.get("NewPropertyBeanDesc"));
  }

  @Override
  protected void createContent(Composite parent) {
    // find client session

    m_beanNameField = new StyledTextField(parent, Texts.get("Dialog_rename_oldNameLabel"));
    m_beanNameField.setText(getBeanName());
    m_beanNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        m_beanName = m_beanNameField.getText();
        pingStateChanging();
      }
    });

    m_beanTypeField = new ProposalTextField(parent, new SignatureProposalProvider(m_searchScope, true, true));
    m_beanTypeField.setLabelText(Texts.get("Dialog_propertyBean_typeLabel"));
    m_beanTypeField.acceptProposal(getBeanSignature());
    m_beanTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_beanSignature = (SignatureProposal) event.proposal;
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    // m_beanGenericTypeField=new ProposalTextField(parent, new BcElementProposalProvider(getProjectGroup(), false, false));
    // m_beanGenericTypeField.setEnabled(false);
    // m_beanGenericTypeField.acceptProposal(getBeanGenericType());
    // m_beanGenericTypeField.setLabelText(Texts.get("Dialog_propertyBean_genericLabel"));
    // m_beanGenericTypeField.addProposalAdapterListener(new IProposalAdapterListener(){
    // public void proposalAccepted(ContentProposalEvent event){
    // m_beanGenericType=(IBCTypeProposal)event.proposal;
    // pingStateChanging();
    // }
    // });

    parent.setLayout(new GridLayout(1, true));
    // layout
    m_beanNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_beanTypeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    // m_beanGenericTypeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL|GridData.GRAB_HORIZONTAL));
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {

    // m_propertyName
    IStatus status = getPropertyNameStatus();
    multiStatus.add(status);
    multiStatus.add(getPropertyTypeStatus());
    // m_beanType
    // IStatus typeStatus=getPropertyTypeStatus();
    // ControlStatusFactory.applyStatusToField(m_beanTypeField, typeStatus);
    // multiStatus.add(typeStatus);
    // // m_genericType
    // IStatus genericStatus=getBeanGenericStatus();
    // ControlStatusFactory.applyStatusToField(m_beanGenericTypeField, genericStatus);
    // multiStatus.add(genericStatus);
  }

  private IStatus getPropertyNameStatus() {
    String propertyName = getBeanName();
    if (propertyName == null || propertyName.length() == 0) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    else {
      // check existing method names
      if (m_notAllowedNames != null &&
          (m_notAllowedNames.contains("get" + getBeanName(true)) ||
              m_notAllowedNames.contains("set" + getBeanName(true)) ||
          m_notAllowedNames.contains("is" + getBeanName(true)))) {
        return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
      if (propertyName.matches(Regex.REGEX_WELLFORMED_PROPERTY)) {
        return Status.OK_STATUS;
      }
      if (propertyName.matches(Regex.REGEX_JAVAFIELD)) {
        return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
      }
      else {
        return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_invalidFieldX", propertyName));
      }
    }
  }

  private IStatus getPropertyTypeStatus() {
    SignatureProposal signature = getBeanSignature();
    if (signature == null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_beanTypeNull"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
    m_operation.setBeanName(getBeanName());
    if (getBeanSignature() != null) {
      m_operation.setBeanTypeSignature(getBeanSignature().getSignature());
    }

    m_operation.setMethodFlags(Flags.AccPublic);
    m_operation.run(monitor, manager);
    return true;
  }

  public Set<String> getNotAllowedNames() {
    return m_notAllowedNames;
  }

  public void setNotAllowedNames(Set<String> notAllowedNames) {
    m_notAllowedNames = notAllowedNames;
  }

  public IBeanPropertyNewOperation getOperation() {
    return m_operation;
  }

  public void setOperation(IBeanPropertyNewOperation operation) {
    m_operation = operation;
  }

  public String getBeanName() {
    return m_beanName;
  }

  public String getBeanName(boolean startWithUpperCase) {
    if (StringUtility.isNullOrEmpty(getBeanName())) {
      return null;
    }
    if (startWithUpperCase) {
      return Character.toUpperCase(getBeanName().charAt(0)) + getBeanName().substring(1);
    }
    else {
      return Character.toLowerCase(getBeanName().charAt(0)) + getBeanName().substring(1);
    }
  }

  public void setBeanName(String beanName) {
    try {
      setStateChanging(true);
      m_beanName = beanName;
      if (isControlCreated()) {
        m_beanNameField.setText(beanName);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public SignatureProposal getBeanSignature() {
    return m_beanSignature;
  }

  public void setBeanSignature(SignatureProposal beanSignature) {
    try {
      setStateChanging(true);
      m_beanSignature = beanSignature;
      if (isControlCreated()) {
        m_beanTypeField.acceptProposal(beanSignature);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

}
