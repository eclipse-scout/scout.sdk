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

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.IBeanPropertyNewOperation;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureProposalProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.IRegEx;
import org.eclipse.scout.sdk.util.NamingUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.type.MethodFilters;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class BeanPropertyNewWizardPage extends AbstractWorkspaceWizardPage {

  // fields
  private String m_beanName;
  private String m_beanSignature;

  // ui fields
  private StyledTextField m_beanNameField;
  private ProposalTextField m_beanTypeField;

  // other fields
  private IBeanPropertyNewOperation m_operation;
  private Set<String> m_notAllowedNames;

  private final IJavaSearchScope m_searchScope;
  private final IType m_declaringType;
  private List<IType> m_allValueFields;

  public BeanPropertyNewWizardPage(IJavaSearchScope searchScope, IType declaringType) {
    super(BeanPropertyNewWizardPage.class.getName());
    m_searchScope = searchScope;
    m_declaringType = declaringType;
    setTitle(Texts.get("NewPropertyBean"));
    setDescription(Texts.get("NewPropertyBeanDesc"));

    ITypeHierarchy typeHierarchy = TypeUtility.getLocalTypeHierarchy(m_declaringType);
    m_allValueFields = ScoutTypeUtility.getAllTypes(m_declaringType.getCompilationUnit(), TypeFilters.getSubtypeFilter(TypeUtility.getType(IRuntimeClasses.IValueField), typeHierarchy));
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

    ILabelProvider labelProvider = new SignatureLabelProvider();
    SignatureProposalProvider contentProvider = new SignatureProposalProvider(m_searchScope, labelProvider, SignatureProposalProvider.DEFAULT_MOST_USED, true);
    contentProvider.setPrimitivSignatures(SignatureProposalProvider.DEFAULT_PRIMITIV_SIGNATURES);
    m_beanTypeField = getFieldToolkit().createProposalField(parent, Texts.get("Dialog_propertyBean_typeLabel"));
    m_beanTypeField.setLabelProvider(labelProvider);
    m_beanTypeField.setContentProvider(contentProvider);
    m_beanTypeField.acceptProposal(getBeanSignature());
    m_beanTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        try {
          setStateChanging(true);
          m_beanSignature = (String) event.proposal;
        }
        finally {
          setStateChanging(false);
        }
      }
    });

    parent.setLayout(new GridLayout(1, true));
    // layout
    m_beanNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    m_beanTypeField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getPropertyNameStatus());
    multiStatus.add(getPropertyTypeStatus());
  }

  private IStatus getPropertyNameStatus() {
    String propertyName = getBeanName();
    if (propertyName == null || propertyName.length() == 0) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_className"));
    }

    String name = NamingUtility.ensureStartWithUpperCase(getBeanName());
    // check existing method names
    if (m_notAllowedNames != null) {
      if (m_notAllowedNames.contains("get" + name) || m_notAllowedNames.contains("set" + name) || m_notAllowedNames.contains("is" + name)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }

    // check that no value field has the same name. this could lead to errors in form data generation (duplicate methods).
    for (IType valueField : m_allValueFields) {
      String fieldName = ScoutUtility.removeFieldSuffix(valueField.getElementName());
      if (name.equals(fieldName)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }

    if (TypeUtility.getMethods(m_declaringType, MethodFilters.getNameRegexFilter(Pattern.compile("^(get|set|is)" + name))).size() > 0) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }

    if (IRegEx.WELLFORMED_PROPERTY.matcher(propertyName).matches()) {
      return Status.OK_STATUS;
    }

    if (IRegEx.JAVAFIELD.matcher(propertyName).matches()) {
      return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }

    return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_invalidFieldX", propertyName));
  }

  private IStatus getPropertyTypeStatus() {
    String signature = getBeanSignature();
    if (signature == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_beanTypeNull"));
    }
    return Status.OK_STATUS;
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
    m_operation.setBeanName(getBeanName());
    m_operation.setBeanTypeSignature(getBeanSignature());
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

  public String getBeanSignature() {
    return m_beanSignature;
  }

  public void setBeanSignature(String beanSignature) {
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
