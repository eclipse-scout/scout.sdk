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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.SunJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ElementBean;

public class WsProviderCodeFirstDeleteOperation implements IOperation {

  public static final int ID_REGISTRATION = 1 << 0;
  public static final int ID_IMPL_TYPE = 1 << 1;

  private IScoutBundle m_bundle;
  private SunJaxWsBean m_sunJaxWsBean;
  private List<ElementBean> m_elements;

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_bundle == null) {
      throw new IllegalArgumentException("No bundle set");
    }
    if (m_sunJaxWsBean == null) {
      throw new IllegalArgumentException("No sunJaxWsBean set");
    }
    if (m_elements == null) {
      throw new IllegalArgumentException("No elements set");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    for (ElementBean element : m_elements) {
      switch (element.getId()) {
        case ID_REGISTRATION:
          deleteSunJaxWsXmlEntry(monitor);
          break;
        case ID_IMPL_TYPE:
          deleteType((IType) element.getJavaElement(), monitor);
          break;
      }
    }
  }

  private void deleteSunJaxWsXmlEntry(IProgressMonitor monitor) throws CoreException {
    String alias = m_sunJaxWsBean.getAlias();
    ScoutXmlDocument xmlDocument = m_sunJaxWsBean.getXml().getDocument();
    xmlDocument.getRoot().removeChild(m_sunJaxWsBean.getXml());
    ResourceFactory.getSunJaxWsResource(m_bundle).storeXml(xmlDocument, alias, IResourceListener.EVENT_SUNJAXWS_ENTRY_REMOVED, monitor);
  }

  private void deleteType(IType type, IProgressMonitor monitor) throws JavaModelException {
    try {
      type.getCompilationUnit().delete(true, monitor);
    }
    catch (Exception e) {
      JaxWsSdk.logError("could not delete type", e);
    }
  }

  @Override
  public String getOperationName() {
    return WsProviderCodeFirstDeleteOperation.class.getName();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public SunJaxWsBean getSunJaxWsBean() {
    return m_sunJaxWsBean;
  }

  public void setSunJaxWsBean(SunJaxWsBean sunJaxWsBean) {
    m_sunJaxWsBean = sunJaxWsBean;
  }

  public List<ElementBean> getElements() {
    return m_elements;
  }

  public void setElements(List<ElementBean> elements) {
    m_elements = elements;
  }
}
