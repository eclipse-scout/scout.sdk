/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.util.EclipseFileHandle;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class WsdlResource extends ManagedResource {

  private Definition m_wsdlDefinition;

  public WsdlResource(IScoutBundle bundle) {
    super(bundle.getProject());
  }

  public Definition loadWsdlDefinition() {
    synchronized (m_fileLock) {
      if (!existsFile()) {
        m_wsdlDefinition = null;
        return null;
      }

      if (m_wsdlDefinition == null || m_file.getModificationStamp() != m_modificationStamp) {
        m_modificationStamp = m_file.getModificationStamp();
        m_wsdlDefinition = JaxWsSdkUtility.loadWsdlDefinition(new EclipseFileHandle(m_file));
      }
    }

    return m_wsdlDefinition;
  }

  public void storeWsdlAsync(final Definition definition, final String notificationElement, final int notificationEvent) {
    IOperation op = new IOperation() {

      @Override
      public void validate() {
      }

      @Override
      public String getOperationName() {
        return "Store WSDL resource to disk";
      }

      @Override
      public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
        storeWsdl(definition, notificationElement, notificationEvent, monitor);
      }
    };
    new OperationJob(op).schedule();
  }

  public void storeWsdl(Definition definition, String notificationElement, int notificationEvent, IProgressMonitor monitor) throws CoreException {
    try {
      synchronized (m_fileLock) {
        if (m_file == null) {
          throw new CoreException(new ScoutStatus("File must not be null"));
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLWriter writer = factory.newWSDLWriter();
        writer.writeWSDL(definition, os);

        m_modificationStamp = ManagedResource.API_MODIFICATION_STAMP;
        try {
          JaxWsSdkUtility.ensureFileAccessibleAndRegistered(m_file, true);
          m_file.setContents(new ByteArrayInputStream(os.toByteArray()), true, true, monitor);
          m_wsdlDefinition = definition;
        }
        finally {
          m_modificationStamp = m_file.getModificationStamp();
        }
      }
      notifyResourceListeners(notificationElement, notificationEvent);
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus(IStatus.ERROR, "Failed to persist XML file '" + m_file.getName() + "'.", e));
    }
  }

  public void notifyStubRebuilt(String alias) {
    notifyResourceListeners(alias, IResourceListener.EVENT_STUB_REBUILT);
  }
}