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
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.operation.util.ResourceDeleteOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

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
  public void validate() {
    if (getServiceImplementation() == null || !getServiceImplementation().exists()) {
      throw new IllegalArgumentException("service implementation can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JavaElementDeleteOperation javaMemberDeleteOperation = new JavaElementDeleteOperation();
    ResourceDeleteOperation resourceDeleteOperation = new ResourceDeleteOperation();
    javaMemberDeleteOperation.addMember(getServiceImplementation());
    if (getServiceInterface() != null) {
      javaMemberDeleteOperation.addMember(getServiceInterface());
      // unregister client side
      ScoutUtility.unregisterServiceProxy(getServiceInterface());
    }
    // unregister server side
    ScoutUtility.unregisterServiceImplementation(getServiceImplementation());

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
