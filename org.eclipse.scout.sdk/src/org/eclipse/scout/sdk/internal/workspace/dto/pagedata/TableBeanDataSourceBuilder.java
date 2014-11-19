/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.internal.workspace.dto.pagedata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.workspace.dto.AbstractTableBeanSourceBuilder;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.pagedata.DataAnnotation;

/**
 * <h3>{@link TableBeanDataSourceBuilder}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 28.08.2013
 */
public class TableBeanDataSourceBuilder extends AbstractTableBeanSourceBuilder {

  private DataAnnotation m_dataAnnotation;

  /**
   * @param modelType
   * @param elementName
   * @param setup
   */
  public TableBeanDataSourceBuilder(IType modelType, ITypeHierarchy modelLocalTypeHierarchy, String elementName, DataAnnotation dataAnnotation, ICompilationUnit derivedCu, IProgressMonitor monitor) {
    super(modelType, modelLocalTypeHierarchy, elementName, false, derivedCu, monitor);
    m_dataAnnotation = dataAnnotation;
    setup(monitor);
  }

  @Override
  protected String computeSuperTypeSignature() throws CoreException {
    return getDataAnnotation().getSuperDataTypeSignature();
  }

  public DataAnnotation getDataAnnotation() {
    return m_dataAnnotation;
  }
}
