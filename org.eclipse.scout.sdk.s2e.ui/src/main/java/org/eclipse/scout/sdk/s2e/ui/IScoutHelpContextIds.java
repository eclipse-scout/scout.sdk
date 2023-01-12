/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui;

import org.eclipse.scout.sdk.s2e.doc.IContextsScoutXmlIds;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;

/**
 * <h3>{@link IScoutHelpContextIds}</h3>
 *
 * @since 5.2.0
 */
public interface IScoutHelpContextIds {

  String PREFIX = S2ESdkUiActivator.PLUGIN_ID + '.';

  String SCOUT_PROJECT_NEW_WIZARD_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_PROJECT_NEW_WIZARD_ID;
  String SCOUT_FORM_NEW_WIZARD_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_FORM_NEW_WIZARD_ID;
  String SCOUT_PAGE_NEW_WIZARD_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_ID_NEW_WIZARD_ID;
  String SCOUT_ENTRY_WIZARD_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_ENTRY_WIZARD_ID;
  String SCOUT_LOOKUPCALL_NEW_WIZARD_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_LOOKUPCALL_NEW_WIZARD_ID;
  String SCOUT_CODETYPE_NEW_WIZARD_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_CODETYPE_NEW_WIZARD_ID;
  String SCOUT_PERMISSION_NEW_WIZARD_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_PERMISSION_NEW_WIZARD_ID;
  String SCOUT_LANGUAGE_NEW_WIZARD_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_LANGUAGE_NEW_WIZARD_ID;
  String SCOUT_WEB_SERVICE_NEW_WIZARD_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_WEB_SERVICE_NEW_WIZARD_ID;
  String SCOUT_WEB_SERVICE_EDITOR_PAGE = PREFIX + IContextsScoutXmlIds.SCOUT_WEB_SERVICE_EDITOR_ID;
}
