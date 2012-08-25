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
  public static final String EXTENSION_ELEMENT_SERVICE = "service";
  public static final String EXTENSION_SERVICE_RANKING = "ranking";
  public static final String EXTENSION_POINT_SERVICES = "org.eclipse.scout.service.services";

  public static final String CLIENT_EXTENSION_POINT_SERVICE_PROXIES = "org.eclipse.scout.service.services";
  public static final String CLIENT_EXTENSION_ELEMENT_SERVICE_PROXY = "proxy";

  public static final String CLIENT_PACKAGE_APPENDIX_UI = ".ui";
  public static final String CLIENT_PACKAGE_APPENDIX_UI_DESKTOP = ".ui.desktop";
  public static final String CLIENT_PACKAGE_APPENDIX_UI_DESKTOP_OUTLINES = ".ui.desktop.outlines";
  public static final String CLIENT_PACKAGE_APPENDIX_UI_DESKTOP_OUTLINES_PAGES = ".ui.desktop.outlines.pages";
  public static final String CLIENT_PACKAGE_APPENDIX_UI_FORMS = ".ui.forms";
  public static final String CLIENT_PACKAGE_APPENDIX_UI_TEMPLATE_FORM_FIELD = ".ui.template.formfield";
  public static final String CLIENT_PACKAGE_APPENDIX_UI_SEARCHFORMS = ".ui.searchforms";
  public static final String CLIENT_PACKAGE_APPENDIX_UI_WIZARDFORMS = ".ui.wizardforms";
  public static final String CLIENT_PACKAGE_APPENDIX_UI_WIZARDS = ".ui.wizards";
  public static final String CLIENT_PACKAGE_APPENDIX_SERVICES = ".services";
  public static final String CLIENT_PACKAGE_APPENDIX_SERVICES_LOOKUP = CLIENT_PACKAGE_APPENDIX_SERVICES + ".lookup";

  // shared
  public static final String SHARED_PACKAGE_APPENDIX_SECURITY = ".security";
  public static final String SHARED_PACKAGE_APPENDIX_SERVICES = ".services";
  public static final String SHARED_PACKAGE_APPENDIX_SERVICES_OUTLINE = SHARED_PACKAGE_APPENDIX_SERVICES + ".outline";
  public static final String SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS = SHARED_PACKAGE_APPENDIX_SERVICES + ".process";
  public static final String SHARED_PACKAGE_APPENDIX_SERVICES_LOOKUP = SHARED_PACKAGE_APPENDIX_SERVICES + ".lookup";
  public static final String SHARED_PACKAGE_APPENDIX_SERVICES_CODE = SHARED_PACKAGE_APPENDIX_SERVICES + ".code";
  public static final String SHARED_PACKAGE_APPENDIX_SERVICES_CUSTOM = SHARED_PACKAGE_APPENDIX_SERVICES + ".custom";
  public static final String SHARED_PACKAGE_APPENDIX_SERVICES_COMMON = SHARED_PACKAGE_APPENDIX_SERVICES + ".common";
  public static final String SHARED_PACKAGE_APPENDIX_SERVICES_COMMON_BOOKMARK = SHARED_PACKAGE_APPENDIX_SERVICES_COMMON + ".bookmark";
  public static final String SHARED_PACKAGE_APPENDIX_SERVICES_COMMON_CALENDAR = SHARED_PACKAGE_APPENDIX_SERVICES_COMMON + ".calendar";
  // server
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES = ".services";
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES_LOOKUP = SERVER_PACKAGE_APPENDIX_SERVICES + ".lookup";
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES_PROCESS = SERVER_PACKAGE_APPENDIX_SERVICES + ".process";
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES_OUTLINE = SERVER_PACKAGE_APPENDIX_SERVICES + ".outline";
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES_CODE = SERVER_PACKAGE_APPENDIX_SERVICES + ".code";
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES_CUSTOM = SERVER_PACKAGE_APPENDIX_SERVICES + ".custom";
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES_COMMON = SERVER_PACKAGE_APPENDIX_SERVICES + ".common";
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES_COMMON_BOOKMARK = SERVER_PACKAGE_APPENDIX_SERVICES_COMMON + ".bookmark";
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES_COMMON_CALENDAR = SERVER_PACKAGE_APPENDIX_SERVICES_COMMON + ".calendar";
  public static final String SERVER_PACKAGE_APPENDIX_SERVICES_COMMON_SQL = SERVER_PACKAGE_APPENDIX_SERVICES_COMMON + ".sql";
}
