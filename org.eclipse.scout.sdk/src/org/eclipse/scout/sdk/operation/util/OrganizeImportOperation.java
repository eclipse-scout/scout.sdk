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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Organizes the imports of the given compilation unit if a corresponding service is registered.
 */
public class OrganizeImportOperation implements IOperation {

  private final ICompilationUnit m_icu;

  public OrganizeImportOperation(ICompilationUnit icu) {
    m_icu = icu;

  }

  @Override
  public String getOperationName() {
    return "Organize imports...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getCompilationUnit() == null) {
      throw new IllegalArgumentException("no compilation unit set.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    BundleContext context = ScoutSdk.getDefault().getBundle().getBundleContext();
    ServiceReference<IOrganizeImportService> reference = context.getServiceReference(IOrganizeImportService.class);
    try {
      if (reference != null) {
        IOrganizeImportService service = context.getService(reference);
        if (service != null) {
          service.organize(getCompilationUnit(), monitor);
        }
        else {
          ScoutSdk.logWarning("No valid Organize Imports Service has been registered.");
        }
      }
      else {
        ScoutSdk.logWarning("No Organize Imports Service has been registered.");
      }
    }
    finally {
      context.ungetService(reference);
    }
  }

  public ICompilationUnit getCompilationUnit() {
    return m_icu;
  }
}
