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
import org.eclipse.scout.sdk.util.type.TypeUtility;

public final class JaxWsRuntimeClasses {

  private JaxWsRuntimeClasses() {
  }

  public static final String JaxWsPlugin = "org.eclipse.scout.jaxws";
  public static final IType JaxWsServlet = TypeUtility.getType("org.eclipse.scout.jaxws.JaxWsServlet");

  public static final IType JaxWsActivator = TypeUtility.getType("org.eclipse.scout.jaxws.Activator");
  public static final IType ScoutWebService = TypeUtility.getType("org.eclipse.scout.jaxws.annotation.ScoutWebService");
  public static final IType ScoutWebServiceClient = TypeUtility.getType("org.eclipse.scout.jaxws.annotation.ScoutWebServiceClient");
  public static final IType ScoutTransaction = TypeUtility.getType("org.eclipse.scout.jaxws.annotation.ScoutTransaction");

  public static final String WsImportType = "com.sun.tools.internal.ws.WsImport";

  public static final IType IServerSessionFactory = TypeUtility.getType("org.eclipse.scout.jaxws.session.IServerSessionFactory");
  public static final IType IAuthenticationHandlerProvider = TypeUtility.getType("org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler");
  public static final IType ICredentialValidationStrategy = TypeUtility.getType("org.eclipse.scout.jaxws.security.provider.ICredentialValidationStrategy");
  public static final IType IAuthenticationHandlerConsumer = TypeUtility.getType("org.eclipse.scout.jaxws.security.consumer.IAuthenticationHandler");

  public static final IType DefaultServerSessionFactory = TypeUtility.getType("org.eclipse.scout.jaxws.session.DefaultServerSessionFactory");
  public static final IType BasicAuthenticationHandlerProvider = TypeUtility.getType("org.eclipse.scout.jaxws.security.provider.BasicAuthenticationHandler");
  public static final IType BasicAuthenticationHandlerConsumer = TypeUtility.getType("org.eclipse.scout.jaxws.security.consumer.BasicAuthenticationHandler");
  public static final IType NullAuthenticationHandlerConsumer = TypeUtility.getType("org.eclipse.scout.jaxws.security.consumer.IAuthenticationHandler$NONE");
  public static final IType NullAuthenticationHandlerProvider = TypeUtility.getType("org.eclipse.scout.jaxws.security.provider.IAuthenticationHandler$NONE");
  public static final IType ConfigIniCredentialValidationStrategy = TypeUtility.getType("org.eclipse.scout.jaxws.security.provider.ConfigIniCredentialValidationStrategy");

  public static final IType JaxWsStubGenerator = TypeUtility.getType("org.eclipse.scout.jaxws.tool.JaxWsStubGenerator");

  public static final IType IWebServiceClient = TypeUtility.getType("org.eclipse.scout.jaxws.service.IWebServiceClient");
  public static final IType AbstractWebServiceClient = TypeUtility.getType("org.eclipse.scout.jaxws.service.AbstractWebServiceClient");

  public static final String PROP_SWS_SESSION_FACTORY = "sessionFactory";
  public static final String PROP_SWS_AUTH_HANDLER = "authenticationHandler";
  public static final String PROP_SWS_CREDENTIAL_STRATEGY = "credentialValidationStrategy";

  public static final IType DefaultTimezoneDateAdapter = TypeUtility.getType("org.eclipse.scout.jaxws.adapters.DefaultTimezoneDateAdapter");
  public static final IType CalendarAdapter = TypeUtility.getType("org.eclipse.scout.jaxws.adapters.CalendarAdapter");
  public static final IType UtcDateAdapter = TypeUtility.getType("org.eclipse.scout.jaxws.adapters.UtcDateAdapter");

  public static final IType ServiceTunnelServlet = TypeUtility.getType("org.eclipse.scout.rt.server.ServiceTunnelServlet");

}
