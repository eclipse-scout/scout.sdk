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
package org.eclipse.scout.sdk.ui.wizard.lookupcall;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.Regex;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * <h3>LookupCallNewWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 25.08.2010
 */
public class LookupCallNewWizardPage extends AbstractWorkspaceWizardPage {

  public static enum LOOKUP_SERVICE_STRATEGY {
    CREATE_NEW, USE_EXISTING, NO_SERVICE
  }

  private static final IType iLookupService = ScoutSdk.getType(RuntimeClasses.ILookupService);
  private static final IType abstractSqlLookupService = ScoutSdk.getType(RuntimeClasses.AbstractSqlLookupService);
  private static final IType abstractLookupService = ScoutSdk.getType(RuntimeClasses.AbstractLookupService);

  /** {@link String} **/
  public static final String PROP_TYPE_NAME = "typeName";
  /** {@link ITypeProposal} **/
  public static final String PROP_SERVICE_SUPER_TYPE = "serviceSuperType";
  /** {@link ITypeProposal} **/
  public static final String PROP_LOOKUP_SERVICE = "lookupService";
  /** {@link LOOKUP_SERVICE_STRATEGY} **/
  public static final String PROP_LOOKUP_SERVICE_STRATEGY = "lookupServiceStrategy";

  // ui fields
  private StyledTextField m_typeNameField;
  private Button m_createServiceButton;
  private Button m_useServiceButton;
  private Button m_noServiceButton;
  private ProposalTextField m_serviceSuperTypeField;
  private ProposalTextField m_lookupServiceTypeField;

  // process members
  private IScoutBundle m_sharedBundle;
  private IScoutBundle m_serverBundle;

  public LookupCallNewWizardPage(IScoutBundle sharedBundle) {
    super(LookupCallNewWizardPage.class.getName());
    m_sharedBundle = sharedBundle;
    setTitle(Texts.get("NewLookupCall"));
    setDefaultMessage(Texts.get("CreateANewLookupCall"));
    setLookupServiceStrategy(LOOKUP_SERVICE_STRATEGY.CREATE_NEW);
    setServiceSuperTypeInternal(ScoutProposalUtility.getScoutTypeProposalsFor(abstractSqlLookupService)[0]);

  }

  @Override
  protected void createContent(Composite parent) {

    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"));
    m_typeNameField.setReadOnlySuffix(ScoutIdeProperties.SUFFIX_LOOKUP_CALL);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    Control lookupServiceGroup = createLookupServiceGroup(parent);

    updateSharedBundle();
    updateServerBundle();
    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    lookupServiceGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  protected Control createLookupServiceGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText("Lookup Service");
    Composite radioButtons = new Composite(group, SWT.INHERIT_DEFAULT);
    m_createServiceButton = new Button(radioButtons, SWT.RADIO);
    m_createServiceButton.addSelectionListener(new P_LookupServiceStrategyButtonListener(LOOKUP_SERVICE_STRATEGY.CREATE_NEW));
    m_createServiceButton.setText("create new lookup service");
    m_createServiceButton.setSelection(true);
    m_useServiceButton = new Button(radioButtons, SWT.RADIO);
    m_useServiceButton.addSelectionListener(new P_LookupServiceStrategyButtonListener(LOOKUP_SERVICE_STRATEGY.USE_EXISTING));
    m_useServiceButton.setText("use existing lookup service");
    m_noServiceButton = new Button(radioButtons, SWT.RADIO);
    m_noServiceButton.addSelectionListener(new P_LookupServiceStrategyButtonListener(LOOKUP_SERVICE_STRATEGY.NO_SERVICE));
    m_noServiceButton.setText("no lookup service");

    m_serviceSuperTypeField = getFieldToolkit().createProposalField(group, null, Texts.get("LookupServiceSuperType"));
    m_serviceSuperTypeField.acceptProposal(getServiceSuperType());
    m_serviceSuperTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setServiceSuperTypeInternal((ITypeProposal) event.proposal);
        pingStateChanging();
      }
    });

    m_lookupServiceTypeField = getFieldToolkit().createProposalField(group, null, Texts.get("LookupSerivice"));
    m_lookupServiceTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setLookupServiceTypeInternal((ITypeProposal) event.proposal);
        pingStateChanging();
      }
    });

    // layout
    group.setLayout(new GridLayout(1, true));
    radioButtons.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    GridLayout radioButtonGroupLayout = new GridLayout(3, true);
    radioButtons.setLayout(radioButtonGroupLayout);
    m_createServiceButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_useServiceButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_noServiceButton.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

    m_serviceSuperTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    GridData lookupServiceData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
    lookupServiceData.exclude = true;
    m_lookupServiceTypeField.setLayoutData(lookupServiceData);

    return group;
  }

  private void updateSharedBundle() {
    DefaultProposalProvider lookupTypeProposalProvider = new DefaultProposalProvider();
    if (getSharedBundle() != null) {
      ICachedTypeHierarchy lookupServiceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iLookupService);
      IType[] lookupServices = lookupServiceHierarchy.getAllInterfaces(TypeFilters.getTypesOnClasspath(getSharedBundle().getJavaProject()), TypeComparators.getTypeNameComparator());
      ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(lookupServices);
      lookupTypeProposalProvider = new DefaultProposalProvider(proposals);
    }

    ITypeProposal lookupTypeProp = (ITypeProposal) m_serviceSuperTypeField.getSelectedProposal();
    m_lookupServiceTypeField.setContentProposalProvider(lookupTypeProposalProvider);
    if (lookupTypeProp != null) {
      if (getSharedBundle().isOnClasspath(lookupTypeProp.getType())) {
        m_lookupServiceTypeField.acceptProposal(lookupTypeProp);
      }
    }
  }

  private void updateServerBundle() {
    DefaultProposalProvider superTypeProvider = new DefaultProposalProvider();
    if (getServerBundle() != null) {
      ITypeProposal[] shotList = ScoutProposalUtility.getScoutTypeProposalsFor(abstractLookupService, abstractSqlLookupService);
      ICachedTypeHierarchy lookupServiceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iLookupService);
      IType[] abstractLookupServices = lookupServiceHierarchy.getAllClasses(TypeFilters.getAbstractOnClasspath(getServerBundle().getJavaProject()), TypeComparators.getTypeNameComparator());
      ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(abstractLookupServices);
      superTypeProvider = new DefaultProposalProvider(shotList, proposals);
    }

    ITypeProposal superTypeProp = (ITypeProposal) m_serviceSuperTypeField.getSelectedProposal();
    m_serviceSuperTypeField.setContentProposalProvider(superTypeProvider);
    if (superTypeProp != null) {
      if (getServerBundle().isOnClasspath(superTypeProp.getType())) {
        m_serviceSuperTypeField.acceptProposal(superTypeProp);
      }
    }

  }

  @Override
  public LookupCallNewWizard getWizard() {
    return (LookupCallNewWizard) super.getWizard();
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    try {
      multiStatus.add(getStatusNameField());
      multiStatus.add(getStatusSuperType());
      multiStatus.add(getStatusLookupService());
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("could not validate name field.", e);
    }
  }

  protected IStatus getStatusNameField() throws JavaModelException {
    if (StringUtility.isNullOrEmpty(getTypeName()) || getTypeName().equals(ScoutIdeProperties.SUFFIX_LOOKUP_CALL)) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_fieldNull"));
    }
    // check not allowed names
    if (ScoutSdk.existsType(getSharedBundle().getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_LOOKUP) + "." + getTypeName())) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
    }

    if (getTypeName().matches(Regex.REGEX_WELLFORMD_JAVAFIELD)) {
      return Status.OK_STATUS;
    }
    else if (getTypeName().matches(Regex.REGEX_JAVAFIELD)) {
      return new Status(IStatus.WARNING, ScoutSdk.PLUGIN_ID, Texts.get("Warning_notWellformedJavaName"));
    }
    else {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("Error_invalidFieldX", getTypeName()));
    }
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getLookupServiceStrategy() == LOOKUP_SERVICE_STRATEGY.CREATE_NEW && getServiceSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusLookupService() throws JavaModelException {
    if (getLookupServiceStrategy() == LOOKUP_SERVICE_STRATEGY.USE_EXISTING && getLookupServiceType() == null) {
      return new Status(IStatus.ERROR, ScoutSdk.PLUGIN_ID, Texts.get("TheLookupCallCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
  }

  public void setSharedBundle(IScoutBundle sharedBundle) {
    m_sharedBundle = sharedBundle;
    if (isControlCreated()) {
      updateSharedBundle();
    }
  }

  public IScoutBundle getServerBundle() {
    return m_serverBundle;
  }

  public void setServerBundle(IScoutBundle serverBundle) {
    m_serverBundle = serverBundle;
    if (isControlCreated()) {
      updateServerBundle();
    }
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

  public LOOKUP_SERVICE_STRATEGY getLookupServiceStrategy() {
    Object strategy = getProperty(PROP_LOOKUP_SERVICE_STRATEGY);
    if (!(strategy instanceof LOOKUP_SERVICE_STRATEGY)) {
      strategy = LOOKUP_SERVICE_STRATEGY.NO_SERVICE;
    }
    return (LOOKUP_SERVICE_STRATEGY) strategy;
  }

  public void setLookupServiceStrategy(LOOKUP_SERVICE_STRATEGY strategy) {
    try {
      if (strategy == null) {
        strategy = LOOKUP_SERVICE_STRATEGY.NO_SERVICE;
      }
      setStateChanging(true);
      setLookupServiceStrategyInternal(strategy);
      if (isControlCreated()) {
        switch (strategy) {
          case CREATE_NEW:
            m_createServiceButton.setSelection(true);
            break;
          case USE_EXISTING:
            m_useServiceButton.setSelection(true);
            break;
          case NO_SERVICE:
            m_noServiceButton.setSelection(true);
            break;
        }
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setLookupServiceStrategyInternal(LOOKUP_SERVICE_STRATEGY strategy) {
    setProperty(PROP_LOOKUP_SERVICE_STRATEGY, strategy);
  }

  public ITypeProposal getServiceSuperType() {
    return (ITypeProposal) getProperty(PROP_SERVICE_SUPER_TYPE);
  }

  public void setServiceSuperType(ITypeProposal superType) {
    try {
      setStateChanging(true);
      setServiceSuperTypeInternal(superType);
      if (isControlCreated()) {
        m_serviceSuperTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setServiceSuperTypeInternal(ITypeProposal superType) {
    setProperty(PROP_SERVICE_SUPER_TYPE, superType);
  }

  public ITypeProposal getLookupServiceType() {
    return (ITypeProposal) getProperty(PROP_LOOKUP_SERVICE);
  }

  public void setLookupServiceType(ITypeProposal lookupService) {
    try {
      setStateChanging(true);
      setLookupServiceTypeInternal(lookupService);
      if (isControlCreated()) {
        m_lookupServiceTypeField.acceptProposal(lookupService);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  private void setLookupServiceTypeInternal(ITypeProposal lookupService) {
    setProperty(PROP_LOOKUP_SERVICE, lookupService);
  }

  private class P_LookupServiceStrategyButtonListener extends SelectionAdapter {
    private final LOOKUP_SERVICE_STRATEGY m_strategy;

    public P_LookupServiceStrategyButtonListener(LOOKUP_SERVICE_STRATEGY strategy) {
      m_strategy = strategy;

    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      setLookupServiceStrategyInternal(m_strategy);
      switch (m_strategy) {
        case CREATE_NEW:
          m_serviceSuperTypeField.setVisible(true);
          ((GridData) m_serviceSuperTypeField.getLayoutData()).exclude = false;
          m_lookupServiceTypeField.setVisible(false);
          ((GridData) m_lookupServiceTypeField.getLayoutData()).exclude = true;
          break;
        case USE_EXISTING:
          m_serviceSuperTypeField.setVisible(false);
          ((GridData) m_serviceSuperTypeField.getLayoutData()).exclude = true;
          m_lookupServiceTypeField.setVisible(true);
          ((GridData) m_lookupServiceTypeField.getLayoutData()).exclude = false;
          break;
        case NO_SERVICE:
          m_serviceSuperTypeField.setVisible(false);
          ((GridData) m_serviceSuperTypeField.getLayoutData()).exclude = true;
          m_lookupServiceTypeField.setVisible(false);
          ((GridData) m_lookupServiceTypeField.getLayoutData()).exclude = true;
          break;
      }
      m_lookupServiceTypeField.getParent().layout(true);
      pingStateChanging();
    }
  }
}
