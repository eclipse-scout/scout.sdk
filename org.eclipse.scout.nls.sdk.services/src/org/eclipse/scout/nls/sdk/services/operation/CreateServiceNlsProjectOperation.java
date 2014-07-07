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
package org.eclipse.scout.nls.sdk.services.operation;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.simple.operations.CreateSimpleNlsProjectOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class CreateServiceNlsProjectOperation implements IOperation {

  private final String m_serviceName;
  private final String m_packageName;
  private final IJavaProject m_javaProject;
  private IType m_superType;
  private String m_translationFolder;
  private String m_translationFilePrefix;
  private String[] m_languages;

  private IType m_createdServiceType;

  public CreateServiceNlsProjectOperation(String serviceName, String packagename, IJavaProject javaProject) {
    m_serviceName = serviceName;
    m_packageName = packagename;
    m_javaProject = javaProject;
  }

  @Override
  public String getOperationName() {
    return "Create new NLS Service Project...";
  }

  @Override
  public void validate() {
    if (m_superType == null) {
      throw new IllegalArgumentException("super type not set.");
    }
    if (m_javaProject == null) {
      throw new IllegalArgumentException("bundle not set.");
    }
    if (StringUtility.isNullOrEmpty(getTranslationFolder())) {
      throw new IllegalArgumentException("translation folder not set.");
    }
    if (StringUtility.isNullOrEmpty(getTranslationFilePrefix())) {
      throw new IllegalArgumentException("translation file prefix not set.");
    }
    if (StringUtility.isNullOrEmpty(getServiceName())) {
      throw new IllegalArgumentException("service name not set.");
    }
    if (m_languages == null || m_languages.length < 1) {
      throw new IllegalArgumentException("no languages set.");
    }
  }

  private void createLanguageFiles(IProgressMonitor monitor) throws CoreException {
    IFolder folder = getJavaProject().getProject().getFolder(getTranslationFolder());
    for (String lang : getLanguages()) {
      CreateSimpleNlsProjectOperation.createLanguageFile(lang, folder, getTranslationFilePrefix(), monitor);
    }
  }

  private void addBuildEntry() throws CoreException {
    IFolder folder = getJavaProject().getProject().getFolder(getTranslationFolder());

    PluginModelHelper pmh = new PluginModelHelper(getJavaProject().getProject());

    // check if the folder is already exported
    IContainer checkFolder = folder;
    while (getJavaProject().getProject().getLocation().isPrefixOf(checkFolder.getLocation())) {
      if (pmh.BuildProperties.existsBinaryBuildEntry(checkFolder)) {
        return;
      }
      checkFolder = checkFolder.getParent();
    }

    String fld = getTranslationFolder();
    if (!fld.endsWith("/")) {
      fld = fld + "/";
    }
    pmh.BuildProperties.addBinaryBuildEntry(fld);
    pmh.save();
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // ensure sync
    getJavaProject().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

    // create language translation properties files
    createLanguageFiles(monitor);

    // add the text files to the build properties (if not already existing)
    addBuildEntry();

    // create and register text provider service
    ServiceNewOperation serviceOp = new ServiceNewOperation(null, getServiceName());
    serviceOp.setImplementationProject(getJavaProject());
    serviceOp.setImplementationPackageName(getPackageName());
    serviceOp.addServiceRegistration(new ServiceRegistrationDescription(getJavaProject()));
    serviceOp.setImplementationSuperTypeSignature(SignatureCache.createTypeSignature(getSuperType().getFullyQualifiedName()));
    // getDynamicNlsBaseName method
    IMethodSourceBuilder getDynamicNlsBaseNameBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(serviceOp.getImplementationSourceBuilder(), "getDynamicNlsBaseName");
    getDynamicNlsBaseNameBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createSimpleMethodBody("return \"" + CreateSimpleNlsProjectOperation.getResourcePathString(getTranslationFolder(), getTranslationFilePrefix()) + "\";"));
    serviceOp.getImplementationSourceBuilder().addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodAnyKey(getDynamicNlsBaseNameBuilder), getDynamicNlsBaseNameBuilder);
    serviceOp.validate();
    serviceOp.run(monitor, workingCopyManager);
    m_createdServiceType = serviceOp.getCreatedServiceImplementation();

    // wait until all events have been fired and handled
    ResourcesPlugin.getWorkspace().checkpoint(false);
    JdtUtility.waitForIndexesReady();
  }

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  public String getTranslationFolder() {
    return m_translationFolder;
  }

  public void setTranslationFolder(String translationFolder) {
    m_translationFolder = translationFolder;
  }

  public String getTranslationFilePrefix() {
    return m_translationFilePrefix;
  }

  public void setTranslationFilePrefix(String translationFilePrefix) {
    m_translationFilePrefix = translationFilePrefix;
  }

  public String getServiceName() {
    return m_serviceName;
  }

  public void setLanguages(String[] languages) {
    m_languages = languages;
  }

  public String[] getLanguages() {
    return m_languages;
  }

  public IType getCreatedServiceType() {
    return m_createdServiceType;
  }

  public String getPackageName() {
    return m_packageName;
  }

}
