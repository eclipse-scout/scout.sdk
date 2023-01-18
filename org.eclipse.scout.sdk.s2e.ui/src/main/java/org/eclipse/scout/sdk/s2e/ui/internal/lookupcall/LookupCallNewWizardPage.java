/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.lookupcall;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractCompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.JdtUtils.PublicAbstractPrimaryTypeFilter;
import org.eclipse.scout.sdk.s2e.util.S2eTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link LookupCallNewWizardPage}</h3>
 *
 * @since 5.2.0
 */
public class LookupCallNewWizardPage extends AbstractCompilationUnitNewWizardPage {

  public static final String PROP_SVC_IMPL_SUPER_TYPE_BASE = "svcImplSuperTypeBase";
  public static final String PROP_SVC_IMPL_SUPER_TYPE = "svcImplSuperType";
  public static final String PROP_SERVER_SOURCE_FOLDER = "serverSourceFolder";
  public static final String PROP_KEY_TYPE = "keyType";
  public static final String PROP_SERVER_JAVA_PROJECT = "serverJavaProject";

  private EclipseEnvironment m_provider;

  protected ProposalTextField m_lookupServiceSuperTypeField;
  protected ProposalTextField m_serverSourceFolder;
  protected ProposalTextField m_keyTypeField;

  public LookupCallNewWizardPage(PackageContainer packageContainer) {
    super(LookupCallNewWizardPage.class.getName(), packageContainer, ISdkConstants.SUFFIX_LOOKUP_CALL, ScoutTier.Shared);
    setTitle("Create a new LookupCall");
    setDescription(getTitle());
    setIcuGroupName("New LookupCall Details");
  }

  @Override
  protected Optional<ITypeNameSupplier> calcSuperTypeDefaultFqn() {
    return scoutApi().map(IScoutApi::LookupCall);
  }

  @Override
  protected Optional<ITypeNameSupplier> calcSuperTypeDefaultBaseFqn() {
    return scoutApi().map(IScoutApi::ILookupCall);
  }

  @Override
  public LookupCallNewWizard getWizard() {
    return (LookupCallNewWizard) super.getWizard();
  }

  @Override
  protected void createContent(Composite parent) {
    super.createContent(parent);
    m_provider = EclipseEnvironment.createUnsafe(env -> getControl().addDisposeListener(e -> env.close()));

    // change the filter for the super types to also include the LookupCall class as proposal (which is not abstract and would be excluded otherwise)
    var superTypeContentProvider = (StrictHierarchyTypeContentProvider) getSuperTypeField().getContentProvider();
    superTypeContentProvider.setTypeProposalFilter(new PublicAbstractPrimaryTypeFilter() {
      @Override
      public boolean test(IType candidate) {
        var lookupCallFqn = scoutApi().map(IScoutApi::LookupCall).map(ITypeNameSupplier::fqn).orElse(null);
        return JdtUtils.exists(candidate) && (candidate.getFullyQualifiedName().equals(lookupCallFqn) || super.test(candidate));
      }
    });

    guessServerFolders();
    createLookupCallPropertiesGroup(parent);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IScoutHelpContextIds.SCOUT_LOOKUPCALL_NEW_WIZARD_PAGE);
  }

  protected IType calcServiceImplSuperTypeDefault() {
    return scoutApi()
        .map(IScoutApi::AbstractLookupService)
        .map(ITypeNameSupplier::fqn)
        .map(fqn -> resolveType(getServerJavaProject(), fqn))
        .orElse(null);
  }

  protected void createLookupCallPropertiesGroup(Composite p) {
    var parent = FieldToolkit.createGroupBox(p, "Lookup Service");

    // server source folder
    m_serverSourceFolder = FieldToolkit.createSourceFolderField(parent, "Server Source Folder", ScoutTier.Server, getLabelWidth());
    m_serverSourceFolder.acceptProposal(getServerSourceFolder());
    m_serverSourceFolder.addProposalListener(proposal -> {
      setServerSourceFolderInternal((IPackageFragmentRoot) proposal);
      pingStateChanging();
    });

    // Lookup Service Super Type
    var superType = calcServiceImplSuperTypeDefault();
    if (JdtUtils.exists(superType)) {
      setServiceImplSuperTypeInternal(superType);
    }
    m_lookupServiceSuperTypeField = FieldToolkit.createAbstractTypeProposalField(parent, "Service Super Class", getServerJavaProject(), getServiceImplSuperTypeBaseClass(), getLabelWidth());
    m_lookupServiceSuperTypeField.acceptProposal(getServiceImplSuperType());
    m_lookupServiceSuperTypeField.setEnabled(JdtUtils.exists(getServerJavaProject()));
    m_lookupServiceSuperTypeField.addProposalListener(proposal -> {
      setServiceImplSuperTypeInternal((IType) proposal);
      pingStateChanging();
    });

    m_keyTypeField = FieldToolkit.createTypeProposalField(getIcuGroupComposite(), "Key Class", getJavaProject(), getLabelWidth());
    m_keyTypeField.addProposalListener(proposal -> {
      setKeyTypeInternal((IType) proposal);
      pingStateChanging();
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

  public IJavaProject getServerJavaProject() {
    return getProperty(PROP_SERVER_JAVA_PROJECT, IJavaProject.class);
  }

  protected void setServerJavaProjectInternal(IJavaProject javaProject) {
    if (setProperty(PROP_SERVER_JAVA_PROJECT, javaProject)) {
      handleServerJavaProjectChanged();
    }
  }

  protected void guessServerFolders() {
    var sharedSourceFolder = getSourceFolder();
    if (!JdtUtils.exists(sharedSourceFolder)) {
      return;
    }

    setServerSourceFolder(S2eTier.wrap(ScoutTier.Shared).convert(ScoutTier.Server, sharedSourceFolder).orElse(null));
  }

  protected void handleServerJavaProjectChanged() {
    if (!isControlCreated()) {
      return;
    }
    m_lookupServiceSuperTypeField.setEnabled(JdtUtils.exists(getServerJavaProject()));
    var superTypeContentProvider = (StrictHierarchyTypeContentProvider) m_lookupServiceSuperTypeField.getContentProvider();
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
    setServiceImplSuperTypeBaseClassInternal(scoutApi().map(IScoutApi::ILookupService).map(ITypeNameSupplier::fqn).orElse(null));

    if (!isControlCreated()) {
      return;
    }

    ((StrictHierarchyTypeContentProvider) m_keyTypeField.getContentProvider()).setJavaProject(getJavaProject());
    m_keyTypeField.setEnabled(JdtUtils.exists(getJavaProject()));
  }

  protected void syncKeyTypeFieldToSuperType() {
    var superType = getSuperType();
    if (!JdtUtils.exists(superType)) {
      m_keyTypeField.setEnabled(false);
    }
    else {
      var typeParameters = m_provider.toScoutType(superType).typeParameters().toList();
      var typeParamAvailable = !typeParameters.isEmpty();
      m_keyTypeField.setEnabled(typeParamAvailable);
      if (typeParamAvailable) {
        var bounds = typeParameters.get(0).bounds().toList();
        var typeContentProvider = (StrictHierarchyTypeContentProvider) m_keyTypeField.getContentProvider();
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
    multiStatus.add(getStatusServerVisibility());
  }

  protected IStatus getStatusServerVisibility() {
    if (!JdtUtils.exists(getSourceFolder())) {
      return Status.OK_STATUS;
    }
    if (!JdtUtils.exists(getServerSourceFolder())) {
      return Status.OK_STATUS;
    }

    if (!getServerSourceFolder().getJavaProject().isOnClasspath(getSourceFolder().getJavaProject())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The LookupCall Source Folder is not accessible from the selected Server Source Folder.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusServerSourceFolder() {
    if (JdtUtils.exists(getServerSourceFolder()) != JdtUtils.exists(getServiceImplSuperType())) {
      return new Status(IStatus.WARNING, S2ESdkActivator.PLUGIN_ID, "A Lookup Service is only created if a server source folder and a service super class are selected.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusLookupServiceSuperTypeField() {
    // check if key type and service super type argument are compatible
    var serviceImplSuperType = getServiceImplSuperType();
    if (!JdtUtils.exists(serviceImplSuperType) || !JdtUtils.exists(getKeyType())) {
      return Status.OK_STATUS;
    }

    var scoutSuperType = m_provider.toScoutType(serviceImplSuperType);
    var iLookupService = scoutApi().orElseThrow().ILookupService();
    var superClassKeyValue = scoutSuperType.resolveTypeParamValue(iLookupService.keyTypeTypeParamIndex(), iLookupService.fqn());
    if (superClassKeyValue.isEmpty()) {
      return Status.OK_STATUS;
    }

    var bound = superClassKeyValue.orElseThrow().findFirst();
    if (bound.isEmpty() || Object.class.getName().equals(bound.orElseThrow().name())) {
      return Status.OK_STATUS;
    }

    var keyType = m_provider.toScoutType(getKeyType());
    if (bound.orElseThrow().isAssignableFrom(keyType)) {
      return Status.OK_STATUS;
    }
    return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The selected service super class cannot be used with the selected key class.");
  }

  protected IStatus getStatusKeyTypeField() {
    if (!JdtUtils.exists(getKeyType())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose a key class.");
    }
    return Status.OK_STATUS;
  }

  public IType getServiceImplSuperType() {
    return getProperty(PROP_SVC_IMPL_SUPER_TYPE, IType.class);
  }

  public void setServiceImplSuperType(IType superType) {
    setPropertyWithChangingControl(m_lookupServiceSuperTypeField, () -> setServiceImplSuperTypeInternal(superType), field -> field.acceptProposal(superType));
  }

  protected boolean setServiceImplSuperTypeInternal(IType superType) {
    return setProperty(PROP_SVC_IMPL_SUPER_TYPE, superType);
  }

  public String getServiceImplSuperTypeBaseClass() {
    return getProperty(PROP_SVC_IMPL_SUPER_TYPE_BASE, String.class);
  }

  public void setServiceImplSuperTypeBaseClass(String className) {
    setPropertyWithChangingControl(m_lookupServiceSuperTypeField, () -> setServiceImplSuperTypeBaseClassInternal(className), field -> {
      var superTypeContentProvider = (StrictHierarchyTypeContentProvider) m_lookupServiceSuperTypeField.getContentProvider();
      superTypeContentProvider.setBaseClassFqn(className);
      field.acceptProposal(calcServiceImplSuperTypeDefault());
    });
  }

  protected boolean setServiceImplSuperTypeBaseClassInternal(String className) {
    return setProperty(PROP_SVC_IMPL_SUPER_TYPE_BASE, className);
  }

  public IPackageFragmentRoot getServerSourceFolder() {
    return getProperty(PROP_SERVER_SOURCE_FOLDER, IPackageFragmentRoot.class);
  }

  public void setServerSourceFolder(IPackageFragmentRoot serverSourceFolder) {
    setPropertyWithChangingControl(m_serverSourceFolder, () -> setServerSourceFolderInternal(serverSourceFolder), field -> field.acceptProposal(serverSourceFolder));
  }

  protected boolean setServerSourceFolderInternal(@SuppressWarnings("TypeMayBeWeakened") IPackageFragmentRoot serverSourceFolder) {
    if (setProperty(PROP_SERVER_SOURCE_FOLDER, serverSourceFolder)) {
      if (serverSourceFolder != null) {
        setServerJavaProjectInternal(serverSourceFolder.getJavaProject());
      }
      else {
        setServerJavaProjectInternal(null);
      }
      return true;
    }
    return false;
  }

  public IType getKeyType() {
    return getProperty(PROP_KEY_TYPE, IType.class);
  }

  public void setKeyType(IType keyType) {
    setPropertyWithChangingControl(m_keyTypeField, () -> setKeyTypeInternal(keyType), field -> field.acceptProposal(keyType));
  }

  protected boolean setKeyTypeInternal(IType keyType) {
    return setProperty(PROP_KEY_TYPE, keyType);
  }
}
