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
package org.eclipse.scout.sdk.ui.fields.proposal.javaelement;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link JavaElementAbstractTypeContentProvider}</h3>
 *
 * @author Matthias Villiger
 * @since 3.8.0 20.04.2012
 */
public class JavaElementAbstractTypeContentProvider extends AbstractJavaElementContentProvider {

  private final IType m_superType;
  private final IJavaProject m_project;
  private final IType[] m_mostlyUsed;
  private ITypeFilter m_filter;

  public JavaElementAbstractTypeContentProvider(IType superType, IJavaProject project, IType... mostlyUsed) {
    this(superType, project, null, mostlyUsed);
  }

  public JavaElementAbstractTypeContentProvider(IType superType, IJavaProject project, ITypeFilter filter, IType... mostlyUsed) {
    m_superType = superType;
    m_project = project;
    m_mostlyUsed = mostlyUsed;
    m_filter = filter;
  }

  @Override
  protected Object[][] computeProposals() {
    ITypeFilter filter = null;
    IType[] mostlyUsed = null;
    if (getFilter() == null) {
      filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getNotInTypes(m_mostlyUsed), TypeFilters.getNoSurroundingContextTypeFilter());
      mostlyUsed = m_mostlyUsed;
    }
    else {
      filter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getNotInTypes(m_mostlyUsed), TypeFilters.getNoSurroundingContextTypeFilter(), getFilter());

      // filter the mostly used
      if (m_mostlyUsed != null) {
        ArrayList<IType> mu = new ArrayList<IType>(m_mostlyUsed.length);
        for (IType o : m_mostlyUsed) {
          if (TypeUtility.exists(o) && getFilter().accept(o)) {
            mu.add(o);
          }
        }
        mostlyUsed = mu.toArray(new IType[mu.size()]);
      }
    }

    Set<IType> types = TypeUtility.getAbstractTypesOnClasspath(m_superType, m_project, filter);

    return new Object[][]{mostlyUsed, types.toArray(new IType[types.size()])};
  }

  public ITypeFilter getFilter() {
    return m_filter;
  }

  public void setFilter(ITypeFilter filter) {
    m_filter = filter;
    invalidateCache();
  }
}
