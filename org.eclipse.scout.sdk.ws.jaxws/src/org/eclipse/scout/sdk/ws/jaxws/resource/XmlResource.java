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
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.w3c.dom.Document;

public class XmlResource extends ManagedResource {

  private Document m_xmlDocument;

  public XmlResource(IScoutBundle bundle) {
    super(bundle.getProject());
  }

  public Document loadXml() {
    synchronized (m_fileLock) {
      if (!existsFile()) {
        m_xmlDocument = null;
        return null;
      }

      if (m_xmlDocument == null || m_file.getModificationStamp() != m_modificationStamp) {
        m_xmlDocument = null;
        m_modificationStamp = m_file.getModificationStamp();

        InputStream is = null;
        try {
          is = m_file.getContents();
          m_xmlDocument = JaxWsSdkUtility.createNewXmlDocument(is);
        }
        catch (Exception e) {
          JaxWsSdk.logWarning("Failed to parse XML file '" + m_file.getName() + "'.", e);
        }
        finally {
          if (is != null) {
            try {
              is.close();
            }
            catch (IOException e) {
            }
          }
        }
      }
    }
    return m_xmlDocument;
  }

  public void storeXmlAsync(final Document xmlDocument, final int notificationEvent, final String... notificationElements) {
    IOperation op = new IOperation() {

      @Override
      public void validate() throws IllegalArgumentException {
      }

      @Override
      public String getOperationName() {
        return "Store resource to disk";
      }

      @Override
      public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
        storeXml(xmlDocument, notificationEvent, monitor, notificationElements);
      }
    };
    new OperationJob(op).schedule();
  }

  public void storeXml(Document xmlDocument, int notificationEvent, IProgressMonitor monitor, String... notificationElements) throws CoreException {
    try {
      synchronized (m_fileLock) {
        if (m_file == null) {
          throw new CoreException(new ScoutStatus("File must not be null"));
        }

        String xmlContent = JaxWsSdkUtility.getXmlContent(xmlDocument);
        m_modificationStamp = ManagedResource.API_MODIFICATION_STAMP;
        try {
          JaxWsSdkUtility.ensureFileAccessibleAndRegistered(m_file, true);
          m_file.setContents(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")), true, true, monitor);
          m_xmlDocument = xmlDocument;
        }
        finally {
          m_modificationStamp = m_file.getModificationStamp();
        }
      }
      for (String notificationElement : notificationElements) {
        notifyResourceListeners(notificationElement, notificationEvent);
      }
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus(IStatus.ERROR, "Failed to persist XML file.", e));
    }
  }
}
