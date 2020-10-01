/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.wizard;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.apidef.IClassNameSupplier;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.fields.FieldToolkit;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.PackageContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.util.ApiHelper;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * <h3>{@link AbstractCompilationUnitNewWizardPage}</h3>
 *
 * @since 5.2.0
 */
public abstract class AbstractCompilationUnitNewWizardPage extends AbstractWizardPage {

  private static final Pattern WELL_FORMED_JAVA_FIELD = Pattern.compile("\\b[A-Z][a-zA-Z0-9_]{0,200}\\b");
  private static final Pattern JAVA_FIELD = Pattern.compile("\\b[A-Za-z_][a-zA-Z0-9_]{0,200}\\b");

  public static final String PROP_SOURCE_FOLDER = "sourceFolder";
  public static final String PROP_TARGET_PACKAGE = "targetPackage";
  public static final String PROP_JAVA_PROJECT = "javaProject";
  public static final String PROP_ICU_NAME = "icuName";
  public static final String PROP_SUPER_TYPE = "superType";
  public static final String PROP_SUPER_TYPE_BASE = "superTypeBase";

  public static final String PREF_SUPER_TYPE = "superType";

  private final ScoutTier m_sourceFolderTier;
  private IScoutApi m_scoutApi;
  private String m_superTypeDefaultBase;
  private String m_superTypeDefault;
  private String m_readOnlySuffix;
  private String m_icuGroupName;

  // ui fields
  private ProposalTextField m_sourceFolderField;
  private ProposalTextField m_packageField;
  private StyledTextField m_nameField;
  private ProposalTextField m_superTypeField;
  private Group m_icuGroupField;

  protected AbstractCompilationUnitNewWizardPage(String pageName, PackageContainer packageContainer, String readOnlySuffix, ScoutTier sourceFolderTier) {
    super(Ensure.notNull(pageName));
    m_readOnlySuffix = Ensure.notNull(readOnlySuffix);
    m_sourceFolderTier = Ensure.notNull(sourceFolderTier);
    if (packageContainer.getPackage() != null) {
      setTargetPackageInternal(packageContainer.getPackage().getElementName());
    }
    setSourceFolderInternal(packageContainer.getSrcFolder());
  }

  @Override
  protected void createContent(Composite parent) {
    GridLayoutFactory
        .swtDefaults()
        .applyTo(parent);
    createIcuGroup(parent);
  }

  protected int getLabelWidth() {
    return 100;
  }

  protected void createIcuGroup(Composite p) {
    String groupName = getIcuGroupName();
    if (Strings.isBlank(groupName)) {
      groupName = "New Class Details";
    }
    m_icuGroupField = FieldToolkit.createGroupBox(p, groupName);

    int labelWidth = getLabelWidth();
    boolean enabled = JdtUtils.exists(getSourceFolder());

    // source folder
    m_sourceFolderField = FieldToolkit.createSourceFolderField(m_icuGroupField, "Source Folder", m_sourceFolderTier, labelWidth);
    m_sourceFolderField.acceptProposal(getSourceFolder());
    m_sourceFolderField.addProposalListener(proposal -> {
      setSourceFolderInternal((IPackageFragmentRoot) proposal);
      pingStateChanging();
    });

    // package
    m_packageField = FieldToolkit.createPackageField(m_icuGroupField, "Package", getJavaProject(), labelWidth);
    m_packageField.setText(getTargetPackage());
    m_packageField.addModifyListener(e -> {
      setTargetPackageInternal(m_packageField.getText());
      pingStateChanging();
    });
    m_packageField.setEnabled(enabled);

    // name
    m_nameField = FieldToolkit.createStyledTextField(m_icuGroupField, "Name", TextField.TYPE_LABEL, labelWidth);
    m_nameField.setText(getIcuName());
    m_nameField.setReadOnlySuffix(getReadOnlySuffix());
    m_nameField.addModifyListener(e -> {
      setIcuNameInternal(m_nameField.getText());
      pingStateChanging();
    });

    // super type
    IType superType = calcSuperTypeDefault();
    if (JdtUtils.exists(superType)) {
      setSuperTypeInternal(superType);
    }
    m_superTypeField = FieldToolkit.createAbstractTypeProposalField(m_icuGroupField, "Super Class", getJavaProject(), getSuperTypeBaseClass(), labelWidth);
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalListener(proposal -> {
      setSuperTypeInternal((IType) proposal);
      pingStateChanging();
    });
    m_superTypeField.setEnabled(enabled);

    // layout
    GridLayoutFactory
        .swtDefaults()
        .applyTo(m_icuGroupField);
    applyFieldLayoutData(m_icuGroupField);
    applyFieldLayoutData(m_sourceFolderField);
    applyFieldLayoutData(m_packageField);
    applyFieldLayoutData(m_nameField);
    applyFieldLayoutData(m_superTypeField);
  }

  protected static void applyFieldLayoutData(Control c) {
    GridDataFactory
        .defaultsFor(c)
        .align(SWT.FILL, SWT.CENTER)
        .grab(true, false)
        .applyTo(c);
  }

  protected void handleSuperTypeChanged() {
    // callback for subclasses invoked when the super type is changed
  }

  protected void handleSourceFolderChanged() {
    // callback for subclasses invoked when the source folder is changed
  }

  protected void handleTargetPackageChanged() {
    // callback for subclasses invoked when the target package is changed
  }

  protected void handleIcuNameChanged() {
    // callback for subclasses invoked when the class name is changed
  }

  protected IType calcSuperTypeDefault() {
    IType defaultSuperType = null;
    String prefSuperTypeFqn = getDialogSettings().get(PREF_SUPER_TYPE);
    if (Strings.hasText(prefSuperTypeFqn)) {
      defaultSuperType = resolveType(prefSuperTypeFqn);
    }
    if (!JdtUtils.exists(defaultSuperType)) {
      defaultSuperType = resolveType(getSuperTypeDefault()); // fallback to default
    }
    return defaultSuperType;
  }

  protected IType resolveType(String fqn) {
    return resolveType(getJavaProject(), fqn);
  }

  protected static IType resolveType(IJavaProject javaProject, String fqn) {
    if (!JdtUtils.exists(javaProject)) {
      return null;
    }

    try {
      return javaProject.findType(fqn);
    }
    catch (JavaModelException e) {
      SdkLog.info("Could not find default super type {} in project {}.", fqn, javaProject.getElementName(), e);
    }
    return null;
  }

  @Override
  public boolean performFinish() {
    getDialogSettings().put(PREF_SUPER_TYPE, getSuperType().getFullyQualifiedName());
    return true;
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (!visible) {
      return;
    }

    // initial focus
    if (m_sourceFolderField.getSelectedProposal() == null) {
      m_sourceFolderField.setFocus();
    }
    else if (Strings.isBlank(m_packageField.getText())) {
      m_packageField.setFocus();
    }
    else {
      m_nameField.setFocus();
    }
  }

  protected void handleJavaProjectChanged() {
    IJavaProject javaProject = getJavaProject();
    if (javaProject == null) {
      m_scoutApi = null;
    }
    else {
      m_scoutApi = ApiHelper.requireScoutApiFor(javaProject, null);
    }
    m_superTypeDefault = calcSuperTypeDefaultFqn().map(IClassNameSupplier::fqn).orElse(null);
    String newSuperTypeDefaultBase = calcSuperTypeDefaultBaseFqn().map(IClassNameSupplier::fqn).orElse(null);
    if (!Objects.equals(m_superTypeDefaultBase, newSuperTypeDefaultBase)) {
      m_superTypeDefaultBase = newSuperTypeDefaultBase;
      setSuperTypeBaseClass(newSuperTypeDefaultBase);
    }

    if (!isControlCreated()) {
      return;
    }
    boolean isEnabled = JdtUtils.exists(javaProject);
    m_packageField.setEnabled(isEnabled);
    PackageContentProvider packageContentProvider = (PackageContentProvider) m_packageField.getContentProvider();
    packageContentProvider.setJavaProject(javaProject);
    m_packageField.setText(null);

    m_superTypeField.setEnabled(isEnabled);
    StrictHierarchyTypeContentProvider superTypeContentProvider = (StrictHierarchyTypeContentProvider) m_superTypeField.getContentProvider();
    superTypeContentProvider.setJavaProject(javaProject);
    m_superTypeField.acceptProposal(getSuperType(), true, true);
    if (m_superTypeField.getSelectedProposal() == null) {
      m_superTypeField.acceptProposal(calcSuperTypeDefault());
    }
  }

  protected abstract Optional<IClassNameSupplier> calcSuperTypeDefaultFqn();

  protected abstract Optional<IClassNameSupplier> calcSuperTypeDefaultBaseFqn();

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusSourceFolder());
    multiStatus.add(getStatusPackage());
    multiStatus.add(getStatusName());
    multiStatus.add(getStatusSuperType());
  }

  protected IStatus getStatusSourceFolder() {
    if (!JdtUtils.exists(getSourceFolder())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose a source folder.");
    }
    return Status.OK_STATUS;
  }

  protected IStatus getStatusPackage() {
    return validatePackageName(getTargetPackage());
  }

  protected IStatus getStatusName() {
    IStatus javaFieldNameStatus = validateJavaName(getIcuName(), getReadOnlySuffix());
    if (javaFieldNameStatus.getSeverity() > IStatus.WARNING) {
      return javaFieldNameStatus;
    }
    IStatus existingStatus = validateTypeNotExisting(getSourceFolder(), getTargetPackage(), getIcuName());
    if (!existingStatus.isOK()) {
      return existingStatus;
    }
    return javaFieldNameStatus;
  }

  protected IStatus getStatusSuperType() {
    if (!JdtUtils.exists(getSuperType())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose a super class.");
    }
    return Status.OK_STATUS;
  }

  protected ProposalTextField getSourceFolderField() {
    return m_sourceFolderField;
  }

  protected ProposalTextField getPackageField() {
    return m_packageField;
  }

  protected StyledTextField getNameField() {
    return m_nameField;
  }

  protected ProposalTextField getSuperTypeField() {
    return m_superTypeField;
  }

  /**
   * Gets the status of the given java name. Checks if a name that differs from the suffix is entered and that the name
   * is a valid java name.
   *
   * @param name
   *          The name to check
   * @param suffix
   *          The suffix to compare against.
   * @return A status that describes the state of the given name
   */
  public static IStatus validateJavaName(String name, String suffix) {
    if (Strings.isBlank(name) || name.equals(suffix)) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please specify a class name.");
    }
    if (WELL_FORMED_JAVA_FIELD.matcher(name).matches()) {
      return Status.OK_STATUS;
    }
    else if (JAVA_FIELD.matcher(name).matches()) {
      return new Status(IStatus.WARNING, S2ESdkActivator.PLUGIN_ID, "Name should start with upper case.");
    }
    else {
      // "package-info.java" will not be valid which is what we want
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Name not valid.");
    }
  }

  private static String getContainingJavaKeyWord(String s) {
    for (String keyWord : JavaTypes.getJavaKeyWords()) {
      if (s.startsWith(keyWord + JavaTypes.C_DOT) || s.endsWith(JavaTypes.C_DOT + keyWord) || s.contains(JavaTypes.C_DOT + keyWord + JavaTypes.C_DOT)) {
        return keyWord;
      }
    }
    return null;
  }

  /**
   * Gets the validation status for a potential new class in the given source folder, below the given package with given
   * name.<br>
   * The method does no check for java classes, but checks if there already exists a resource with the target name (case
   * insensitive).
   *
   * @param srcFolder
   *          The source folder
   * @param pck
   *          The package under which the type would be created.
   * @param typeName
   *          the name of the potential type.
   * @return the {@link Status#OK_STATUS} if no file exists at the target location with the given name. An error status
   *         otherwise.
   */
  @SuppressWarnings("pmd:NPathComplexity")
  public static IStatus validateTypeNotExisting(IPackageFragmentRoot srcFolder, String pck, String typeName) {
    if (Strings.isBlank(typeName)) {
      return Status.OK_STATUS;
    }
    if (!JdtUtils.exists(srcFolder)) {
      return Status.OK_STATUS;
    }
    if (pck == null) {
      pck = "";
    }

    IPackageFragment packageFragment = srcFolder.getPackageFragment(pck);
    if (!JdtUtils.exists(packageFragment)) {
      return Status.OK_STATUS;
    }

    IResource folder = packageFragment.getResource();
    if (folder == null || !folder.exists()) {
      return Status.OK_STATUS;
    }

    boolean[] elementFound = new boolean[1];

    if (folder.exists()) {
      String typeNameComplete = typeName + JavaTypes.JAVA_FILE_SUFFIX;
      try {
        folder.accept(new IResourceProxyVisitor() {
          boolean m_selfVisited;

          @Override
          public boolean visit(IResourceProxy proxy) {
            if (proxy.getType() == IResource.FOLDER) {
              if (!m_selfVisited) {
                m_selfVisited = true;
                return true;
              }
              return false;
            }
            if (proxy.getType() == IResource.FILE && typeNameComplete.equalsIgnoreCase(proxy.getName())) {
              elementFound[0] = true;
            }
            return false;
          }
        }, IResource.DEPTH_ONE, IResource.NONE);
      }
      catch (CoreException e) {
        SdkLog.warning("Unable to check if the type '{}' already exists.", typeName, e);
      }
    }

    if (elementFound[0]) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Name '" + typeName + "' is already used. Choose another name.");
    }
    return Status.OK_STATUS;
  }

  /**
   * Gets an {@link IStatus} describing the given package name
   *
   * @param pckName
   *          The package name to validate
   * @return An {@link IStatus} describing the given package name.
   */
  public static IStatus validatePackageName(String pckName) {
    if (Strings.isBlank(pckName)) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The default package is not allowed.");
    }
    String invalidPackageName = "The package name is not valid.";
    // no double points
    if (pckName.contains("..")) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, invalidPackageName);
    }
    // invalid characters
    Pattern regexPackageName = Pattern.compile("^[0-9a-zA-Z._]*$");
    if (!regexPackageName.matcher(pckName).matches()) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, invalidPackageName);
    }
    // no start and end with number or special characters
    Pattern regexPackageNameStart = Pattern.compile("[a-zA-Z].*$");
    Pattern regexPackageNameEnd = Pattern.compile("^.*[a-zA-Z]$");
    if (!regexPackageNameStart.matcher(pckName).matches() || !regexPackageNameEnd.matcher(pckName).matches()) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, invalidPackageName);
    }
    // reserved java keywords
    String jkw = getContainingJavaKeyWord(pckName);
    if (jkw != null) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The package may not contain a reserved Java keyword: '" + jkw + '\'');
    }
    // warn containing upper case characters
    Pattern regexContainsUpperCase = Pattern.compile(".*[A-Z].*");
    if (regexContainsUpperCase.matcher(pckName).matches()) {
      return new Status(IStatus.WARNING, S2ESdkActivator.PLUGIN_ID, "The package should contain only lower case characters.");
    }
    return Status.OK_STATUS;
  }

  public IJavaProject getJavaProject() {
    return getProperty(PROP_JAVA_PROJECT, IJavaProject.class);
  }

  protected void setJavaProjectInternal(IJavaProject javaProject) {
    if (setProperty(PROP_JAVA_PROJECT, javaProject)) {
      handleJavaProjectChanged();
    }
  }

  public IPackageFragmentRoot getSourceFolder() {
    return getProperty(PROP_SOURCE_FOLDER, IPackageFragmentRoot.class);
  }

  public void setSourceFolder(IPackageFragmentRoot sourceFolder) {
    setPropertyWithChangingControl(m_sourceFolderField, () -> setSourceFolderInternal(sourceFolder), field -> field.acceptProposal(sourceFolder));
  }

  protected boolean setSourceFolderInternal(@SuppressWarnings("TypeMayBeWeakened") IPackageFragmentRoot sourceFolder) {
    if (setProperty(PROP_SOURCE_FOLDER, sourceFolder)) {
      handleSourceFolderChanged();
      if (sourceFolder != null) {
        setJavaProjectInternal(sourceFolder.getJavaProject());
      }
      else {
        setJavaProjectInternal(null);
      }
      return true;
    }
    return false;
  }

  public String getTargetPackage() {
    return getProperty(PROP_TARGET_PACKAGE, String.class);
  }

  public void setTargetPackage(String targetPackage) {
    setPropertyWithChangingControl(m_packageField, () -> setTargetPackageInternal(targetPackage), field -> field.setText(targetPackage));
  }

  protected boolean setTargetPackageInternal(String targetPackage) {
    if (setProperty(PROP_TARGET_PACKAGE, targetPackage)) {
      handleTargetPackageChanged();
      return true;
    }
    return false;
  }

  public String getIcuName() {
    return getProperty(PROP_ICU_NAME, String.class);
  }

  public void setIcuName(String name) {
    setPropertyWithChangingControl(m_nameField, () -> setIcuNameInternal(name), field -> field.setText(name));
  }

  protected boolean setIcuNameInternal(String name) {
    if (setProperty(PROP_ICU_NAME, name)) {
      handleIcuNameChanged();
      return true;
    }
    return false;
  }

  public IType getSuperType() {
    return getProperty(PROP_SUPER_TYPE, IType.class);
  }

  public void setSuperType(IType superType) {
    setPropertyWithChangingControl(m_superTypeField, () -> setSuperTypeInternal(superType), field -> field.acceptProposal(superType));
  }

  protected boolean setSuperTypeInternal(IType superType) {
    if (setProperty(PROP_SUPER_TYPE, superType)) {
      handleSuperTypeChanged();
      return true;
    }
    return false;
  }

  public String getSuperTypeBaseClass() {
    return getProperty(PROP_SUPER_TYPE_BASE, String.class);
  }

  public void setSuperTypeBaseClass(String className) {
    setPropertyWithChangingControl(m_superTypeField, () -> setSuperTypeBaseClassInternal(className), field -> {
      StrictHierarchyTypeContentProvider superTypeContentProvider = (StrictHierarchyTypeContentProvider) m_superTypeField.getContentProvider();
      superTypeContentProvider.setBaseClassFqn(className);
      m_superTypeField.acceptProposal(calcSuperTypeDefault());
    });
  }

  protected boolean setSuperTypeBaseClassInternal(String className) {
    return setProperty(PROP_SUPER_TYPE_BASE, className);
  }

  public Optional<IScoutApi> scoutApi() {
    return Optional.ofNullable(m_scoutApi);
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
  }

  public void setReadOnlySuffix(String newSuffix) {
    m_readOnlySuffix = newSuffix;
    if (m_nameField != null) {
      m_nameField.setReadOnlySuffix(newSuffix);
    }
  }

  public String getIcuGroupName() {
    return m_icuGroupName;
  }

  public void setIcuGroupName(String icuGroupName) {
    m_icuGroupName = icuGroupName;
  }

  public String getSuperTypeDefault() {
    return m_superTypeDefault;
  }

  public String getSuperTypeDefaultBase() {
    return m_superTypeDefaultBase;
  }

  protected Group getIcuGroupComposite() {
    return m_icuGroupField;
  }
}
