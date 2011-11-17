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
package org.eclipse.scout.sdk.ws.jaxws.ext;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.extensions.IPageFactory;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.operation.BuildJaxWsFileCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.JaxWsServletRegistrationOperation;
import org.eclipse.scout.sdk.ws.jaxws.operation.SunJaxWsFileCreateOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServicesTablePage;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class WebServicePageFactory implements IPageFactory {

  public WebServicePageFactory() {
  }

  @Override
  public void createChildren(IPage parentPage) {
    IScoutBundle bundle = (IScoutBundle) parentPage.getScoutResource();
    if (bundle.getType() != IScoutBundle.BUNDLE_SERVER) {
      return;
    }

    if (!isJaxWsDependencyInstalled(bundle)) {
      return;
    }

    // ensure folders to be created and registered in build.properties
    JaxWsSdkUtility.getFolder(bundle, JaxWsConstants.PATH_WEB_INF, true);

    // create sunJaxWs.xml file if not exist
    IFile sunJaxWsXmlFile = JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_SUN_JAXWS, false);
    if (sunJaxWsXmlFile == null || !sunJaxWsXmlFile.exists()) {
      SunJaxWsFileCreateOperation op = new SunJaxWsFileCreateOperation(bundle);
      new OperationJob(op).schedule();
    }

    // create buildJaxWs.xml file if not exist
    IFile buildJaxWsXmlFile = JaxWsSdkUtility.getFile(bundle, JaxWsConstants.PATH_BUILD_JAXWS, false);
    if (buildJaxWsXmlFile == null || !buildJaxWsXmlFile.exists()) {
      BuildJaxWsFileCreateOperation op = new BuildJaxWsFileCreateOperation(bundle);
      new OperationJob(op).schedule();
    }

    ensureJaxWsServletRegistration(bundle);

    // contribute WebServicesTablePage
    new WebServicesTablePage(parentPage);
  }

  private void ensureJaxWsServletRegistration(IScoutBundle bundle) {
    try {
      // register JAX-WS servlet
      IScoutBundle servletContributingBundle = JaxWsSdkUtility.getServletContributingBundle(bundle);
      if (servletContributingBundle == null) {
        JaxWsSdk.logError("Failed to find servlet contributing bundle");
        return;
      }
      // plugin.xml of bundle
      PluginModelHelper h = new PluginModelHelper(servletContributingBundle.getProject());
      HashMap<String, String> attributes = new HashMap<String, String>();
      attributes.put("class", JaxWsRuntimeClasses.JaxWsServlet.getFullyQualifiedName());
      if (!h.PluginXml.existsSimpleExtension(JaxWsConstants.SERVER_EXTENSION_POINT_SERVLETS, "servlet", attributes)) {
        JaxWsServletRegistrationOperation op = new JaxWsServletRegistrationOperation();
        op.setBundle(bundle);
        op.setJaxWsAlias(JaxWsConstants.JAX_WS_ALIAS);
        try {
          op.run(new NullProgressMonitor(), null);
        }
        catch (Exception e) {
          JaxWsSdk.logError("failed to register JAX-WS servlet", e);
        }
      }
    }
    catch (Exception e) {
      JaxWsSdk.logError("Failed to ensure JAX-WS servlet registration", e);
    }
  }

  private boolean isJaxWsDependencyInstalled(IScoutBundle bundle) {
    return TypeUtility.exists(JaxWsRuntimeClasses.JaxWsActivator) && TypeUtility.isOnClasspath(JaxWsRuntimeClasses.JaxWsActivator, bundle.getJavaProject());
  }
}
