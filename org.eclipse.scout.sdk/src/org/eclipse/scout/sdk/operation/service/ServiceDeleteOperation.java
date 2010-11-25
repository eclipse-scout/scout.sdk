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
package org.eclipse.scout.sdk.operation.service;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.util.ResourceDeleteOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class ServiceDeleteOperation implements IOperation {

  private IType m_serviceImplementation;
  private IType m_serviceInterface;
  private IType[] m_additionalTypesToBeDeleted;
  private IResource[] m_additionalResourcesToBeDeleted;

  public ServiceDeleteOperation() {
    m_additionalTypesToBeDeleted = new IType[0];
    m_additionalResourcesToBeDeleted = new IResource[0];
  }

  @Override
  public String getOperationName() {
    return "Delete Service...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getServiceImplementation() == null || !getServiceImplementation().exists()) {
      throw new IllegalArgumentException("service implementation can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    JavaElementDeleteOperation javaMemberDeleteOperation = new JavaElementDeleteOperation();
    ResourceDeleteOperation resourceDeleteOperation = new ResourceDeleteOperation();
    javaMemberDeleteOperation.addMember(getServiceImplementation());
    if (getServiceInterface() != null) {
      javaMemberDeleteOperation.addMember(getServiceInterface());
      // unregister client side
      IScoutBundle interfaceBundle = ScoutSdk.getScoutWorkspace().getScoutBundle(getServiceInterface().getJavaProject().getProject());
      for (IScoutBundle clientBundle : interfaceBundle.getDependentBundles(ScoutBundleFilters.getClientFilter(), false)) {
        ScoutUtility.unregisterServiceClass(clientBundle.getProject(), IScoutBundle.CLIENT_EXTENSION_POINT_SERVICE_PROXIES, IScoutBundle.CLIENT_EXTENSION_ELEMENT_SERVICE_PROXY, getServiceInterface().getFullyQualifiedName(), null, monitor);
      }
    }
    // unregister server side
    IScoutBundle implementationBundle = ScoutSdk.getScoutWorkspace().getScoutBundle(getServiceImplementation().getJavaProject().getProject());
    for (IScoutBundle serverBundle : implementationBundle.getRequiredBundles(ScoutBundleFilters.getServerFilter(), true)) {
      ScoutUtility.unregisterServiceClass(serverBundle.getProject(), IScoutBundle.EXTENSION_POINT_SERVICES, IScoutBundle.EXTENSION_ELEMENT_SERVICE, getServiceImplementation().getFullyQualifiedName(), serverBundle.getRootPackageName() + ".ServerSession", monitor);
    }

    for (IType type : getAdditionalTypesToBeDeleted()) {
      javaMemberDeleteOperation.addMember(type);
    }

    for (IResource resource : getAdditionalResourcesToBeDeleted()) {
      resourceDeleteOperation.addResource(resource);
    }

    javaMemberDeleteOperation.run(monitor, workingCopyManager);
    resourceDeleteOperation.run(monitor, workingCopyManager);

  }

  public void setServiceImplementation(IType serviceImplementation) {
    m_serviceImplementation = serviceImplementation;
  }

  public IType getServiceImplementation() {
    return m_serviceImplementation;
  }

  public void setServiceInterface(IType serviceInterface) {
    m_serviceInterface = serviceInterface;
  }

  public IType getServiceInterface() {
    return m_serviceInterface;
  }

  public IType[] getAdditionalTypesToBeDeleted() {
    return m_additionalTypesToBeDeleted;
  }

  public void setAdditionalTypesToBeDeleted(IType[] additionalTypesToBeDeleted) {
    m_additionalTypesToBeDeleted = additionalTypesToBeDeleted;
  }

  public IResource[] getAdditionalResourcesToBeDeleted() {
    return m_additionalResourcesToBeDeleted;
  }

  public void setAdditionalResourcesToBeDeleted(IResource[] additionalResourcesToBeDeleted) {
    m_additionalResourcesToBeDeleted = additionalResourcesToBeDeleted;
  }
}
