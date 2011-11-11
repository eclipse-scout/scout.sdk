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
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;

public class SunJaxWsFileCreateOperation implements IOperation {

  private IScoutBundle m_bundle;

  public SunJaxWsFileCreateOperation(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ScoutXmlDocument xmlDocument = new ScoutXmlDocument();
    xmlDocument.setXmlVersion("1.0");
    xmlDocument.setXmlEncoding("UTF-8");
    xmlDocument.setPrettyPrint(true);
    ScoutXmlElement rootXml = xmlDocument.setRoot("endpoints");
    rootXml.setNamespace("jws", SunJaxWsBean.NS_ENDPOINT);
    rootXml.setName("jws:endpoints");
    rootXml.setAttribute("version", "2.0");
    ResourceFactory.getSunJaxWsResource(m_bundle, true).storeXml(xmlDocument, IResourceListener.ELEMENT_FILE, IResourceListener.EVENT_SUNJAXWS_REPLACED, monitor);
  }

  @Override
  public String getOperationName() {
    return SunJaxWsFileCreateOperation.class.getName();
  }
}
