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
package org.eclipse.scout.sdk.s2e.ui.internal.lookupcall;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.model.api.ITypeParameter;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.ui.IScoutHelpContextIds;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.ui.wizard.CompilationUnitNewWizardPage;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.JdtUtils.PublicAbstractPrimaryTypeFilter;
import org.eclipse.scout.sdk.s2e.util.S2eScoutTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

/**
 * <h3>{@link LookupCallNewWizardPage}</h3>
 *
 * @since 5.2.0
 */
public class LookupCallNewWizardPage extends CompilationUnitNewWizardPage {

  public static final String PROP_SVC_IMPL_SUPER_TYPE_BASE = "svcImplSuperTypeBase";
  public static final String PROP_SVC_IMPL_SUPER_TYPE = "svcImplSuperType";
  public static final String PROP_SERVER_SOURCE_FOLDER = "serverSourceFolder";
  public static final String PROP_KEY_TYPE = "keyType";
  public static final String PROP_SERVER_JAVA_PROJECT = "serverJavaProject";

  private final EclipseEnvironment m_provider;

  protected ProposalTextField m_lookupServiceSuperTypeField;
  protected ProposalTextField m_serverSourceFolder;
  protected ProposalTextField m_keyTypeField;

  public LookupCallNewWizardPage(PackageContainer packageContainer) {
    super(LookupCallNewWizardPage.class.getName(), packageContainer, ISdkProperties.SUFFIX_LOOKUP_CALL, IScoutRuntimeTypes.ILookupCall, IScoutRuntimeTypes.LookupCall, ScoutTier.Shared);
    setTitle("Create a new LookupCall");
    setDescription(getTitle());
    setIcuGroupName("New LookupCall Details");
    setServiceImplSuperTypeBaseClassInternal(IScoutRuntimeTypes.ILookupService);
    m_provider = EclipseEnvironment.createUnsafe(env -> getControl().addDisposeListener(e -> env.close()));
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
      public boolean test(IType candidate) {
        return JdtUtils.exists(candidate) && (IScoutRuntimeTypes.LookupCall.equals(candidate.getFullyQualifiedName()) || super.test(candidate));
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
    Group parent = FieldToolkit.createGroupBox(p, "Lookup Service");

    // server source folder
    m_serverSourceFolder = FieldToolkit.createSourceFolderField(parent, "Server Source Folder", ScoutTier.Server, getLabelWidth());
    m_serverSourceFolder.acceptProposal(getServerSourceFolder());
    m_serverSourceFolder.addProposalListener(proposal -> {
      setServerSourceFolderInternal((IPackageFragmentRoot) proposal);
      pingStateChanging();
    });

    // Lookup Service Super Type
    IType superType = calcServiceImplSuperTypeDefault();
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
    IPackageFragmentRoot sharedSourceFolder = getSourceFolder();
    if (!JdtUtils.exists(sharedSourceFolder)) {
      return;
    }

    setServerSourceFolder(S2eScoutTier.wrap(ScoutTier.Shared).convert(ScoutTier.Server, sharedSourceFolder).orElse(null));
  }

  protected void handleServerJavaProjectChanged() {
    if (!isControlCreated()) {
      return;
    }
    m_lookupServiceSuperTypeField.setEnabled(JdtUtils.exists(getServerJavaProject()));
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

    ((StrictHierarchyTypeContentProvider) m_keyTypeField.getContentProvider()).setJavaProject(getJavaProject());
    m_keyTypeField.setEnabled(JdtUtils.exists(getJavaProject()));
  }

  protected void syncKeyTypeFieldToSuperType() {
    IType superType = getSuperType();
    if (!JdtUtils.exists(superType)) {
      m_keyTypeField.setEnabled(false);
    }
    else {
      List<ITypeParameter> typeParameters = m_provider.toScoutType(superType).typeParameters().collect(toList());
      boolean typeParamAvailable = !typeParameters.isEmpty();
      m_keyTypeField.setEnabled(typeParamAvailable);
      if (typeParamAvailable) {
        List<org.eclipse.scout.sdk.core.model.api.IType> bounds = typeParameters.get(0).bounds().collect(toList());
        StrictHierarchyTypeContentProvider typeContentProvider = (StrictHierarchyTypeContentProvider) m_keyTypeField.getContentProvider();
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
    IType serviceImplSuperType = getServiceImplSuperType();
    if (!JdtUtils.exists(serviceImplSuperType) || !JdtUtils.exists(getKeyType())) {
      return Status.OK_STATUS;
    }

    org.eclipse.scout.sdk.core.model.api.IType scoutSuperType = m_provider.toScoutType(serviceImplSuperType);
    Optional<Stream<org.eclipse.scout.sdk.core.model.api.IType>> superClassKeyValue = scoutSuperType.resolveTypeParamValue(IScoutRuntimeTypes.TYPE_PARAM_LOOKUP_SERVICE_KEY_TYPE, IScoutRuntimeTypes.ILookupService);
    if (!superClassKeyValue.isPresent()) {
      return Status.OK_STATUS;
    }

    Optional<org.eclipse.scout.sdk.core.model.api.IType> bound = superClassKeyValue.get().findFirst();
    if (!bound.isPresent() || Object.class.getName().equals(bound.get().name())) {
      return Status.OK_STATUS;
    }

    org.eclipse.scout.sdk.core.model.api.IType keyType = m_provider.toScoutType(getKeyType());
    if (bound.get().isAssignableFrom(keyType)) {
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
      StrictHierarchyTypeContentProvider superTypeContentProvider = (StrictHierarchyTypeContentProvider) m_lookupServiceSuperTypeField.getContentProvider();
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

  protected boolean setServerSourceFolderInternal(IPackageFragmentRoot serverSourceFolder) {
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
