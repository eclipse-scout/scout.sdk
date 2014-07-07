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
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 *
 */
public class WellformFormsOperation implements IOperation {

  private final IScoutBundle m_clientBundle;
  private Set<IType> m_forms;

  public WellformFormsOperation(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
  }

  @Override
  public String getOperationName() {
    return "wellform all forms...";
  }

  @Override
  public void validate() {
    if (!getClientBundle().getType().equals(IScoutBundle.TYPE_CLIENT)) {
      throw new IllegalArgumentException("bundle must be a client bundle.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IType iForm = TypeUtility.getType(IRuntimeClasses.IForm);
    IType iSearchForm = TypeUtility.getType(IRuntimeClasses.ISearchForm);

    // find all forms
    ICachedTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
    Set<IType> searchForms = formHierarchy.getAllSubtypes(iSearchForm, ScoutTypeFilters.getClassesInScoutBundles(getClientBundle()));
    ITypeFilter formFilter = TypeFilters.getMultiTypeFilterAnd(
        ScoutTypeFilters.getClassesInScoutBundles(getClientBundle()),
        TypeFilters.getNotInTypes(searchForms)
        );
    m_forms = formHierarchy.getAllSubtypes(iForm, formFilter, TypeComparators.getTypeNameComparator());
    // format forms
    if (monitor.isCanceled()) {
      return;
    }
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(m_forms, true);
    op.validate();
    op.run(monitor, workingCopyManager);
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public Set<IType> getForms() {
    return m_forms;
  }
}
