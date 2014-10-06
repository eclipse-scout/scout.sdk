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
package org.eclipse.scout.sdk.ws.jaxws.ext;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.extensions.IPageFactory;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsRuntimeClasses;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.pages.WebServicesTablePage;

public class WebServicePageFactory implements IPageFactory {

  public WebServicePageFactory() {
  }

  @Override
  public void createChildren(IPage parentPage) {
    IScoutBundle bundle = parentPage.getScoutBundle();
    if (!bundle.hasType(IScoutBundle.TYPE_SERVER)) {
      return;
    }

    if (!isJaxWsDependencyInstalled(bundle)) {
      return;
    }

    // contribute WebServicesTablePage
    new WebServicesTablePage(parentPage);
  }

  private boolean isJaxWsDependencyInstalled(IScoutBundle bundle) {
    IType activator = TypeUtility.getType(JaxWsRuntimeClasses.JaxWsActivator);
    return TypeUtility.exists(activator) && TypeUtility.isOnClasspath(activator, bundle.getJavaProject());
  }
}
