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
package org.eclipse.scout.sdk.rap.operations.project;

import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.rap.ScoutSdkRap;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public abstract class AbstractUpdateProductOperation implements IOperation {

  private final IFile m_productFile;
  private HashSet<String> m_fragmentIds = new HashSet<String>();

  public AbstractUpdateProductOperation(IFile productFile) {
    m_productFile = productFile;
  }

  @Override
  public String getOperationName() {
    String productFilePath = null;
    if (getProductFile() != null) {
      productFilePath = getProductFile().getLocation().toPortableString();
    }
    return "Update product '" + productFilePath + "'";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getProductFile() == null || !getProductFile().exists()) {
      throw new IllegalArgumentException("product file does not exist");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    try {
      ProductFileModelHelper h = new ProductFileModelHelper(getProductFile());
      updateProductModel(h);
      h.save();
    }
    catch (CoreException e) {
      ScoutSdkRap.logError("could update product model for '" + getProductFile().getLocation() + "'.");
    }
  }

  protected void updateProductModel(ProductFileModelHelper productModel) throws CoreException {
  }

  public IFile getProductFile() {
    return m_productFile;
  }
}
