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
package org.eclipse.scout.sdk.operation.form.field.table;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link TableColumnDeleteOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.9.0 02.05.2013
 */
public class TableColumnDeleteOperation extends JavaElementDeleteOperation {

  private final List<IType> m_columns;

  public TableColumnDeleteOperation(IType... columns) {
    this(Arrays.asList(columns));
  }

  public TableColumnDeleteOperation(List<IType> columns) {
    super(true);
    m_columns = columns;
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getColumns() != null) {
      for (IType col : getColumns()) {
        addMember(col);
        // find column getter
        IMethod getter = ScoutTypeUtility.getColumnGetterMethod(col);
        if (TypeUtility.exists(getter)) {
          addMember(getter);
        }
      }
    }
    validate();
    super.run(monitor, workingCopyManager);
  }

  public List<IType> getColumns() {
    return m_columns;
  }
}
