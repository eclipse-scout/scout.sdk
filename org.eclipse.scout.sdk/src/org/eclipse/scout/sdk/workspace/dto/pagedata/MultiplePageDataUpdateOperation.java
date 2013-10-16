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
package org.eclipse.scout.sdk.workspace.dto.pagedata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link MultiplePageDataUpdateOperation}</h3>
 * 
 * @author mvi
 * @since 3.10.0 16.10.2013
 */
public class MultiplePageDataUpdateOperation implements IOperation {
  private final ITypeResolver m_resolver;

  public MultiplePageDataUpdateOperation(ITypeResolver resolver) {
    m_resolver = resolver;
  }

  @Override
  public String getOperationName() {
    return "Update Page Datas...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (m_resolver == null) {
      throw new IllegalArgumentException("types can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IType[] pages = m_resolver.getTypes();
    monitor.beginTask("Updating Page Datas", pages.length);
    int i = 0;
    for (IType t : pages) {
      i++;
      monitor.setTaskName("Updating Page Data " + i + " of " + pages.length + " (" + t.getElementName() + ")");
      PageDataAnnotation annotation = ScoutTypeUtility.findPageDataAnnotation(t, TypeUtility.getSuperTypeHierarchy(t));
      if (annotation != null) {
        IType pageDataType = TypeUtility.getTypeBySignature(annotation.getPageDataTypeSignature());
        if (TypeUtility.exists(pageDataType)) {
          PageDataDtoUpdateOperation op = new PageDataDtoUpdateOperation(t, annotation);
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
