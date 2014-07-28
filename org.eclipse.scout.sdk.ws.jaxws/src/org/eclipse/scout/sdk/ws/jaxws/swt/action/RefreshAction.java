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
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsIcons;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.swt.widgets.Shell;

public class RefreshAction extends AbstractLinkAction {

  public RefreshAction() {
    super(Texts.get("Refresh"), JaxWsSdk.getImageDescriptor(JaxWsIcons.Refresh));
    setLeadingText(Texts.get("RefreshByClicking"));
    setLinkText("here");
    setToolTip(Texts.get("ReloadResourcesFromDisk"));
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    for (IPage p : selection) {
      p.refresh(true);
    }
    return null;
  }
}
