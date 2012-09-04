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
package org.eclipse.scout.sdk.ui.wizard.services;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.JavaElementAbstractTypeContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ServiceNewWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 29.07.2009
 */
public class ServiceNewWizardPage extends AbstractWorkspaceWizardPage {

  /** {@link String} **/
  public static final String PROP_TYPE_NAME = "typeName";
  /** {@link ITypeProposal} **/
  public static final String PROP_SUPER_TYPE = "superType";

  // ui fields
  private StyledTextField m_typeNameField;
  private ProposalTextField m_superTypeField;

  // process members
  private final IType m_definitionType;
  private final String m_typeNameSuffix;

  private IScoutBundle m_locationBundle;

  public ServiceNewWizardPage(String title, String message, IType definitionType, String typeNameSuffix) {
    super(ServiceNewWizardPage.class.getName());
    m_typeNameSuffix = typeNameSuffix;
    m_definitionType = definitionType;
    setTitle(title);
    setDescription(message);
  }

  @Override
  protected void createContent(Composite parent) {
    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(getTypeNameSuffix());
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    m_superTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("SuperType"),
        new JavaElementAbstractTypeContentProvider(m_definitionType, getLocationBundle().getJavaProject(), getSuperType()));
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setSuperTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
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
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(getTypeNameSuffix())) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  /**
   * @param locationBundle
   *          the locationBundle to set
   */
  public void setLocationBundle(IScoutBundle locationBundle) {
    m_locationBundle = locationBundle;
  }

  /**
   * @return the locationBundle
   */
  public IScoutBundle getLocationBundle() {
    return m_locationBundle;
  }

  public String getTypeNameSuffix() {
    return m_typeNameSuffix;
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

  protected void setTypeNameInternal(String typeName) {
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

  protected void setSuperTypeInternal(IType superType) {
    setProperty(PROP_SUPER_TYPE, superType);
  }

}
