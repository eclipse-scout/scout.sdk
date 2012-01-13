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
package org.eclipse.scout.sdk.ui.rap.internal;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.rap.operations.project.CreateAjaxServletOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class ModifyXmlAction extends AbstractScoutHandler {

  private IJavaProject m_serverProject;

  /**
   * @param label
   */
  public ModifyXmlAction() {
    super("Do it...");
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    CreateAjaxServletOperation op = new CreateAjaxServletOperation(getServerProject());
    new OperationJob(op).schedule();
    return null;
  }

  public void setServerProject(IJavaProject serverProject) {
    m_serverProject = serverProject;
  }

  public IJavaProject getServerProject() {
    return m_serverProject;
  }

}
