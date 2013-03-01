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
package org.eclipse.scout.sdk.ws.jaxws.swt.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.model.BuildJaxWsBean;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.WsConsumerDeleteWizard;
import org.eclipse.swt.widgets.Shell;

public class WsConsumerDeleteAction extends AbstractScoutHandler {

  private IScoutBundle m_bundle;
  private BuildJaxWsBean m_buildJaxWsBean;
  private IType m_type;

  public WsConsumerDeleteAction() {
    super("Delete...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolRemove), "Delete", false, Category.DELETE);
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  public void init(IScoutBundle bundle, IType type, BuildJaxWsBean buildJaxWsBean) {
    setLabel(Texts.get("Action_deleteTypeX", "'" + type.getElementName() + "'"));
    m_bundle = bundle;
    m_type = type;
    m_buildJaxWsBean = buildJaxWsBean;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    WsConsumerDeleteWizard wizard = new WsConsumerDeleteWizard();
    wizard.setBundle(m_bundle);
    wizard.setType(m_type);
    wizard.setBuildJaxWsBean(m_buildJaxWsBean);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    wizardDialog.open();
    return null;
  }
}
