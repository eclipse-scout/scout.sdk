/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.lookupcall;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.IJavaRuntimeTypes;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.CachingJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.IJavaEnvironmentProvider;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IProposalListener;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.TypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils.PublicAbstractPrimaryTypeFilter;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link LookupCallNewWizardPage}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class LookupCallNewWizardPage extends CompilationUnitNewWizardPage {

  public static final String PROP_SVC_IMPL_SUPER_TYPE_BASE = "svcImplSuperTypeBase";
  public static final String PROP_SVC_IMPL_SUPER_TYPE = "svcImplSuperType";
  public static final String PROP_SERVER_SOURCE_FOLDER = "serverSourceFolder";
  public static final String PROP_KEY_TYPE = "keyType";
  public static final String PROP_SERVER_JAVA_PROJECT = "serverJavaProject";

  private final IJavaEnvironmentProvider m_provider;

  protected ProposalTextField m_lookupServiceSuperTypeField;
  protected ProposalTextField m_serverSourceFolder;
  protected ProposalTextField m_keyTypeField;

  public LookupCallNewWizardPage(PackageContainer packageContainer) {
    super(LookupCallNewWizardPage.class.getName(), packageContainer, ISdkProperties.SUFFIX_LOOKUP_CALL, IScoutRuntimeTypes.ILookupCall, IScoutRuntimeTypes.LookupCall, ScoutTier.Shared);
    m_provider = new CachingJavaEnvironmentProvider();
    setTitle("Create a new LookupCall");
    setDescription(getTitle());
    setIcuGroupName("New LookupCall Details");
    setServiceImplSuperTypeBaseClassInternal(IScoutRuntimeTypes.ILookupService);
  }

  @Override
  public LookupCallNewWizard getWizard() {
    return (LookupCallNewWizard) super.getWizard();
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);

    // change the filter for the super types to also include the LookupCall class as proposal (which is not abstract and would be excluded otherwise)
    StrictHierarchyTypeContentProvider superTypeContentProvider = (StrictHierarchyTypeContentProvider) getSuperTypeField().getContentProvider();
    superTypeContentProvider.setTypeProposalFilter(new PublicAbstractPrimaryTypeFilter() {
      @Override
      public boolean evaluate(IType candidate) {
        if (!S2eUtils.exists(candidate)) {
          return false;
        }
        if (IScoutRuntimeTypes.LookupCall.equals(candidate.getFullyQualifiedName())) {
          return true;
        }
        return super.evaluate(candidate);
      }
    });

    guessServerFolders();
    createLookupCallPropertiesGroup(parent);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_LOOKUPCALL_NEW_WIZARD_PAGE);
  }

  protected IType calcServiceImplSuperTypeDefault() {
    return resolveType(getServerJavaProject(), IScoutRuntimeTypes.AbstractLookupService);
  }

  protected void createLookupCallPropertiesGroup(Composite p) {
    Group parent = getFieldToolkit().createGroupBox(p, "Lookup Service");

    // server source folder
    m_serverSourceFolder = getFieldToolkit().createSourceFolderField(parent, "Server Source Folder", ScoutTier.Server, getLabelWidth());
    m_serverSourceFolder.acceptProposal(getServerSourceFolder());
    m_serverSourceFolder.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setServerSourceFolderInternal((IPackageFragmentRoot) proposal);
        pingStateChanging();
      }
    });

    // Lookup Service Super Type
    IType superType = calcServiceImplSuperTypeDefault();
    if (S2eUtils.exists(superType)) {
      setServiceImplSuperTypeInternal(superType);
    }
    m_lookupServiceSuperTypeField = getFieldToolkit().createAbstractTypeProposalField(parent, "Service Super Class", getServerJavaProject(), getServiceImplSuperTypeBaseClass(), getLabelWidth());
    m_lookupServiceSuperTypeField.acceptProposal(getServiceImplSuperType());
    m_lookupServiceSuperTypeField.setEnabled(S2eUtils.exists(getServerJavaProject()));
    m_lookupServiceSuperTypeField.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setServiceImplSuperTypeInternal((IType) proposal);
        pingStateChanging();
      }
    });

    m_keyTypeField = getFieldToolkit().createTypeProposalField(getIcuGroupComposite(), "Key Class", getJavaProject(), getLabelWidth());
    m_keyTypeField.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setKeyTypeInternal((IType) proposal);
        pingStateChanging();
      }
    });

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);
    GridDataFactory
        .defaultsFor(parent)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .indent(0, 10)
        .applyTo(parent);
    GridDataFactory
        .defaultsFor(m_serverSourceFolder)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .span(3, 0)
        .applyTo(m_serverSourceFolder);
    GridDataFactory
        .defaultsFor(m_lookupServiceSuperTypeField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_lookupServiceSuperTypeField);
    GridDataFactory
        .defaultsFor(m_keyTypeField)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(m_keyTypeField);

    syncKeyTypeFieldToSuperType();
  }

  @Override
  protected int getLabelWidth() {
    return 120;
  }

  @Override
  protected void handleSuperTypeChanged() {
    super.handleSuperTypeChanged();
    if (isControlCreated()) {
      syncKeyTypeFieldToSuperType();
    }
  }

  protected IJavaEnvironment getEnvironment() {
    return m_provider.get(getJavaProject());
  }

  public IJavaProject getServerJavaProject() {
    return getProperty(PROP_SERVER_JAVA_PROJECT, IJavaProject.class);
  }

  protected void setServerJavaProjectInternal(IJavaProject javaProject) {
    if (setProperty(PROP_SERVER_JAVA_PROJECT, javaProject)) {
      handleServerJavaProjectChanged();
    }
  }

  protected void guessServerFolders() {
    IPackageFragmentRoot sharedSourceFolder = getSourceFolder();
    if (!S2eUtils.exists(sharedSourceFolder)) {
      return;
    }

    try {
      setServerSourceFolder(ScoutTier.Shared.convert(ScoutTier.Server, sharedSourceFolder));
    }
    catch (JavaModelException e) {
      SdkLog.info("Unable to calculate server source folder.", e);
    }
  }

  protected void handleServerJavaProjectChanged() {
    if (!isControlCreated()) {
      return;
    }
    m_lookupServiceSuperTypeField.setEnabled(S2eUtils.exists(getServerJavaProject()));
    StrictHierarchyTypeContentProvider superTypeContentProvider = (StrictHierarchyTypeContentProvider) m_lookupServiceSuperTypeField.getContentProvider();
    superTypeContentProvider.setJavaProject(getServerJavaProject());
    m_lookupServiceSuperTypeField.acceptProposal(getServiceImplSuperType(), true, true);
    if (m_lookupServiceSuperTypeField.getSelectedProposal() == null) {
      m_lookupServiceSuperTypeField.acceptProposal(calcServiceImplSuperTypeDefault());
    }
  }

  @Override
  protected void handleJavaProjectChanged() {
    super.handleJavaProjectChanged();

    guessServerFolders();

    if (!isControlCreated()) {
      return;
    }

    ((TypeContentProvider) m_keyTypeField.getContentProvider()).setJavaProject(getJavaProject());
    m_keyTypeField.setEnabled(S2eUtils.exists(getJavaProject()));
  }

  protected void syncKeyTypeFieldToSuperType() {
    IType superType = getSuperType();
    if (!S2eUtils.exists(superType)) {
      m_keyTypeField.setEnabled(false);
    }
    else {
      List<ITypeParameter> typeParameters = S2eUtils.jdtTypeToScoutType(superType, getEnvironment()).typeParameters();
      boolean typeParamAvailable = typeParameters.size() > 0;
      m_keyTypeField.setEnabled(typeParamAvailable);
      if (typeParamAvailable) {
        List<org.eclipse.scout.sdk.core.model.api.IType> bounds = typeParameters.get(0).bounds();
        TypeContentProvider typeContentProvider = (TypeContentProvider) m_keyTypeField.getContentProvider();
        if (bounds.isEmpty()) {
          typeContentProvider.setBaseClassFqn(null);
        }
        else {
          typeContentProvider.setBaseClassFqn(bounds.get(0).name());
        }
      }
    }
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    super.validatePage(multiStatus);
    multiStatus.add(getStatusServerSourceFolder());
    multiStatus.add(getStatusLookupServiceSuperTypeField());
    multiStatus.add(getStatusKeyTypeField());
  }

  protected IStatus getStatusServerSourceFolder() {
    if (S2eUtils.exists(getServerSourceFolder()) != S2eUtils.exists(getServiceImplSuperType())) {
      return new Status(IStatus.WARNING, S2ESdkActivator.PLUGIN_ID, "A Lookup Service is only created if a server source folder and a service super class are selected.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusLookupServiceSuperTypeField() {
    // check if key type and service super type argument are compatible
    IType serviceImplSuperType = getServiceImplSuperType();
    if (S2eUtils.exists(serviceImplSuperType) && S2eUtils.exists(getKeyType())) {
      org.eclipse.scout.sdk.core.model.api.IType scoutSuperType = S2eUtils.jdtTypeToScoutType(serviceImplSuperType, m_provider.get(getServerJavaProject()));
      List<org.eclipse.scout.sdk.core.model.api.IType> superClassKeyValue = CoreUtils.getResolvedTypeParamValue(scoutSuperType, IScoutRuntimeTypes.ILookupService, IScoutRuntimeTypes.TYPE_PARAM_LOOKUP_SERVICE_KEY_TYPE);
      if (!superClassKeyValue.isEmpty()) {
        org.eclipse.scout.sdk.core.model.api.IType bound = superClassKeyValue.get(0);
        if (!IJavaRuntimeTypes.Object.equals(bound.name())) {
          org.eclipse.scout.sdk.core.model.api.IType keyType = S2eUtils.jdtTypeToScoutType(getKeyType(), getEnvironment());
          if (!bound.isAssignableFrom(keyType)) {
            return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The selected service super class cannot be used with the selected key class");
          }
        }
      }
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusKeyTypeField() {
    if (!S2eUtils.exists(getKeyType())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose a key class.");
    }
    return Status.OK_STATUS;
  }

  public IType getServiceImplSuperType() {
    return getProperty(PROP_SVC_IMPL_SUPER_TYPE, IType.class);
  }

  public void setServiceImplSuperType(IType superType) {
    try {
      setStateChanging(true);
      setServiceImplSuperTypeInternal(superType);
      if (isControlCreated() && m_lookupServiceSuperTypeField != null) {
        m_lookupServiceSuperTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setServiceImplSuperTypeInternal(IType superType) {
    setProperty(PROP_SVC_IMPL_SUPER_TYPE, superType);
  }

  public String getServiceImplSuperTypeBaseClass() {
    return getProperty(PROP_SVC_IMPL_SUPER_TYPE_BASE, String.class);
  }

  public void setServiceImplSuperTypeBaseClass(String className) {
    try {
      setStateChanging(true);
      setServiceImplSuperTypeBaseClassInternal(className);
      if (isControlCreated() && m_lookupServiceSuperTypeField != null) {
        StrictHierarchyTypeContentProvider superTypeContentProvider = (StrictHierarchyTypeContentProvider) m_lookupServiceSuperTypeField.getContentProvider();
        superTypeContentProvider.setBaseClassFqn(className);
        m_lookupServiceSuperTypeField.acceptProposal(calcServiceImplSuperTypeDefault());
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setServiceImplSuperTypeBaseClassInternal(String className) {
    setProperty(PROP_SVC_IMPL_SUPER_TYPE_BASE, className);
  }

  public IPackageFragmentRoot getServerSourceFolder() {
    return getProperty(PROP_SERVER_SOURCE_FOLDER, IPackageFragmentRoot.class);
  }

  public void setServerSourceFolder(IPackageFragmentRoot serverSourceFolder) {
    try {
      setStateChanging(true);
      setServerSourceFolderInternal(serverSourceFolder);
      if (isControlCreated() && m_serverSourceFolder != null) {
        m_serverSourceFolder.acceptProposal(serverSourceFolder);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setServerSourceFolderInternal(IPackageFragmentRoot serverSourceFolder) {
    setProperty(PROP_SERVER_SOURCE_FOLDER, serverSourceFolder);

    if (serverSourceFolder != null) {
      setServerJavaProjectInternal(serverSourceFolder.getJavaProject());
    }
    else {
      setServerJavaProjectInternal(null);
    }
  }

  public IType getKeyType() {
    return getProperty(PROP_KEY_TYPE, IType.class);
  }

  public void setKeyType(IType keyType) {
    try {
      setStateChanging(true);
      setKeyTypeInternal(keyType);
      if (isControlCreated() && m_keyTypeField != null) {
        m_keyTypeField.acceptProposal(keyType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setKeyTypeInternal(IType keyType) {
    setProperty(PROP_KEY_TYPE, keyType);
  }
}
