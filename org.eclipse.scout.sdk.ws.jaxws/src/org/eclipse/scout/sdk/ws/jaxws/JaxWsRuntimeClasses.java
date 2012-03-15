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


public final class JaxWsRuntimeClasses {

  private JaxWsRuntimeClasses() {
  }

  public static final String JaxWsPlugin = "org.eclipse.scout.jaxws";
  public static final String JaxWsServlet = "org.eclipse.scout.jaxws.JaxWsServlet";

  public static final String JaxWsActivator = "org.eclipse.scout.jaxws.Activator";
  public static final String ScoutWebService = "org.eclipse.scout.jaxws.annotation.ScoutWebService";
  public static final String ScoutWebServiceClient = "org.eclipse.scout.jaxws.annotation.ScoutWebServiceClient";
  public static final String ScoutTransaction = "org.eclipse.scout.jaxws.annotation.ScoutTransaction";

  public static final String WsImportType = "com.sun.tools.internal.ws.WsImport";

  public static final String IServerSessionFactory = "org.eclipse.scout.jaxws.session.IServerSessionFactory";
  public static final String IAuthenticationHandlerProvider = "org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler";
  public static final String ICredentialValidationStrategy = "org.eclipse.scout.jaxws.security.provider.ICredentialValidationStrategy";
  public static final String IAuthenticationHandlerConsumer = "org.eclipse.scout.jaxws.security.consumer.IAuthenticationHandler";

  public static final String DefaultServerSessionFactory = "org.eclipse.scout.jaxws.session.DefaultServerSessionFactory";
  public static final String BasicAuthenticationHandlerProvider = "org.eclipse.scout.jaxws.security.provider.BasicAuthenticationHandler";
  public static final String BasicAuthenticationHandlerConsumer = "org.eclipse.scout.jaxws.security.consumer.BasicAuthenticationHandler";
  public static final String NullAuthenticationHandlerConsumer = "org.eclipse.scout.jaxws.security.consumer.IAuthenticationHandler$NONE";
  public static final String NullAuthenticationHandlerProvider = "org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler$NONE";
  public static final String ConfigIniCredentialValidationStrategy = "org.eclipse.scout.jaxws.security.provider.ConfigIniCredentialValidationStrategy";

  public static final String JaxWsStubGenerator = "org.eclipse.scout.jaxws.tool.JaxWsStubGenerator";

  public static final String IWebServiceClient = "org.eclipse.scout.jaxws.service.IWebServiceClient";
  public static final String AbstractWebServiceClient = "org.eclipse.scout.jaxws.service.AbstractWebServiceClient";

  public static final String PROP_SWS_SESSION_FACTORY = "sessionFactory";
  public static final String PROP_SWS_AUTH_HANDLER = "authenticationHandler";
  public static final String PROP_SWS_CREDENTIAL_STRATEGY = "credentialValidationStrategy";

  public static final String DefaultTimezoneDateAdapter = "org.eclipse.scout.jaxws.adapters.DefaultTimezoneDateAdapter";
  public static final String CalendarAdapter = "org.eclipse.scout.jaxws.adapters.CalendarAdapter";
  public static final String UtcDateAdapter = "org.eclipse.scout.jaxws.adapters.UtcDateAdapter";

  public static final String ServiceTunnelServlet = "org.eclipse.scout.rt.server.ServiceTunnelServlet";

}
