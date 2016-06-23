/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui;

import org.eclipse.scout.sdk.s2e.doc.IContextsScoutXmlIds;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;

/**
 * <h3>{@link IScoutHelpContextIds}</h3>
 *
 * @author Matthias Villiger
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
