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

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class WellformFormsOperation implements IOperation {

  private final IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
  private final IType iSearchForm = TypeUtility.getType(RuntimeClasses.ISearchForm);

  private final IScoutBundle m_clientBundle;
  private IType[] m_forms;

  public WellformFormsOperation(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
  }

  @Override
  public String getOperationName() {
    return "wellform all forms...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!getClientBundle().getType().equals(IScoutBundle.TYPE_CLIENT)) {
      throw new IllegalArgumentException("bundle must be a client bundle.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // find all forms
    ICachedTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
    IType[] searchForms = formHierarchy.getAllSubtypes(iSearchForm, TypeFilters.getTypesInProject(getClientBundle().getJavaProject()));
    ITypeFilter formFilter = TypeFilters.getMultiTypeFilter(
        TypeFilters.getTypesInProject(getClientBundle().getJavaProject()),
        TypeFilters.getNotInTypes(new HashSet<IType>(Arrays.asList(searchForms)))
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

  public IType[] getForms() {
    return m_forms;
  }
}
