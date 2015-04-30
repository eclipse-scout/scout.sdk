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
package org.eclipse.scout.sdk.rap.operations.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.PlatformVersionUtility;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.util.Batik17ProductFileUpgradeOperation;
import org.eclipse.scout.sdk.operation.util.MarsProductFileUpgradeOperation;
import org.eclipse.scout.sdk.operation.util.OsgiSystemCapabilitiesAddOperation;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link RapProductFileUpgradeOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 13.11.2013
 */
public class RapProductFileUpgradeOperation extends AbstractScoutProjectNewOperation {

  protected List<IFile> m_prodFiles;

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    ArrayList<IFile> productFiles = new ArrayList<IFile>(2);
    IFile f = getProperties().getProperty(CreateUiRapPluginOperation.PROP_PRODUCT_FILE_DEV, IFile.class);
    if (f != null) {
      productFiles.add(f);
    }

    f = getProperties().getProperty(CreateUiRapPluginOperation.PROP_PRODUCT_FILE_PROD, IFile.class);
    if (f != null) {
      productFiles.add(f);
    }

    m_prodFiles = productFiles;
  }

  @Override
  public String getOperationName() {
    return "Upgrade the RAP Products";
  }

  @Override
  public void validate() {
    super.validate();
    if (m_prodFiles == null || m_prodFiles.size() != 2) {
      throw new IllegalArgumentException("product file not found.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (JdtUtility.isBatik17OrNewer()) {
      Batik17ProductFileUpgradeOperation op = new Batik17ProductFileUpgradeOperation();
      for (IFile f : m_prodFiles) {
        op.addProductFile(f);
      }
      op.validate();
      op.run(monitor, workingCopyManager);
    }

    if (PlatformVersionUtility.isMarsOrLater(getTargetPlatformVersion())) {
      MarsProductFileUpgradeOperation marsUpdateOp = new MarsProductFileUpgradeOperation(m_prodFiles);
      marsUpdateOp.validate();
      marsUpdateOp.run(monitor, workingCopyManager);
    }

    boolean isMin18 = isMinJavaVersion(1.8);
    if (isMin18 && !PlatformVersionUtility.isLunaOrLater(getTargetPlatformVersion())) {
      String javaVersionStr = (String) getProperties().getProperty(PROP_JAVA_VERSION);
      OsgiSystemCapabilitiesAddOperation osgiCapAddOperation = new OsgiSystemCapabilitiesAddOperation(m_prodFiles, javaVersionStr);
      osgiCapAddOperation.validate();
      osgiCapAddOperation.run(monitor, workingCopyManager);
    }
  }
}
