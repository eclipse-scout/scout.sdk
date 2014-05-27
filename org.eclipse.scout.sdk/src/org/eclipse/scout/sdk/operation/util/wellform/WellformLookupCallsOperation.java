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

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
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

  private final IScoutBundle m_bundle;
  private Set<IType> m_lookupCalls;

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
    IType iLookupCall = TypeUtility.getType(IRuntimeClasses.ILookupCall);
    ICachedTypeHierarchy lookupCallHierarchy = TypeUtility.getPrimaryTypeHierarchy(iLookupCall);
    m_lookupCalls = lookupCallHierarchy.getAllSubtypes(iLookupCall, ScoutTypeFilters.getClassesInScoutBundles(getBundle()));
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

  public Set<IType> getLookupCalls() {
    return m_lookupCalls;
  }
}
