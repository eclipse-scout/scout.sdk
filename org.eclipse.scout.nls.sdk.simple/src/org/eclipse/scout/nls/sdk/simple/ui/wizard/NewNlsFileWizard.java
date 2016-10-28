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
package org.eclipse.scout.nls.sdk.simple.ui.wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.nls.sdk.simple.operations.AbstractCreateNlsProjectOperation;
import org.eclipse.scout.nls.sdk.simple.operations.CreateSimpleNlsProjectOperation;
import org.eclipse.scout.nls.sdk.simple.operations.NewNlsFileOperationDesc;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewNlsFileWizard extends BasicNewResourceWizard {

  private NewNlsFileOperationDesc m_desc;

  public NewNlsFileWizard() {
    m_desc = new NewNlsFileOperationDesc();
  }

  @Override
  public void addPages() {
    addPage(new NewNlsFileWizardPage1("Page1", m_desc));
    addPage(new NewNlsFileWizardPage2("Page2", m_desc));
  }

  @Override
  public boolean performFinish() {
    AbstractCreateNlsProjectOperation op = new CreateSimpleNlsProjectOperation(m_desc);

    IStatus status = op.runSync();
    return status.isOK();
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection s) {
    Object o = s.getFirstElement();
    if (o instanceof IResource) {
      m_desc.setPlugin(((IResource) o).getProject());
    }
    else if (o instanceof IJavaElement) {
      m_desc.setPlugin(((IJavaElement) o).getJavaProject().getProject());
    }
    if (m_desc.getPlugin() != null && !m_desc.getPlugin().isOpen()) {
      m_desc.setPlugin(null);
    }
  }
}