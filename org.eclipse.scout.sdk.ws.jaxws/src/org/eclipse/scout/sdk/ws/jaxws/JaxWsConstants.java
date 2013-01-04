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
package org.eclipse.scout.sdk.ws.jaxws;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public final class JaxWsConstants {

  public static final IPath PATH_WEB_INF = new Path("/WEB-INF");
  public static final IPath PATH_WSDL_PROVIDER = new Path("/WEB-INF/wsdl/provider");
  public static final IPath PATH_WSDL_CONSUMER = new Path("/WEB-INF/wsdl/consumer");
  public static final IPath PATH_BUILD = new Path("/WEB-INF/build");
  public static final IPath PATH_SUN_JAXWS = new Path("/WEB-INF/sun-jaxws.xml");
  public static final IPath PATH_BUILD_JAXWS = new Path("/WEB-INF/build/build-jaxws.xml");
  public static final String JAX_WS_ALIAS = "/jaxws";
  public static final String SERVER_EXTENSION_POINT_SERVLETS = "org.eclipse.equinox.http.registry.servlets";

  public static final String SUFFIX_WS_PROVIDER = "WebService";
  public static final String SUFFIX_WS_CONSUMER = "WebServiceClient";
  public static final String SUFFIX_HANDLER = "Handler";

  public static final String OPTION_BINDING_FILE = "b";
  public static final String OPTION_PACKAGE = "p";
  public static final String OPTION_JAR = "jar";

  public static final IPath STUB_FOLDER = new Path("ws-stub");

  public static final int GENERICS_WEBSERVICE_CLIENT_SERVICE_INDEX = 0;
  public static final int GENERICS_WEBSERVICE_CLIENT_PORT_TYPE_INDEX = 1;

  private JaxWsConstants() {
  }

  public enum MarkerType {
    JaxWs(JaxWsSdk.PLUGIN_ID + ".jaxws"),
    UrlPattern(JaxWsSdk.PLUGIN_ID + ".urlPattern"),
    Service(JaxWsSdk.PLUGIN_ID + ".service"),
    TargetNamespace(JaxWsSdk.PLUGIN_ID + ".targetNamespace"),
    ServiceType(JaxWsSdk.PLUGIN_ID + ".serviceType"),
    Port(JaxWsSdk.PLUGIN_ID + ".port"),
    PortType(JaxWsSdk.PLUGIN_ID + ".porttype"),
    StubFolder(JaxWsSdk.PLUGIN_ID + ".stubFolder"),
    StubJar(JaxWsSdk.PLUGIN_ID + ".stubJar"),
    Implementation(JaxWsSdk.PLUGIN_ID + ".implementation"),
    EndpointInterface(JaxWsSdk.PLUGIN_ID + ".endpointInterface"),
    Package(JaxWsSdk.PLUGIN_ID + ".package"),
    Wsdl(JaxWsSdk.PLUGIN_ID + ".wsdl"),
    WsdlFolder(JaxWsSdk.PLUGIN_ID + ".wsdlFolder"),
    BindingFile(JaxWsSdk.PLUGIN_ID + ".bindingFile"),
    MissingBuildJaxWsEntry(JaxWsSdk.PLUGIN_ID + ".missingBuildJaxWsEntry"),
    HandlerClass(JaxWsSdk.PLUGIN_ID + ".handlerClazz");

    private String m_id;

    private MarkerType(String id) {
      m_id = id;
    }

    public String getId() {
      return m_id;
    }
  }
}
