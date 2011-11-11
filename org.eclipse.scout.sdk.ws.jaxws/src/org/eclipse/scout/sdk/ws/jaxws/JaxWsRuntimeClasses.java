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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ScoutSdk;

public final class JaxWsRuntimeClasses {

  private JaxWsRuntimeClasses() {
  }

  public static final String JaxWsPlugin = "org.eclipse.scout.jaxws216";
  public static final IType JaxWsServlet = ScoutSdk.getType("org.eclipse.scout.jaxws216.JaxWsServlet");

  public static final IType JaxWsActivator = ScoutSdk.getType("org.eclipse.scout.jaxws216.Activator");
  public static final IType ScoutWebService = ScoutSdk.getType("org.eclipse.scout.jaxws216.annotation.ScoutWebService");
  public static final IType ScoutWebServiceClient = ScoutSdk.getType("org.eclipse.scout.jaxws216.annotation.ScoutWebServiceClient");
  public static final IType ScoutTransaction = ScoutSdk.getType("org.eclipse.scout.jaxws216.annotation.ScoutTransaction");

  public static final String WsImportType = "com.sun.tools.internal.ws.WsImport";

  public static final IType IServerSessionFactory = ScoutSdk.getType("org.eclipse.scout.jaxws216.session.IServerSessionFactory");
  public static final IType IAuthenticationHandlerProvider = ScoutSdk.getType("org.eclipse.scout.jaxws216.security.provider.IAuthenticationHandler");
  public static final IType ICredentialValidationStrategy = ScoutSdk.getType("org.eclipse.scout.jaxws216.security.provider.ICredentialValidationStrategy");
  public static final IType IAuthenticationHandlerConsumer = ScoutSdk.getType("org.eclipse.scout.jaxws216.security.consumer.IAuthenticationHandler");

  public static final IType DefaultServerSessionFactory = ScoutSdk.getType("org.eclipse.scout.jaxws216.session.DefaultServerSessionFactory");
  public static final IType BasicAuthenticationHandlerProvider = ScoutSdk.getType("org.eclipse.scout.jaxws216.security.provider.BasicAuthenticationHandler");
  public static final IType BasicAuthenticationHandlerConsumer = ScoutSdk.getType("org.eclipse.scout.jaxws216.security.consumer.BasicAuthenticationHandler");
  public static final IType NullAuthenticationHandlerConsumer = ScoutSdk.getType("org.eclipse.scout.jaxws216.security.consumer.IAuthenticationHandler$NONE");
  public static final IType ConfigIniCredentialValidationStrategy = ScoutSdk.getType("org.eclipse.scout.jaxws216.security.provider.ConfigIniCredentialValidationStrategy");

  public static final IType JaxWsStubGenerator = ScoutSdk.getType("org.eclipse.scout.jaxws216.tool.JaxWsStubGenerator");

  public static final IType IWebServiceClient = ScoutSdk.getType("org.eclipse.scout.jaxws216.service.IWebServiceClient");
  public static final IType AbstractWebServiceClient = ScoutSdk.getType("org.eclipse.scout.jaxws216.service.AbstractWebServiceClient");

  public static final String PROP_SWS_SESSION_FACTORY = "sessionFactory";
  public static final String PROP_SWS_AUTH_HANDLER = "authenticationHandler";
  public static final String PROP_SWS_CREDENTIAL_STRATEGY = "credentialValidationStrategy";

  public static final IType DefaultTimezoneDateAdapter = ScoutSdk.getType("org.eclipse.scout.jaxws216.adapters.DefaultTimezoneDateAdapter");
  public static final IType CalendarAdapter = ScoutSdk.getType("org.eclipse.scout.jaxws216.adapters.CalendarAdapter");
  public static final IType UtcDateAdapter = ScoutSdk.getType("org.eclipse.scout.jaxws216.adapters.UtcDateAdapter");

  public static final IType ServiceTunnelServlet = ScoutSdk.getType("org.eclipse.scout.rt.server.ServiceTunnelServlet");

}
