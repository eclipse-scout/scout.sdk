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

import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;

public class StubRebuildAction extends AbstractLinkAction {

  public StubRebuildAction() {
    super(Texts.get("RebuildWebserviceStub"), JaxWsSdk.getImageDescriptor(JaxWsIcons.RebuildWsStub));
    setLinkText(Texts.get("RebuildWebserviceStub"));
    setToolTip(Texts.get("ClickToGenerateWsStub"));
  }
}
