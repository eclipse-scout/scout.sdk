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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link RapProductMobileAddOperation}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 07.03.2013
 */
public class RapProductMobileAddOperation extends AbstractScoutProjectNewOperation {

  private final ArrayList<IFile> m_productFiles = new ArrayList<IFile>(2);

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
    IFile dev = getProperties().getProperty(CreateUiRapPluginOperation.PROP_PRODUCT_FILE_DEV, IFile.class);
    if (dev != null) {
      m_productFiles.add(dev);
    }
    IFile prod = getProperties().getProperty(CreateUiRapPluginOperation.PROP_PRODUCT_FILE_PROD, IFile.class);
    if (prod != null) {
      m_productFiles.add(prod);
    }
  }

  @Override
  public String getOperationName() {
    return "Add Mobile Client to RAP Products";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String mobileClientName = getProperties().getProperty(CreateMobileClientPluginOperation.PROP_MOBILE_BUNDLE_CLIENT_NAME, String.class);
    for (IFile f : m_productFiles) {
      ProductFileModelHelper pfmh = new ProductFileModelHelper(f);
      pfmh.ProductFile.addDependency(mobileClientName);
      pfmh.save();
    }
  }

}
