/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.util;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.Constants;

/**
 * <h3>{@link OsgiSystemCapabilitiesAddOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 20.11.2014
 */
public class OsgiSystemCapabilitiesAddOperation implements IOperation {

  private final List<IFile> m_productFiles;
  private final String m_javaVersion;

  public OsgiSystemCapabilitiesAddOperation(List<IFile> productFiles, String javaVersion) {
    m_productFiles = productFiles;
    m_javaVersion = javaVersion;
  }

  @Override
  public String getOperationName() {
    return "Add newer OSGi System Capabilities to config files.";
  }

  @Override
  public void validate() {
    if (m_productFiles == null) {
      throw new IllegalArgumentException("Product files must be passed.");
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IFile f : m_productFiles) {
      ProductFileModelHelper h = new ProductFileModelHelper(f);

      // Java 1.8 is only support for Luna platforms or newer.
      // For all other platforms we must add the 1.8 ExecEnv to the config.ini
      h.ConfigurationFile.setEntry(Constants.FRAMEWORK_EXECUTIONENVIRONMENT, "OSGi/Minimum-1.0,OSGi/Minimum-1.1,OSGi/Minimum-1.2,JRE-1.1,J2SE-1.2,J2SE-1.3,J2SE-1.4,J2SE-1.5,JavaSE-1.6,JavaSE-1.7,JavaSE-1.8");
      h.ConfigurationFile.setEntry(Constants.FRAMEWORK_SYSTEMCAPABILITIES_EXTRA, "osgi.ee;osgi.ee=\"JavaSE\";version:List<Version>=\"" + m_javaVersion + "\"");

      h.save();
    }
  }
}
