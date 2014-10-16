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
package org.eclipse.scout.sdk.ws.jaxws.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ws.jaxws.executor.param.HandlerParams;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.HandlerChainFilterWizard;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link HandlerChainFilterEditExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class HandlerChainFilterEditExecutor extends AbstractExecutor {

  private HandlerParams m_params;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    Object firstElement = selection.getFirstElement();
    if (firstElement instanceof HandlerParams) {
      m_params = (HandlerParams) firstElement;
    }
    return m_params != null && isEditable(m_params.getBundle());
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    HandlerChainFilterWizard wizard = new HandlerChainFilterWizard();
    wizard.init(m_params.getBundle(), m_params.getSunJaxWsBean(), m_params.getXmlHandlerChain());

    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setHelpAvailable(false);
    wizardDialog.setPageSize(100, 350);
    wizardDialog.open();
    return null;
  }

}
