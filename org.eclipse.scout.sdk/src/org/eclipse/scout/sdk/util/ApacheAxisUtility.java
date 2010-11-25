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
package org.eclipse.scout.sdk.util;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.pde.PdeUtility;

/**
 * For details see http://ws.apache.org/axis/java/reference.html
 */
public final class ApacheAxisUtility {

  private ApacheAxisUtility() {
  }

  public static void runWsdlToJava(URI wsdlUri, IProject project, File sourceDir, String implClassQName, String username, String password, IProgressMonitor p) throws CoreException {
    String implClassArg = "";
    if (implClassQName != null) {
      implClassArg = " -c \"" + implClassQName + "\"";
    }
    String authentication = "";
    if (username != null && password != null) {
      authentication = " -U " + username + " -P " + password;
    }
    PdeUtility.launchLocalJavaApplicationAndWait(
        "BSI CASE - Axis Wsdl2Java",
        project.getName(),
        "org.apache.axis.wsdl.WSDL2Java",
        "-o \"" + sourceDir.getAbsolutePath() + "\" -d Request -a -s -S false " + implClassArg + authentication + " \"" + wsdlUri.toString() + "\"",
        true,
        p
        );
  }

  /**
   * @param wsdlFile
   *          target file that is created
   * @param publishWsdlUrl
   *          for example "http://localhost:8080/bsicrm/services/Test"
   * @param namespace
   *          for example "urn:Test"
   * @param packageToNamespaceMap
   *          for example [["com.bsiag.crm.server.services.soap.test", "urn:Test"]]
   * @param serviceClass
   *          for example "com.bsiag.crm.server.services.soap.test.MyService"
   *          Not yet used arguments
   *          -e, --extraClasses <argument>
   *          A space or comma separated list of class names to be added to the type section.
   *          -C, --importSchema
   *          A file or URL to an XML Schema that should be physically imported into the generated WSDL
   *          -X, --classpath
   *          additional classpath elements
   */
  public static void runJavaToWsdl(File wsdlFile, String publishWsdlUrl, String namespace, String[][] packageToNamespaceMap, String serviceClass, IProject project, IProgressMonitor p) throws CoreException {
    String serviceName = serviceClass.replaceAll("^(.*\\.)?([^.]+)$", "$2");
    //
    StringBuilder b = new StringBuilder();
    if (packageToNamespaceMap != null) {
      for (String[] pair : packageToNamespaceMap) {
        b.append(" -p\"");
        b.append(pair[0]);
        b.append("\" \"");
        b.append(pair[1]);
        b.append("\"");
      }
    }
    String pArg = b.toString();
    PdeUtility.launchLocalJavaApplicationAndWait(
        "BSI CASE - Axis Java2Wsdl",
        project.getName(),
        "org.apache.axis.wsdl.Java2WSDL",
        "-o \"" + wsdlFile.getAbsolutePath() + "\" -l \"" + publishWsdlUrl + "\" -n \"" + namespace + "\" -P " + serviceName + "PortType -S " + serviceName + "SOAPService -s " + serviceName + "Port -w All -T 1.2 -A OPERATION -y RPC -u LITERAL" + pArg + " \"" + serviceClass + "\"",
        true,
        p
        );
  }

  public static void runDeploy(File deployFile, String role, IProject project, IProgressMonitor p) throws CoreException {
    PdeUtility.launchLocalJavaApplicationAndWait(
        "BSI CASE - Deploy WSDD",
        project.getName(),
        "org.apache.axis.utils.Admin",
        role + " \"" + deployFile.getAbsolutePath() + "\"",
        true,
        p
        );
  }

  public static void runUndeploy(IFile undeployFile, String role, IProject project, IProgressMonitor p) throws CoreException {
    if (undeployFile.exists()) {
      PdeUtility.launchLocalJavaApplicationAndWait(
          "BSI CASE - Undeploy WSDD",
          project.getName(),
          "org.apache.axis.utils.Admin",
          role + " \"" + undeployFile.getLocation() + "\"",
          true,
          p
          );
    }
  }

  public static String reversePackageName(String pck) {
    if (pck != null && pck.length() > 0) {
      String[] a = pck.split("[.]");
      pck = "";
      for (String s : a) {
        pck = "." + s + pck;
      }
      pck = pck.substring(1);
    }
    return pck;
  }

  public static String createPackageName(File srcFolder, File f) {
    String path = f.getParentFile().getAbsolutePath().substring(srcFolder.getAbsolutePath().length()).replace('\\', '/');
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    return path.replaceAll("[/]", ".");
  }

  public static String createPackagePath(String pckName) {
    return pckName.replace('.', '/');
  }

}
