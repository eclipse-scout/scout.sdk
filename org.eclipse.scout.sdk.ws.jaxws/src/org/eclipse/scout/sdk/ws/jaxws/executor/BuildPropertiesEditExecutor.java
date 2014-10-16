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
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceConsumerNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServiceProviderNodePage;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.BuildPropertiesWizard;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link BuildPropertiesEditExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class BuildPropertiesEditExecutor extends AbstractExecutor {

  private IScoutBundle m_bundle;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_bundle = UiUtility.getScoutBundleFromSelection(selection);
    return isEditable(m_bundle);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    Object selected = selection.getFirstElement();
    BuildJaxWsBean buildJaxWsBean;
    if (selected instanceof WebServiceConsumerNodePage) {
      buildJaxWsBean = ((WebServiceConsumerNodePage) selected).getBuildJaxWsBean();
    }
    else if (selected instanceof WebServiceProviderNodePage) {
      buildJaxWsBean = ((WebServiceProviderNodePage) selected).getBuildJaxWsBean();
    }
    else {
      return null;
    }

    BuildPropertiesWizard wizard = new BuildPropertiesWizard(m_bundle, buildJaxWsBean);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.setHelpAvailable(false);
    wizardDialog.setPageSize(100, 500);
    wizardDialog.open();

    return null;
  }

}
