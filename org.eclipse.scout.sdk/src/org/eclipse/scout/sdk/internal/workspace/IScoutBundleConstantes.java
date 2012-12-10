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
package org.eclipse.scout.sdk.internal.workspace;

/**
 *
 */
public interface IScoutBundleConstantes {
  // extension points
  String EXTENSION_ELEMENT_SERVICE = "service";
  String EXTENSION_SERVICE_RANKING = "ranking";
  String EXTENSION_POINT_SERVICES = "org.eclipse.scout.service.services";

  // client
  String CLIENT_EXTENSION_POINT_SERVICE_PROXIES = "org.eclipse.scout.service.services";
  String CLIENT_EXTENSION_ELEMENT_SERVICE_PROXY = "proxy";
  String CLIENT_PACKAGE_APPENDIX_UI = ".ui";
  String CLIENT_PACKAGE_APPENDIX_UI_DESKTOP = ".ui.desktop";
  String CLIENT_PACKAGE_APPENDIX_UI_DESKTOP_OUTLINES = ".ui.desktop.outlines";
  String CLIENT_PACKAGE_APPENDIX_UI_DESKTOP_OUTLINES_PAGES = ".ui.desktop.outlines.pages";
  String CLIENT_PACKAGE_APPENDIX_UI_FORMS = ".ui.forms";
  String CLIENT_PACKAGE_APPENDIX_UI_TEMPLATE_FORM_FIELD = ".ui.template.formfield";
  String CLIENT_PACKAGE_APPENDIX_UI_SEARCHFORMS = ".ui.searchforms";
  String CLIENT_PACKAGE_APPENDIX_UI_WIZARDFORMS = ".ui.wizardforms";
  String CLIENT_PACKAGE_APPENDIX_UI_WIZARDS = ".ui.wizards";
  String CLIENT_PACKAGE_APPENDIX_SERVICES = ".services";
  String CLIENT_PACKAGE_APPENDIX_SERVICES_LOOKUP = CLIENT_PACKAGE_APPENDIX_SERVICES + ".lookup";

  // shared
  String SHARED_PACKAGE_APPENDIX_SECURITY = ".security";
  String SHARED_PACKAGE_APPENDIX_SERVICES = ".services";
  String SHARED_PACKAGE_APPENDIX_SERVICES_LOOKUP = SHARED_PACKAGE_APPENDIX_SERVICES + ".lookup";
  String SHARED_PACKAGE_APPENDIX_SERVICES_CODE = SHARED_PACKAGE_APPENDIX_SERVICES + ".code";
  String SHARED_PACKAGE_APPENDIX_SERVICES_COMMON = SHARED_PACKAGE_APPENDIX_SERVICES + ".common";
  String SHARED_PACKAGE_APPENDIX_SERVICES_COMMON_BOOKMARK = SHARED_PACKAGE_APPENDIX_SERVICES_COMMON + ".bookmark";
  String SHARED_PACKAGE_APPENDIX_SERVICES_COMMON_CALENDAR = SHARED_PACKAGE_APPENDIX_SERVICES_COMMON + ".calendar";

  // server
  String SERVER_PACKAGE_APPENDIX_SERVICES = ".services";
  String SERVER_PACKAGE_APPENDIX_SERVICES_LOOKUP = SERVER_PACKAGE_APPENDIX_SERVICES + ".lookup";
  String SERVER_PACKAGE_APPENDIX_SERVICES_COMMON = SERVER_PACKAGE_APPENDIX_SERVICES + ".common";
  String SERVER_PACKAGE_APPENDIX_SERVICES_COMMON_BOOKMARK = SERVER_PACKAGE_APPENDIX_SERVICES_COMMON + ".bookmark";
  String SERVER_PACKAGE_APPENDIX_SERVICES_COMMON_CALENDAR = SERVER_PACKAGE_APPENDIX_SERVICES_COMMON + ".calendar";
  String SERVER_PACKAGE_APPENDIX_SERVICES_COMMON_SQL = SERVER_PACKAGE_APPENDIX_SERVICES_COMMON + ".sql";
  String SERVER_PACKAGE_APPENDIX_SERVICES_COMMON_SMTP = SERVER_PACKAGE_APPENDIX_SERVICES_COMMON + ".smtp";
}
