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
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 *
 */
public class WellformLookupCallsOperation implements IOperation {

  private final IType lookupCall = TypeUtility.getType(RuntimeClasses.LookupCall);

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
    String bundleType = getBundle().getType();
    if (!bundleType.equals(IScoutBundle.TYPE_CLIENT) && !bundleType.equals(IScoutBundle.TYPE_SHARED)) {
      throw new IllegalArgumentException("bundle must be a client or shared bundle.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // find types
    ICachedTypeHierarchy lookupCallHierarchy = TypeUtility.getPrimaryTypeHierarchy(lookupCall);
    m_lookupCalls = lookupCallHierarchy.getAllSubtypes(lookupCall, ScoutTypeFilters.getTypesInScoutBundles(getBundle()));
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
