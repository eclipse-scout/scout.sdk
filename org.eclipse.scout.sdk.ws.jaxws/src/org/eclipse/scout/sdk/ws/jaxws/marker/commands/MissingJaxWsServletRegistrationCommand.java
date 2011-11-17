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
package org.eclipse.scout.sdk.ws.jaxws.marker.commands;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsConstants;
import org.eclipse.scout.sdk.ws.jaxws.operation.JaxWsServletRegistrationOperation;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;

public class MissingJaxWsServletRegistrationCommand extends AbstractExecutableMarkerCommand {

  private IScoutBundle m_bundle;

  public MissingJaxWsServletRegistrationCommand(IScoutBundle bundle) {
    super("Missing or invalid JAX-WS Servlet registration");
    m_bundle = bundle;
    IScoutBundle rootServerBundle = JaxWsSdkUtility.getServletContributingBundle(bundle);
    String rootServerBundleName;
    if (rootServerBundle != null) {
      rootServerBundleName = rootServerBundle.getBundleName();
    }
    else {
      rootServerBundleName = "?";
    }
    setSolutionDescription("By using this task, the JAX-WS Servlet is registered in plugin.xml of bundle '" + rootServerBundleName + "' with the alias '" + JaxWsConstants.JAX_WS_ALIAS + "'");
  }

  @Override
  public void execute(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    JaxWsServletRegistrationOperation op = new JaxWsServletRegistrationOperation();
    op.setBundle(m_bundle);
    op.setJaxWsAlias(JaxWsConstants.JAX_WS_ALIAS);
    op.run(monitor, workingCopyManager);
  }
}
