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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;

/**
 *
 */
public class WellformLookupCallsOperation implements IOperation {

  final IType lookupCall = ScoutSdk.getType(RuntimeClasses.LookupCall);

  private final IScoutBundle m_bundle;
  private IType[] m_lookupCalls;

  public WellformLookupCallsOperation(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  public String getOperationName() {
    return "wellform all lookup calls...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    int bundleType = getBundle().getType();
    if (bundleType != IScoutBundle.BUNDLE_CLIENT && bundleType != IScoutBundle.BUNDLE_SHARED) {
      throw new IllegalArgumentException("bundle must be a client/shared bundle.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // find types
    ICachedTypeHierarchy lookupCallHierarchy = ScoutSdk.getPrimaryTypeHierarchy(lookupCall);
    m_lookupCalls = lookupCallHierarchy.getAllSubtypes(lookupCall, TypeFilters.getClassesInProject(getBundle().getJavaProject()));
    // format types
    if (monitor.isCanceled()) {
      return;
    }
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(m_lookupCalls, true);
    op.validate();
    op.run(monitor, workingCopyManager);
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  public IType[] getLookupCalls() {
    return m_lookupCalls;
  }
}
