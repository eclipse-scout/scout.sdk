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

import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ws.jaxws.Texts;

public class ProviderNewWizardAction extends AbstractLinkAction {

  public ProviderNewWizardAction() {
    super(Texts.get("Action_newTypeX", Texts.get("Provider")), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd));
    setLeadingText(Texts.get("CreateNewWsProviderByClicking"));
    setLinkText(Texts.get("here"));
  }
}
