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
public class WellformCodeTypesOperation implements IOperation {

  private final IType iCodeType = TypeUtility.getType(RuntimeClasses.ICodeType);

  private final IScoutBundle m_sharedBundle;
  private IType[] m_codeTypes;

  public WellformCodeTypesOperation(IScoutBundle sharedBundle) {
    m_sharedBundle = sharedBundle;
  }

  @Override
  public String getOperationName() {
    return "wellform all code types...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!getSharedBundle().getType().equals(IScoutBundle.TYPE_SHARED)) {
      throw new IllegalArgumentException("bundle must be a shared bundle.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // find all forms
    ICachedTypeHierarchy codeTypeHierarchy = TypeUtility.getPrimaryTypeHierarchy(iCodeType);
    m_codeTypes = codeTypeHierarchy.getAllSubtypes(iCodeType, TypeFilters.getClassesInProject(getSharedBundle().getJavaProject()));
    // format forms
    if (monitor.isCanceled()) {
      return;
    }
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(m_codeTypes, true);
    op.validate();
    op.run(monitor, workingCopyManager);
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
  }

  public IType[] getCodeTypes() {
    return m_codeTypes;
  }
}
