/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.HandlerNewOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.page.HandlerNewWizardPage;

public class HandlerNewWizard extends AbstractWorkspaceWizard {

  public static final int TYPE_SERVICE_IMPLEMENTATION = 100;
  public static final int TYPE_SERVICE_REG_SERVER = 101;

  private IScoutBundle m_bundle;
  private HandlerNewWizardPage m_page;

  private HandlerNewOperation m_op;

  public HandlerNewWizard(IScoutBundle bundle) {
    m_bundle = bundle;
    setWindowTitle(Texts.get("CreateHandler"));
  }

  @Override
  public void addPages() {
    m_page = new HandlerNewWizardPage(m_bundle);
    addPage(m_page);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_op = new HandlerNewOperation();
    m_op.setBundle(m_bundle);
    m_op.setPackageName(m_page.getPackageName());
    m_op.setTypeName(m_page.getTypeName());
    m_op.setTransactional(m_page.isTransactional());
    m_op.setSessionFactoryType(m_page.getSessionFactoryType());
    m_op.setSuperType(m_page.getSuperType());
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    m_op.run(monitor, workingCopyManager);
    return true;
  }
}
