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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link Batik17ProductFileUpgradeOperation}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 13.11.2013
 */
public class Batik17ProductFileUpgradeOperation implements IOperation {

  private final ArrayList<IFile> m_prodFiles;

  public Batik17ProductFileUpgradeOperation() {
    m_prodFiles = new ArrayList<IFile>(2);
  }

  public void addProductFile(IFile f) {
    m_prodFiles.add(f);
  }

  @Override
  public String getOperationName() {
    return "Add Batik 1.7 Plugins to Product Files";
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    final String[] additionalBatik17Plugins = new String[]{"org.w3c.dom.events", "org.eclipse.scout.org.w3c.dom.svg.fragment"};
    for (IFile f : m_prodFiles) {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(f);

      // additional product file dependencies
      for (String plugin : additionalBatik17Plugins) {
        pfmh.ProductFile.addDependency(plugin);
      }

      pfmh.save();
    }
  }
}
