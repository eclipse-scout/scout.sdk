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
package org.eclipse.scout.sdk.operation.project;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.util.Batik17ProductFileUpgradeOperation;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link SwingProductFileUpgradeOperation}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 13.11.2013
 */
public class SwingProductFileUpgradeOperation extends AbstractScoutProjectNewOperation {

  protected IFile[] m_prodFiles;

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiSwingPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    ArrayList<IFile> productFiles = new ArrayList<IFile>(2);
    IFile f = getProperties().getProperty(CreateUiSwingPluginOperation.PROP_PRODUCT_FILE_DEV, IFile.class);
    if (f != null) productFiles.add(f);

    f = getProperties().getProperty(CreateUiSwingPluginOperation.PROP_PRODUCT_FILE_PROD, IFile.class);
    if (f != null) productFiles.add(f);

    m_prodFiles = productFiles.toArray(new IFile[productFiles.size()]);
  }

  @Override
  public String getOperationName() {
    return "Upgrade the Swing Products";
  }

  @Override
  public void validate() {
    super.validate();
    if (m_prodFiles == null || m_prodFiles.length != 2) {
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
  }
}
