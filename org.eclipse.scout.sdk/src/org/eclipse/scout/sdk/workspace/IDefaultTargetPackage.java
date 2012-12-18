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
package org.eclipse.scout.sdk.workspace;

/**
 * <h3>{@link IDefaultTargetPackage}</h3> Specifies package identifiers to be used by IScoutBundle.getPackageName().
 * Must match the id's of the org.eclipse.scout.sdk.targetPackage extensions.
 * 
 * @author mvi
 * @since 3.9.0 17.12.2012
 * @see IScoutBundle
 */
public interface IDefaultTargetPackage {
  String CLIENT_DESKTOP = "client.desktop";
  String CLIENT_OUTLINES = "client.outline";
  String CLIENT_PAGES = "client.page";
  String CLIENT_FORMS = "client.form";
  String CLIENT_TEMPLATE_FORMFIELD = "client.template.formfield";
  String CLIENT_SEARCHFORMS = "client.searchform";
  String CLIENT_WIZARDS = "client.wizard";
  String CLIENT_SERVICES = "client.services";
  String CLIENT_SERVICES_LOOKUP = "client.services.lookup";

  // shared
  String SHARED_SECURITY = "shared.security";
  String SHARED_SERVICES = "shared.services";
  String SHARED_SERVICES_LOOKUP = "shared.services.lookup";
  String SHARED_SERVICES_CODE = "shared.services.code";

  // server
  String SERVER_SERVICES = "server.services";
  String SERVER_SERVICES_LOOKUP = "server.services.lookup";
  String SERVER_SERVICES_BOOKMARK = "server.services.bookmark";
  String SERVER_SERVICES_CALENDAR = "server.services.calendar";
  String SERVER_SERVICES_SQL = "server.services.sql";
  String SERVER_SERVICES_SMTP = "server.services.smtp";
}
