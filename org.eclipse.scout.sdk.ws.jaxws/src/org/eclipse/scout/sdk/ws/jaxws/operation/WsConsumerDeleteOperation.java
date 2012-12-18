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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.sdk.IRuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.resource.IResourceListener;
import org.eclipse.scout.sdk.ws.jaxws.resource.ResourceFactory;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.ElementBean;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class WsConsumerDeleteOperation implements IOperation {

  public static final int ID_REGISTRATION = 1 << 0;
  public static final int ID_SERVICE_REGISTRATION = 1 << 1;
  public static final int ID_STUB = 1 << 2;
  public static final int ID_TYPE = 1 << 3;
  public static final int ID_BINDING_FILE = 1 << 4;
  public static final int ID_WSDL_FILE = 1 << 5;
  public static final int ID_REF_WSDL = 1 << 6;
  public static final int ID_REF_XSD = 1 << 7;

  private IScoutBundle m_bundle;
  private BuildJaxWsBean m_buildJaxWsBean;
  private List<ElementBean> m_elements;

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_bundle == null) {
      throw new IllegalArgumentException("No bundle set");
    }
    if (m_elements == null) {
      throw new IllegalArgumentException("No elements set");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    for (ElementBean element : m_elements) {
      switch (element.getId()) {
        case ID_REGISTRATION:
          deleteBuildJaxWsXmlEntry(monitor);
          break;
        case ID_SERVICE_REGISTRATION:
          unregisterService(m_bundle, (IType) element.getJavaElement(), monitor);
          break;
        case ID_TYPE:
          deleteType((IType) element.getJavaElement(), monitor);
          break;
        case ID_STUB:
          deleteStubFile((IFile) element.getResource(), monitor);
          break;
        case ID_WSDL_FILE:
        case ID_BINDING_FILE:
        case ID_REF_WSDL:
        case ID_REF_XSD:
          deleteFile((IFile) element.getResource(), monitor);
          break;
      }
    }
  }

  private void deleteBuildJaxWsXmlEntry(IProgressMonitor monitor) throws CoreException {
    if (m_buildJaxWsBean == null) {
      return;
    }
    String alias = m_buildJaxWsBean.getAlias();
    ScoutXmlDocument xmlDocument = m_buildJaxWsBean.getXml().getDocument();
    xmlDocument.getRoot().removeChild(m_buildJaxWsBean.getXml());
    ResourceFactory.getBuildJaxWsResource(m_bundle).storeXml(xmlDocument, IResourceListener.EVENT_BUILDJAXWS_ENTRY_ADDED, monitor, alias);
  }

  private void deleteType(IType type, IProgressMonitor monitor) throws JavaModelException {
    try {
      type.getCompilationUnit().delete(true, monitor);
    }
    catch (Exception e) {
      JaxWsSdk.logError("could not delete type", e);
    }
  }

  private void deleteFile(IFile file, IProgressMonitor monitor) throws CoreException {
    if (file != null && file.exists()) {
      file.delete(true, true, monitor);
    }
  }

  private void deleteStubFile(IFile jarFile, IProgressMonitor monitor) throws CoreException {
    if (jarFile != null && jarFile.exists()) {
      try {
        jarFile.delete(true, true, monitor);
        JaxWsSdkUtility.registerJarLib(m_bundle, jarFile, true, monitor);
      }
      catch (Exception e) {
        JaxWsSdk.logError(e);
      }
    }
  }

  private void unregisterService(IScoutBundle bundle, IType type, IProgressMonitor monitor) throws CoreException {
    // unregister server side
    IScoutBundle implementationBundle = ScoutSdkCore.getScoutWorkspace().getScoutBundle(bundle.getJavaProject().getProject());
    for (IScoutBundle serverBundle : implementationBundle.getRequiredBundles(ScoutBundleFilters.getServerFilter(), true)) {
      ScoutUtility.unregisterServiceClass(serverBundle.getProject(), IRuntimeClasses.EXTENSION_POINT_SERVICES, IRuntimeClasses.EXTENSION_ELEMENT_SERVICE, type.getFullyQualifiedName(), serverBundle.getRootPackageName() + ".ServerSession", monitor);
    }
  }

  @Override
  public String getOperationName() {
    return WsConsumerDeleteOperation.class.getName();
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public void setBundle(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  public BuildJaxWsBean getBuildJaxWsBean() {
    return m_buildJaxWsBean;
  }

  public void setBuildJaxWsBean(BuildJaxWsBean buildJaxWsBean) {
    m_buildJaxWsBean = buildJaxWsBean;
  }

  public List<ElementBean> getElements() {
    return m_elements;
  }

  public void setElements(List<ElementBean> elements) {
    m_elements = elements;
  }
}
