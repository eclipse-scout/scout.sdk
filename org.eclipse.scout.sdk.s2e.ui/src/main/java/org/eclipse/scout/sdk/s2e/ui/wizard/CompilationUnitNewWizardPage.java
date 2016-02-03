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
package org.eclipse.scout.sdk.s2e.ui.wizard;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFolder;
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
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.IProposalListener;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.PackageContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.util.PackageContainer;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * <h3>{@link CompilationUnitNewWizardPage}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class CompilationUnitNewWizardPage extends AbstractWizardPage {

  private static final Pattern WELLFORMD_JAVAFIELD = Pattern.compile("\\b[A-Z][a-zA-Z0-9_]{0,200}\\b");
  private static final Pattern JAVAFIELD = Pattern.compile("\\b[A-Za-z_][a-zA-Z0-9_]{0,200}\\b");

  public static final String PROP_SOURCE_FOLDER = "sourceFolder";
  public static final String PROP_TARGET_PACKAGE = "targetPackage";
  public static final String PROP_JAVA_PROJECT = "javaProject";
  public static final String PROP_ICU_NAME = "icuName";
  public static final String PROP_SUPER_TYPE = "superType";
  public static final String PROP_SUPER_TYPE_BASE = "superTypeBase";

  public static final String PREF_SUPER_TYPE = "superType";

  private final String m_readOnlySuffix;
  private final String m_superTypeDefaultBase;
  private final String m_defaultSuperType;
  private final ScoutTier m_sourceFolderTier;

  private String m_icuGroupName;

  // ui fields
  private ProposalTextField m_sourceFolderField;
  private ProposalTextField m_packageField;
  private StyledTextField m_nameField;
  private ProposalTextField m_superTypeField;
  private Group m_icuGroupField;

  public CompilationUnitNewWizardPage(String pageName, PackageContainer packageContainer, String readOnlySuffix, String superTypeBaseClass, String defaultSuperType, ScoutTier sourceFolderTier) {
    super(Validate.notNull(pageName));
    m_readOnlySuffix = Validate.notNull(readOnlySuffix);
    m_superTypeDefaultBase = superTypeBaseClass;
    m_defaultSuperType = Validate.notNull(defaultSuperType);
    m_sourceFolderTier = Validate.notNull(sourceFolderTier);
    setSuperTypeBaseClassInternal(superTypeBaseClass);
    if (packageContainer.getPackage() != null) {
      setTargetPackageInternal(packageContainer.getPackage().getElementName());
    }
    setSourceFolderInternal(packageContainer.getSrcFolder());
  }

  @Override
  protected void createContent(Composite parent) {
    parent.setLayout(new GridLayout(1, true));

    createIcuGroup(parent);
  }

  protected void createIcuGroup(Composite p) {
    String groupName = getIcuGroupName();
    if (StringUtils.isBlank(groupName)) {
      groupName = "New Class Details";
    }
    m_icuGroupField = getFieldToolkit().createGroupBox(p, groupName);
    m_icuGroupField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_icuGroupField.setLayout(new GridLayout(1, true));

    int labelColWidthPercent = 20;
    boolean enabled = S2eUtils.exists(getSourceFolder());

    // source folder
    m_sourceFolderField = getFieldToolkit().createSourceFolderTextField(m_icuGroupField, "Source Folder", m_sourceFolderTier, labelColWidthPercent);
    m_sourceFolderField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_sourceFolderField.acceptProposal(getSourceFolder());
    m_sourceFolderField.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setSourceFolderInternal((IPackageFragmentRoot) proposal);
        pingStateChanging();
      }
    });

    // package
    m_packageField = getFieldToolkit().createPackageTextField(m_icuGroupField, "Package", getJavaProject(), labelColWidthPercent);
    m_packageField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_packageField.setText(getTargetPackage());
    m_packageField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setTargetPackageInternal(m_packageField.getText());
        pingStateChanging();
      }
    });
    m_packageField.setEnabled(enabled);

    // name
    m_nameField = getFieldToolkit().createStyledTextField(m_icuGroupField, "Name", labelColWidthPercent);
    m_nameField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_nameField.setText(getIcuName());
    m_nameField.setReadOnlySuffix(getReadOnlySuffix());
    m_nameField.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setIcuNameInternal(m_nameField.getText());
        pingStateChanging();
      }
    });

    // super type
    IType superType = calcSuperTypeDefault();
    if (S2eUtils.exists(superType)) {
      setSuperTypeInternal(superType);
    }
    m_superTypeField = getFieldToolkit().createAbstractTypeProposalField(m_icuGroupField, "Super Class", getJavaProject(), getSuperTypeBaseClass());
    m_superTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_superTypeField.acceptProposal(getSuperType());
    m_superTypeField.addProposalListener(new IProposalListener() {
      @Override
      public void proposalAccepted(Object proposal) {
        setSuperTypeInternal((IType) proposal);
        pingStateChanging();
      }
    });
    m_superTypeField.setEnabled(enabled);
  }

  protected void handleSuperTypeChanged() {
  }

  protected void handleSourceFolderChanged() {
  }

  protected void handleTargetPackageChanged() {
  }

  protected void handleIcuNameChanged() {
  }

  protected IType calcSuperTypeDefault() {
    IType defaultSuperType = null;
    String prefSuperTypeFqn = getDialogSettings().get(PREF_SUPER_TYPE);
    if (StringUtils.isNotBlank(prefSuperTypeFqn)) {
      defaultSuperType = resolveType(prefSuperTypeFqn);
    }
    if (!S2eUtils.exists(defaultSuperType)) {
      defaultSuperType = resolveType(getSuperTypeDefault()); // fallback to default
    }
    return defaultSuperType;
  }

  protected IType resolveType(String fqn) {
    return resolveType(getJavaProject(), fqn);
  }

  protected static IType resolveType(IJavaProject javaProject, String fqn) {
    if (!S2eUtils.exists(javaProject)) {
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
    else if (StringUtils.isBlank(m_packageField.getText())) {
      m_packageField.setFocus();
    }
    else {
      m_nameField.setFocus();
    }
  }

  protected void handleJavaProjectChanged() {
    if (!isControlCreated()) {
      return;
    }
    boolean isEnabled = S2eUtils.exists(getJavaProject());
    m_packageField.setEnabled(isEnabled);
    PackageContentProvider packageContentProvider = (PackageContentProvider) m_packageField.getContentProvider();
    packageContentProvider.setJavaProject(getJavaProject());
    m_packageField.setText(null);

    m_superTypeField.setEnabled(isEnabled);
    StrictHierarchyTypeContentProvider superTypeContentProvider = (StrictHierarchyTypeContentProvider) m_superTypeField.getContentProvider();
    superTypeContentProvider.setJavaProject(getJavaProject());
    m_superTypeField.acceptProposal(getSuperType(), true, true);
    if (m_superTypeField.getSelectedProposal() == null) {
      m_superTypeField.acceptProposal(calcSuperTypeDefault());
    }
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    multiStatus.add(getStatusSourceFolder());
    multiStatus.add(getStatusPackage());
    multiStatus.add(getStatusName());
    multiStatus.add(getStatusSuperType());
  }

  protected IStatus getStatusSourceFolder() {
    if (!S2eUtils.exists(getSourceFolder())) {
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
    if (!S2eUtils.exists(getSuperType())) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please choose a super class.");
    }
    return Status.OK_STATUS;
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
  protected static IStatus validateJavaName(String name, String suffix) {
    if (StringUtils.isBlank(name) || name.equals(suffix)) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Please specify a class name.");
    }
    if (WELLFORMD_JAVAFIELD.matcher(name).matches()) {
      return Status.OK_STATUS;
    }
    else if (JAVAFIELD.matcher(name).matches()) {
      return new Status(IStatus.WARNING, S2ESdkActivator.PLUGIN_ID, "Name should start with upper case.");
    }
    else {
      // "package-info.java" will not be valid which is what we want
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "Name not valid.");
    }
  }

  private static String getContainingJavaKeyWord(String s) {
    for (String keyWord : CoreUtils.getJavaKeyWords()) {
      if (s.startsWith(keyWord + '.') || s.endsWith('.' + keyWord) || s.contains('.' + keyWord + '.')) {
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
  protected static IStatus validateTypeNotExisting(IPackageFragmentRoot srcFolder, String pck, String typeName) {
    if (StringUtils.isBlank(typeName)) {
      return Status.OK_STATUS;
    }
    if (!S2eUtils.exists(srcFolder)) {
      return Status.OK_STATUS;
    }
    if (pck == null) {
      pck = "";
    }

    IPackageFragment packageFragment = srcFolder.getPackageFragment(pck);
    if (!S2eUtils.exists(packageFragment)) {
      return Status.OK_STATUS;
    }

    IFolder folder = (IFolder) packageFragment.getResource();
    if (folder == null || !folder.exists()) {
      return Status.OK_STATUS;
    }

    final boolean[] elementFound = new boolean[1];
    final String typeNameComplete = typeName + SuffixConstants.SUFFIX_STRING_java;

    if (folder.exists()) {
      try {
        folder.accept(new IResourceProxyVisitor() {
          boolean selfVisited = false;

          @Override
          public boolean visit(IResourceProxy proxy) throws CoreException {
            if (proxy.getType() == IResource.FOLDER) {
              if (!selfVisited) {
                selfVisited = true;
                return true;
              }
              return false;
            }
            else if (proxy.getType() == IResource.FILE && typeNameComplete.equalsIgnoreCase(proxy.getName())) {
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
  protected static IStatus validatePackageName(String pckName) {
    if (StringUtils.isBlank(pckName)) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The default package is not allowed.");
    }
    String invalidPackageName = "The package name is not valid.";
    // no double points
    if (pckName.contains("..")) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, invalidPackageName);
    }
    // invalid characters
    Pattern regexPackageName = Pattern.compile("^[0-9a-zA-Z\\.\\_]*$");
    if (!regexPackageName.matcher(pckName).matches()) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, invalidPackageName);
    }
    // no start and end with number or special characters
    Pattern regexPackageNameStart = Pattern.compile("[a-zA-Z]{1}.*$");
    Pattern regesPackageNameEnd = Pattern.compile("^.*[a-zA-Z]{1}$");
    if (!regexPackageNameStart.matcher(pckName).matches() || !regesPackageNameEnd.matcher(pckName).matches()) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, invalidPackageName);
    }
    // reserved java keywords
    String jkw = getContainingJavaKeyWord(pckName);
    if (jkw != null) {
      return new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, "The package may not contain a reserved Java keyword: '" + jkw + "'");
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
    try {
      setStateChanging(true);
      setSourceFolderInternal(sourceFolder);
      if (isControlCreated() && m_sourceFolderField != null) {
        m_sourceFolderField.acceptProposal(sourceFolder);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setSourceFolderInternal(IPackageFragmentRoot sourceFolder) {
    if (setProperty(PROP_SOURCE_FOLDER, sourceFolder)) {
      handleSourceFolderChanged();
    }
    if (sourceFolder != null) {
      setJavaProjectInternal(sourceFolder.getJavaProject());
    }
    else {
      setJavaProjectInternal(null);
    }
  }

  public String getTargetPackage() {
    return getProperty(PROP_TARGET_PACKAGE, String.class);
  }

  public void setTargetPackage(String targetPackage) {
    try {
      setStateChanging(true);
      setTargetPackageInternal(targetPackage);
      if (isControlCreated() && m_packageField != null) {
        m_packageField.setText(targetPackage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setTargetPackageInternal(String targetPackage) {
    if (setProperty(PROP_TARGET_PACKAGE, targetPackage)) {
      handleTargetPackageChanged();
    }
  }

  public String getIcuName() {
    return getProperty(PROP_ICU_NAME, String.class);
  }

  public void setIcuName(String name) {
    try {
      setStateChanging(true);
      setIcuNameInternal(name);
      if (isControlCreated() && m_nameField != null) {
        m_nameField.setText(name);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setIcuNameInternal(String name) {
    if (setProperty(PROP_ICU_NAME, name)) {
      handleIcuNameChanged();
    }
  }

  public IType getSuperType() {
    return getProperty(PROP_SUPER_TYPE, IType.class);
  }

  public void setSuperType(IType superType) {
    try {
      setStateChanging(true);
      setSuperTypeInternal(superType);
      if (isControlCreated() && m_superTypeField != null) {
        m_superTypeField.acceptProposal(superType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setSuperTypeInternal(IType superType) {
    if (setProperty(PROP_SUPER_TYPE, superType)) {
      handleSuperTypeChanged();
    }
  }

  public String getSuperTypeBaseClass() {
    return getProperty(PROP_SUPER_TYPE_BASE, String.class);
  }

  public void setSuperTypeBaseClass(String className) {
    try {
      setStateChanging(true);
      setSuperTypeBaseClassInternal(className);
      if (isControlCreated() && m_superTypeField != null) {
        StrictHierarchyTypeContentProvider superTypeContentProvider = (StrictHierarchyTypeContentProvider) m_superTypeField.getContentProvider();
        superTypeContentProvider.setBaseClassFqn(className);
        m_superTypeField.acceptProposal(calcSuperTypeDefault());
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  protected void setSuperTypeBaseClassInternal(String className) {
    setProperty(PROP_SUPER_TYPE_BASE, className);
  }

  public String getSuperTypeDefault() {
    return m_defaultSuperType;
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
  }

  public String getIcuGroupName() {
    return m_icuGroupName;
  }

  public void setIcuGroupName(String icuGroupName) {
    m_icuGroupName = icuGroupName;
  }

  public String getSuperTypeDefaultBase() {
    return m_superTypeDefaultBase;
  }

  protected Group getIcuGroupComposite() {
    return m_icuGroupField;
  }
}
