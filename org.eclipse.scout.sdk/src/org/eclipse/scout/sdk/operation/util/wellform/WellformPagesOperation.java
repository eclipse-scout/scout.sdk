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
package org.eclipse.scout.sdk.operation.util.wellform;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class WellformPagesOperation implements IOperation {

  private final IType iPage = TypeUtility.getType(RuntimeClasses.IPage);

  private final IScoutBundle m_clientBundle;
  private IType[] m_pages;

  public WellformPagesOperation(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
  }

  @Override
  public String getOperationName() {
    return "wellform all pages...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!getClientBundle().getType().equals(IScoutBundle.TYPE_CLIENT)) {
      throw new IllegalArgumentException("bundle must be a client bundle.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // find types
    ICachedTypeHierarchy hierarchy = TypeUtility.getPrimaryTypeHierarchy(iPage);
    m_pages = hierarchy.getAllSubtypes(iPage, TypeFilters.getTypesInProject(getClientBundle().getJavaProject()));
    // format types
    if (monitor.isCanceled()) {
      return;
    }
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(m_pages, true);
    op.validate();
    op.run(monitor, workingCopyManager);
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public IType[] getPages() {
    return m_pages;
  }
}
