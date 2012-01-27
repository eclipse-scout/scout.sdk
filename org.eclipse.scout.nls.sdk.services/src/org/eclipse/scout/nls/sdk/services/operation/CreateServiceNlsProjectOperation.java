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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.services.internal.NlsSdkService;
import org.eclipse.scout.nls.sdk.services.model.ws.NlsServiceType;
import org.eclipse.scout.nls.sdk.simple.operations.CreateSimpleNlsProjectOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class CreateServiceNlsProjectOperation implements IOperation {

  private IType m_superType;
  private IScoutBundle m_bundle;
  private String m_translationFolder;
  private String m_translationFilePrefix;
  private String m_serviceName;
  private String[] m_languages;

  public CreateServiceNlsProjectOperation() {
  }

  @Override
  public String getOperationName() {
    return "Create new NLS Service Project...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_superType == null) {
      throw new IllegalArgumentException("super type not set.");
    }
    if (m_bundle == null) {
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
    IFolder folder = getBundle().getProject().getFolder(getTranslationFolder());
    for (String lang : getLanguages()) {
      CreateSimpleNlsProjectOperation.createLanguageFile(lang, folder, getTranslationFilePrefix(), monitor);
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // ensure sync
    getBundle().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

    // create language translation properties files
    createLanguageFiles(monitor);

    // create and register text provider service
    ServiceNewOperation serviceOp = new ServiceNewOperation();
    serviceOp.addServiceRegistrationBundle(getBundle());
    serviceOp.setImplementationBundle(getBundle());
    serviceOp.setServiceName(getServiceName());
    serviceOp.setServicePackageName(getBundle().getProject().getName() + NlsSdkService.TEXT_SERVICE_PACKAGE_SUFFIX);
    serviceOp.setServiceSuperTypeSignature(Signature.createTypeSignature(getSuperType().getFullyQualifiedName(), true));
    serviceOp.validate();
    serviceOp.run(monitor, workingCopyManager);

    // override abstract method with path to resources
    MethodOverrideOperation methodOp = new MethodOverrideOperation(serviceOp.getCreatedServiceImplementation(), NlsServiceType.DYNAMIC_NLS_BASE_NAME_GETTER, true);
    methodOp.setSimpleBody("return \"" + CreateSimpleNlsProjectOperation.getResourcePathString(getTranslationFolder(), getTranslationFilePrefix()) + "\";");
    methodOp.validate();
    methodOp.run(monitor, workingCopyManager);

    // we have changed the NLS service hierarchy: clear the cache so that it will be re-created next time including our just created service.
    getBundle().getScoutProject().clearNlsProjectCache();
  }

  public IType getSuperType() {
    return m_superType;
  }

  public void setSuperType(IType superType) {
    m_superType = superType;
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
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

  public void setServiceName(String serviceName) {
    m_serviceName = serviceName;
  }

  public void setLanguages(String[] languages) {
    m_languages = languages;
  }

  public String[] getLanguages() {
    return m_languages;
  }
}
