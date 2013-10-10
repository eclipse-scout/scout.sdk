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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.javacode.EntityTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.MoreElementsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
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

  private final IType iLookupService = TypeUtility.getType(RuntimeClasses.ILookupService);
  private final IType abstractLookupService = TypeUtility.getType(RuntimeClasses.AbstractLookupService);

  public static final String PROP_TYPE_NAME = "typeName";
  public static final String PROP_SERVICE_SUPER_TYPE = "serviceSuperType";
  public static final String PROP_LOOKUP_SERVICE = "lookupService";
  public static final String PROP_LOOKUP_SERVICE_STRATEGY = "lookupServiceStrategy";
  public static final String PROP_TARGET_PACKAGE = "targetPackage";

  // ui fields
  private StyledTextField m_typeNameField;
  private Button m_createServiceButton;
  private Button m_useServiceButton;
  private Button m_noServiceButton;
  private ProposalTextField m_serviceSuperTypeField;
  private ProposalTextField m_lookupServiceTypeField;
  private EntityTextField m_entityField;

  // process members
  private IType m_abstractSqlLookupService;
  private final IScoutBundle m_sharedBundle;
  private final IScoutBundle m_serverBundle;

  public LookupCallNewWizardPage(IScoutBundle sharedBundle, IScoutBundle serverBundle) {
    super(LookupCallNewWizardPage.class.getName());
    m_sharedBundle = sharedBundle;
    m_serverBundle = serverBundle;
    setTargetPackage(DefaultTargetPackage.get(sharedBundle, IDefaultTargetPackage.SHARED_SERVICES_LOOKUP));
    setTitle(Texts.get("NewLookupCall"));
    setDescription(Texts.get("CreateANewLookupCall"));
    if (serverBundle != null) {
      setLookupServiceStrategy(LOOKUP_SERVICE_STRATEGY.CREATE_NEW);
      m_abstractSqlLookupService = RuntimeClasses.getSuperType(RuntimeClasses.ILookupService, serverBundle.getJavaProject());
      setServiceSuperTypeInternal(m_abstractSqlLookupService);
    }
    else {
      setLookupServiceStrategy(LOOKUP_SERVICE_STRATEGY.NO_SERVICE);
    }
  }

  @Override
  protected void createContent(Composite parent) {
    int labelPercentage = 20;
    m_typeNameField = getFieldToolkit().createStyledTextField(parent, Texts.get("TypeName"), labelPercentage);
    m_typeNameField.setReadOnlySuffix(SdkProperties.SUFFIX_LOOKUP_CALL);
    m_typeNameField.setText(getTypeName());
    m_typeNameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTypeNameInternal(m_typeNameField.getText());
        pingStateChanging();
      }
    });

    if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
      m_entityField = getFieldToolkit().createEntityTextField(parent, Texts.get("EntityTextField"), m_sharedBundle, labelPercentage);
      m_entityField.setText(getTargetPackage());
      m_entityField.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          setTargetPackageInternal(m_entityField.getText());
          pingStateChanging();
        }
      });
      m_entityField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    }

    Control lookupServiceGroup = createLookupServiceGroup(parent);

    // layout
    parent.setLayout(new GridLayout(1, true));

    m_typeNameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    lookupServiceGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  protected Control createLookupServiceGroup(Composite parent) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText("Lookup Service");

    boolean serverAvailable = getServerBundle() != null;

    Composite radioButtons = new Composite(group, SWT.INHERIT_DEFAULT);
    m_createServiceButton = new Button(radioButtons, SWT.RADIO);
    m_createServiceButton.addSelectionListener(new P_LookupServiceStrategyButtonListener(LOOKUP_SERVICE_STRATEGY.CREATE_NEW));
    m_createServiceButton.setText("create new lookup service");
    m_createServiceButton.setSelection(serverAvailable);
    m_useServiceButton = new Button(radioButtons, SWT.RADIO);
    m_useServiceButton.addSelectionListener(new P_LookupServiceStrategyButtonListener(LOOKUP_SERVICE_STRATEGY.USE_EXISTING));
    m_useServiceButton.setText("use existing lookup service");
    m_noServiceButton = new Button(radioButtons, SWT.RADIO);
    m_noServiceButton.addSelectionListener(new P_LookupServiceStrategyButtonListener(LOOKUP_SERVICE_STRATEGY.NO_SERVICE));
    m_noServiceButton.setText("no lookup service");
    m_noServiceButton.setSelection(!serverAvailable);

    m_serviceSuperTypeField = getFieldToolkit().createProposalField(group, Texts.get("LookupServiceSuperType"));
    if (serverAvailable) {
      AbstractJavaElementContentProvider contentProvider = new AbstractJavaElementContentProvider() {
        @Override
        protected Object[][] computeProposals() {
          List<Object> proposals = new ArrayList<Object>();
          proposals.add(abstractLookupService);
          if (m_abstractSqlLookupService != null) {
            proposals.add(m_abstractSqlLookupService);
          }
          proposals.add(MoreElementsProposal.INSTANCE);
          ICachedTypeHierarchy lookupServiceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iLookupService);
          IType[] abstractLookupServices = lookupServiceHierarchy.getAllClasses(TypeFilters.getAbstractOnClasspath(getServerBundle().getJavaProject()), TypeComparators.getTypeNameComparator());
          for (IType t : abstractLookupServices) {
            if (!proposals.contains(t)) {
              proposals.add(t);
            }
          }
          return new Object[][]{proposals.toArray(new Object[proposals.size()])};
        }
      };
      m_serviceSuperTypeField.setContentProvider(contentProvider);
      m_serviceSuperTypeField.setLabelProvider(contentProvider.getLabelProvider());
      m_serviceSuperTypeField.acceptProposal(getServiceSuperType());
    }
    else {
      m_serviceSuperTypeField.setEnabled(false);
      radioButtons.setEnabled(false);
    }
    m_serviceSuperTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setServiceSuperTypeInternal((IType) event.proposal);
        pingStateChanging();
      }
    });

    m_lookupServiceTypeField = getFieldToolkit().createProposalField(group, Texts.get("LookupService"));
    if (getSharedBundle() != null) {
      AbstractJavaElementContentProvider contentProvider = new AbstractJavaElementContentProvider() {
        @Override
        protected Object[][] computeProposals() {
          ICachedTypeHierarchy lookupServiceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iLookupService);
          IType[] lookupServices = lookupServiceHierarchy.getAllInterfaces(TypeFilters.getTypesOnClasspath(getSharedBundle().getJavaProject()), TypeComparators.getTypeNameComparator());
          return new Object[][]{lookupServices};
        }
      };
      m_lookupServiceTypeField.setContentProvider(contentProvider);
      m_lookupServiceTypeField.setLabelProvider(contentProvider.getLabelProvider());
    }
    else {
      m_lookupServiceTypeField.setEnabled(false);
    }
    m_lookupServiceTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setLookupServiceTypeInternal((IType) event.proposal);
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
    IStatus javaFieldNameStatus = ScoutUtility.getJavaNameStatus(getTypeName(), SdkProperties.SUFFIX_LOOKUP_CALL);
    if (javaFieldNameStatus.isOK()) {
      if (TypeUtility.existsType(getSharedBundle().getPackageName(getTargetPackage()) + "." + getTypeName())) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("Error_nameAlreadyUsed"));
      }
    }
    return javaFieldNameStatus;
  }

  protected IStatus getStatusSuperType() throws JavaModelException {
    if (getLookupServiceStrategy() == LOOKUP_SERVICE_STRATEGY.CREATE_NEW && getServiceSuperType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheSuperTypeCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusLookupService() throws JavaModelException {
    if (getLookupServiceStrategy() == LOOKUP_SERVICE_STRATEGY.USE_EXISTING && getLookupServiceType() == null) {
      return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("TheLookupCallCanNotBeNull"));
    }
    return Status.OK_STATUS;
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
  }

  public IScoutBundle getServerBundle() {
    return m_serverBundle;
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

  public IType getServiceSuperType() {
    return (IType) getProperty(PROP_SERVICE_SUPER_TYPE);
  }

  public void setServiceSuperType(IType superType) {
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

  private void setServiceSuperTypeInternal(IType superType) {
    setProperty(PROP_SERVICE_SUPER_TYPE, superType);
  }

  public IType getLookupServiceType() {
    return (IType) getProperty(PROP_LOOKUP_SERVICE);
  }

  public void setLookupServiceType(IType lookupService) {
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

  private void setLookupServiceTypeInternal(IType lookupService) {
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

  public String getTargetPackage() {
    return (String) getProperty(PROP_TARGET_PACKAGE);
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
    setProperty(PROP_TARGET_PACKAGE, targetPackage);
  }
}
