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
package org.eclipse.scout.sdk.workspace.dto.formdata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataDtoUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ITypeResolver;
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

  private final ITypeResolver m_resolver;

  public MultipleFormDataUpdateOperation(ITypeResolver resolver) {
    m_resolver = resolver;
  }

  @Override
  public String getOperationName() {
    return "Update Form Datas...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_resolver == null) {
      throw new IllegalArgumentException("type resolver can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    validate();
    IType[] types = m_resolver.getTypes();
    monitor.beginTask("Updating Form Datas", types.length);
    int i = 0;
    for (IType t : types) {
      i++;
      monitor.setTaskName("Updating Form Data " + i + " of " + types.length + " (" + t.getElementName() + ")");
      FormDataAnnotation annotation = ScoutTypeUtility.findFormDataAnnotation(t, TypeUtility.getSuperTypeHierarchy(t));
      if (FormDataAnnotation.isCreate(annotation)) {
        IType formDataType = annotation.getFormDataType();
        if (TypeUtility.exists(formDataType)) {
          FormDataDtoUpdateOperation op = new FormDataDtoUpdateOperation(t);
          op.validate();
          op.run(monitor, workingCopyManager);
        }
      }
      if (monitor.isCanceled()) {
        return;
      }
      monitor.worked(1);
    }
  }
}
