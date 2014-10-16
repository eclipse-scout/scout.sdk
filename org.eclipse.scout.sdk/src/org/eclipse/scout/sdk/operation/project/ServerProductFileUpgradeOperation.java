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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.compatibility.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.util.JettyProductFileUpgradeOperation;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Constants;

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

  @SuppressWarnings("deprecation")
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
    double javaVersion = JdtUtility.getExecEnvVersion(getExecutionEnvironment());
    boolean is18OrNewer = Math.abs(1.8 - javaVersion) < 0.0000001;
    String jaxWsFragment = null;
    if (is18OrNewer) {
      jaxWsFragment = "org.eclipse.scout.jaxws216.jre18.fragment";
    }
    else {
      jaxWsFragment = "org.eclipse.scout.jaxws216.jre16.fragment";
    }

    // apply the product file modifications
    for (IFile f : CollectionUtility.arrayList(m_serverDevProdFile, m_serverProdFile)) {
      ProductFileModelHelper h = new ProductFileModelHelper(f);
      h.ProductFile.addDependency(servletPlugin);
      h.ProductFile.addDependency(jaxWsFragment);

      if (is18OrNewer && !PlatformVersionUtility.isLunaOrLater(getTargetPlatformVersion())) {
        // Java 1.8 is only support for Luna platforms or newer.
        // For all other platforms we must add the 1.8 ExecEnv to the config.ini
        h.ConfigurationFile.setEntry(Constants.FRAMEWORK_EXECUTIONENVIRONMENT, "OSGi/Minimum-1.0,OSGi/Minimum-1.1,OSGi/Minimum-1.2,JRE-1.1,J2SE-1.2,J2SE-1.3,J2SE-1.4,J2SE-1.5,JavaSE-1.6,JavaSE-1.7,JavaSE-1.8");
        h.ConfigurationFile.setEntry(Constants.FRAMEWORK_SYSTEMCAPABILITIES_EXTRA, "osgi.ee;osgi.ee=\"JavaSE\";version:List<Version>=\"" + getProperties().getProperty(PROP_JAVA_VERSION) + "\"");
      }

      h.save();
    }
  }
}
