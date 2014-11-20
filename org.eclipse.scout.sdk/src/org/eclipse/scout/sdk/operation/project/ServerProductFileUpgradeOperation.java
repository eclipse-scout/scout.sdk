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
package org.eclipse.scout.sdk.operation.project;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.compatibility.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.util.JettyProductFileUpgradeOperation;
import org.eclipse.scout.sdk.operation.util.OsgiSystemCapabilitiesAddOperation;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class ServerProductFileUpgradeOperation extends AbstractScoutProjectNewOperation {

  private IFile m_serverDevProdFile;
  private IFile m_serverProdFile;

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateServerPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    m_serverDevProdFile = getProperties().getProperty(CreateServerPluginOperation.PROP_PRODUCT_FILE_DEV, IFile.class);
    m_serverProdFile = getProperties().getProperty(CreateServerPluginOperation.PROP_PRODUCT_FILE_PROD, IFile.class);
  }

  @Override
  public void validate() {
    super.validate();
    if (m_serverDevProdFile == null || m_serverProdFile == null) {
      throw new IllegalArgumentException("server products cannot be null.");
    }
  }

  @Override
  public String getOperationName() {
    return "Upgrade Server Products";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // Jetty update for DEV product
    if (PlatformVersionUtility.isJunoOrLater(getTargetPlatformVersion())) {
      JettyProductFileUpgradeOperation op = new JettyProductFileUpgradeOperation(new IFile[]{m_serverDevProdFile}); // only DEV product
      op.validate();
      op.run(monitor, workingCopyManager);
    }

    // Servlet Plugin
    String servletPlugin = null;
    if (JdtUtility.isServlet31OrNewer()) {
      servletPlugin = "org.eclipse.scout.rt.server.servlet31";
    }
    else {
      servletPlugin = "org.eclipse.scout.rt.server.servlet25";
    }

    // Java 1.8/1.6 fragment for JAX-WS
    boolean isMin18 = isMinJavaVersion(1.8);
    String jaxWsFragment = null;
    if (isMin18) {
      jaxWsFragment = "org.eclipse.scout.jaxws216.jre18.fragment";
    }
    else {
      jaxWsFragment = "org.eclipse.scout.jaxws216.jre16.fragment";
    }

    // add jaxws fragments
    ArrayList<IFile> productFiles = CollectionUtility.arrayList(m_serverDevProdFile, m_serverProdFile);
    for (IFile f : productFiles) {
      ProductFileModelHelper h = new ProductFileModelHelper(f);
      h.ProductFile.addDependency(servletPlugin);
      h.ProductFile.addDependency(jaxWsFragment);

      h.save();
    }

    if (isMin18 && !PlatformVersionUtility.isLunaOrLater(getTargetPlatformVersion())) {
      String javaVersionStr = (String) getProperties().getProperty(PROP_JAVA_VERSION);
      OsgiSystemCapabilitiesAddOperation osgiCapAddOperation = new OsgiSystemCapabilitiesAddOperation(productFiles, javaVersionStr);
      osgiCapAddOperation.validate();
      osgiCapAddOperation.run(monitor, workingCopyManager);
    }
  }
}
