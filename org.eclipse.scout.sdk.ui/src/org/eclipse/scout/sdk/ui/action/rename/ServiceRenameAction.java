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
package org.eclipse.scout.sdk.ui.action.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.jdt.JdtRenameTransaction;
import org.eclipse.swt.widgets.Shell;

public class ServiceRenameAction extends AbstractRenameAction {

  private final IType m_serviceInterface;
  private final IType m_serviceImplementation;

  public ServiceRenameAction(Shell shell, String name, IType serviceImplementation, IType serviceInterface, String readOnlySuffix) {
    super(shell, name, serviceImplementation.getElementName(), readOnlySuffix);
    m_serviceImplementation = serviceImplementation;
    m_serviceInterface = serviceInterface;
  }

  @Override
  protected IStatus validate(String newName) {
    IStatus inheritedStatus = getJavaNameStatus(newName);
    if (inheritedStatus.matches(IStatus.ERROR)) {
      return inheritedStatus;
    }
    if (m_serviceImplementation != null) {
      String packName = m_serviceImplementation.getPackageFragment().getElementName();
      if (ScoutSdk.existsType(packName + "." + newName)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Name already in use.");
      }
    }
    if (m_serviceInterface != null) {
      String packName = m_serviceInterface.getPackageFragment().getElementName();
      if (ScoutSdk.existsType(packName + ".I" + newName)) {
        return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "Name already in use.");
      }
    }
    return inheritedStatus;
  }

  @Override
  protected void fillTransaction(JdtRenameTransaction transaction, String newName) throws CoreException {
    transaction.add(m_serviceImplementation, newName);
    if (m_serviceInterface != null) {
      transaction.add(m_serviceInterface, "I" + newName);
    }
  }

  @Override
  public void run() {
    super.run();
    // TODO rename service proxy registration client side.
  }
}
