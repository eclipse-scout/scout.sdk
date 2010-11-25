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
package org.eclipse.scout.sdk.operation.axis;

import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.operation.template.InstallJavaFileOperation;
import org.eclipse.scout.sdk.pde.PluginXml;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public class AxisWebServiceServerSetupOperation extends AxisWebServiceClientSetupOperation {
  private static Logger LOG = Logger.getLogger(ScoutSdk.class.getName());

  public AxisWebServiceServerSetupOperation(IScoutBundle project) {
    super(project);
  }

  @Override
  public String getOperationName() {
    return "Setup/Repair Webservice Provider Environment";
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    super.run(monitor, workingCopyManager);
    try {
      // add servlet
      IType axisServletExType = ScoutSdk.getType(getProject().getRootPackageName() + ".AxisServletEx");
      if (TypeUtility.exists(axisServletExType)) {
        new InstallJavaFileOperation("templates/server/src/AxisServletEx.java", "AxisServletEx.java", getProject()).run(monitor, workingCopyManager);
      }
      // add servlet xp
      PluginXml pluginXml = new PluginXml(getProject().getProject());
      SimpleXmlElement xpElem = pluginXml.getOrCreateExtension("org.eclipse.equinox.http.registry.servlets");
      SimpleXmlElement servletElem = null;
      String axisServletQname = getProject().getRootPackageName() + ".AxisServletEx";
      for (SimpleXmlElement e : xpElem.getChildren("servlet")) {
        if ("org.apache.axis.transport.http.AxisServlet".equals(e.getAttribute("class"))) {
          // transform legacy
          e.setAttribute("class", axisServletQname);
        }
        if (axisServletQname.equals(e.getAttribute("class"))) {
          if (servletElem == null) {
            servletElem = e;
          }
          else {
            xpElem.removeChild(e);
          }
        }
      }
      if (servletElem == null) {
        servletElem = new SimpleXmlElement("servlet");
        servletElem.setAttribute("alias", "/services");
        servletElem.setAttribute("class", getProject().getRootPackageName() + ".AxisServletEx");
        xpElem.addChild(servletElem);
      }
      pluginXml.store(monitor);
    }
    catch (Exception e) {
      throw new CoreException(new ScoutStatus(e));
    }
  }

}
