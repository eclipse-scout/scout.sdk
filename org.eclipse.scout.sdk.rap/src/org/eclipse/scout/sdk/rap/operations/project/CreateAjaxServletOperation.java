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
package org.eclipse.scout.sdk.rap.operations.project;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
public class CreateAjaxServletOperation implements IOperation {

  private final IJavaProject m_serverProject;

  public CreateAjaxServletOperation(IJavaProject serverProject) {
    m_serverProject = serverProject;

  }

  @Override
  public String getOperationName() {
    return "Create ajax servlet on server";
  }

  @Override
  public void validate() throws IllegalArgumentException {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    PluginModelHelper h = new PluginModelHelper(getServerProject().getProject());
    HashMap<String, String> attributes = new HashMap<String, String>();
    attributes.put("alias", "/ajax");
    if (!h.PluginXml.existsSimpleExtension(IRuntimeClasses.EXTENSION_POINT_EQUINOX_SERVLETS, IRuntimeClasses.EXTENSION_ELEMENT_SERVLET, attributes)) {
      attributes.put("class", "org.eclipse.scout.rt.server.ServiceTunnelServlet");
      h.PluginXml.addSimpleExtension(IRuntimeClasses.EXTENSION_POINT_EQUINOX_SERVLETS, IRuntimeClasses.EXTENSION_ELEMENT_SERVLET, attributes);
      h.save();
    }
  }

  public IJavaProject getServerProject() {
    return m_serverProject;
  }

}
