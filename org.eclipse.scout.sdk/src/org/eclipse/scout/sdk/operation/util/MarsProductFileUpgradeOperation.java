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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper.DependencyType;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class MarsProductFileUpgradeOperation implements IOperation {

  private final List<IFile> m_prodFiles;

  public MarsProductFileUpgradeOperation(List<IFile> prodFiles) {
    m_prodFiles = prodFiles;
  }

  @Override
  public String getOperationName() {
    return "Upgrade Product Files to Mars Level";
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (CollectionUtility.isEmpty(m_prodFiles)) {
      return;
    }

    for (IFile prodFile : m_prodFiles) {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(prodFile);
      pfmh.ProductFile.addDependency("org.eclipse.osgi.compatibility.state", DependencyType.FRAGMENT);
      pfmh.save();
    }
  }
}
