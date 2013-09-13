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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link MultipleFormDataUpdateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 03.03.2011
 */
public class MultipleFormDataUpdateOperation implements IOperation {

  private final IType[] m_types;

  public MultipleFormDataUpdateOperation(IType[] types) {
    m_types = types;
  }

  @Override
  public String getOperationName() {
    return "Update Form Datas...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getTypes() == null) {
      throw new IllegalArgumentException("types can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    validate();
    IType[] types = getTypes();
    monitor.beginTask("Updating Form Datas", types.length);
    int i = 0;
    for (IType t : types) {
      i++;
      monitor.setTaskName("Updating Form Data " + i + " of " + types.length + " (" + t.getElementName() + ")");
      FormDataAnnotation annotation = ScoutTypeUtility.findFormDataAnnotation(t, TypeUtility.getSuperTypeHierarchy(t));
      if (annotation != null && FormDataAnnotation.isCreate(annotation)) {
        IType formDataType = TypeUtility.getTypeBySignature(annotation.getFormDataTypeSignature());
        if (TypeUtility.exists(formDataType)) {
          new FormDataUpdateOperation(t, formDataType.getCompilationUnit()).run(monitor, workingCopyManager);
        }
      }
      monitor.worked(1);
    }
  }

  /**
   * @return the types
   */
  public IType[] getTypes() {
    return m_types;
  }

}
