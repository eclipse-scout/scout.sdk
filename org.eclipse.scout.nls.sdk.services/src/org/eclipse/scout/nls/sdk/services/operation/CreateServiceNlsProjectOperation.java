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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.nls.sdk.services.NlsSdkService;
import org.eclipse.scout.nls.sdk.services.model.ws.NlsServiceType;
import org.eclipse.scout.nls.sdk.simple.operations.CreateSimpleNlsProjectOperation;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

public class CreateServiceNlsProjectOperation implements IOperation {
  private final NewNlsServiceModel m_desc;

  public CreateServiceNlsProjectOperation(NewNlsServiceModel desc) {
    m_desc = desc;
  }

  @Override
  public String getOperationName() {
    return "Create new NLS Service Project";
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  private void createLanguageFiles(IProgressMonitor monitor) throws CoreException {
    IFolder folder = m_desc.getBundle().getProject().getFolder(m_desc.getTranslationFolder());
    for (String lang : m_desc.getLanguages()) {
      CreateSimpleNlsProjectOperation.createLanguageFile(lang, folder, m_desc.getTranlationFileName(), monitor);
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // ensure sync
    m_desc.getBundle().getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

    // create language translation properties files
    createLanguageFiles(monitor);

    // create and register text provider service
    ServiceNewOperation serviceOp = new ServiceNewOperation();
    serviceOp.addServiceRegistrationBundle(m_desc.getBundle());
    serviceOp.setImplementationBundle(m_desc.getBundle());
    serviceOp.setServiceName(m_desc.getClassName());
    serviceOp.setServicePackageName(m_desc.getBundle().getProject().getName() + NlsSdkService.TEXT_SERVICE_PACKAGE_SUFFIX);
    serviceOp.setServiceSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.AbstractDynamicNlsTextProviderService, true));
    serviceOp.validate();
    serviceOp.run(monitor, workingCopyManager);

    // override abstract method with path to resources
    MethodOverrideOperation methodOp = new MethodOverrideOperation(serviceOp.getCreatedServiceImplementation(), NlsServiceType.DYNAMIC_NLS_BASE_NAME_GETTER, true);
    methodOp.setSimpleBody("return \"" + CreateSimpleNlsProjectOperation.getResourcePathString(m_desc.getTranslationFolder(), m_desc.getTranlationFileName()) + "\";");
    methodOp.validate();
    methodOp.run(monitor, workingCopyManager);
  }
}
