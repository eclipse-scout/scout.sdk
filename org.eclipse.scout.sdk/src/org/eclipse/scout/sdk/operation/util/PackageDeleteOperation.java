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
package org.eclipse.scout.sdk.operation.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ManifestExportPackageOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
public class PackageDeleteOperation implements IOperation {
  private final IPackageFragment m_packageFragment;

  public PackageDeleteOperation(IPackageFragment pack) {
    m_packageFragment = pack;

  }

  @Override
  public String getOperationName() {
    return "Delete package '" + getPackageFragment().getElementName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getPackageFragment())) {
      throw new IllegalArgumentException("package does not exist");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ManifestExportPackageOperation manifestOp = new ManifestExportPackageOperation(ManifestExportPackageOperation.TYPE_REMOVE, new IPackageFragment[]{getPackageFragment()}, true);
    manifestOp.run(monitor, workingCopyManager);
    getPackageFragment().delete(true, monitor);
  }

  public IPackageFragment getPackageFragment() {
    return m_packageFragment;
  }
}
