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
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

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
    return "update formdatas...";
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
    for (IType t : getTypes()) {
      new FormDataUpdateOperation(t).run(monitor, workingCopyManager);
    }
  }

  /**
   * @return the types
   */
  public IType[] getTypes() {
    return m_types;
  }

}
