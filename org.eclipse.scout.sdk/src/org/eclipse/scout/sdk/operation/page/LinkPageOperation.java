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
package org.eclipse.scout.sdk.operation.page;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

public class LinkPageOperation extends AbstractPageOperation {

  private IType m_page;

  @Override
  public String getOperationName() {
    return null;
  }

  @Override
  public void validate() {
    if (getPage() == null) {
      throw new IllegalArgumentException("page can not be null.");
    }
    if (getHolderType() == null) {
      throw new IllegalArgumentException("the holder of the page can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    addToHolder(getPage(), monitor, workingCopyManager);
  }

  public IType getPage() {
    return m_page;
  }

  public void setPage(IType page) {
    m_page = page;
  }

}
