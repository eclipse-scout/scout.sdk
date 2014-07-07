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
package org.eclipse.scout.sdk.ws.jaxws.operation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.w3c.dom.Document;

public class BuildJaxWsFileCreateOperation implements IOperation {

  private final IScoutBundle m_bundle;

  public BuildJaxWsFileCreateOperation(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    Document xmlDocument = JaxWsSdkUtility.createNewXmlDocument("webservices");
    ResourceFactory.getBuildJaxWsResource(m_bundle, true).storeXml(xmlDocument, IResourceListener.EVENT_BUILDJAXWS_REPLACED, monitor, IResourceListener.ELEMENT_FILE);
  }

  @Override
  public String getOperationName() {
    return BuildJaxWsFileCreateOperation.class.getName();
  }
}
