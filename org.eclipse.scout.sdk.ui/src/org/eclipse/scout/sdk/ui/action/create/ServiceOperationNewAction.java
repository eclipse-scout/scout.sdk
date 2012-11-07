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
package org.eclipse.scout.sdk.ui.action.create;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractWizardAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.services.ServiceOperationNewWizard;

public class ServiceOperationNewAction extends AbstractWizardAction {

  private IType m_interfaceType;
  private IType m_type;

  public ServiceOperationNewAction() {
    super(Texts.get("NewServiceOperation"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceOperationAdd), null, false, Category.NEW);
  }

  public void init(IType interfaceType, IType type) {
    m_type = type;
    m_interfaceType = interfaceType;
  }

  @Override
  protected IWizard getNewWizardInstance() {
    return new ServiceOperationNewWizard(m_interfaceType, new IType[]{m_type});
  }
}
