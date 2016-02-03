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

import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;

/**
 * <h3>{@link IScoutHelpContextIds}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public interface IScoutHelpContextIds {

  String PREFIX = S2ESdkUiActivator.PLUGIN_ID + '.';

  String SCOUT_PROJECT_NEW_WIZARD_PAGE = PREFIX + "scout_wizard_new_project_page_context";
  String SCOUT_FORM_NEW_WIZARD_PAGE = PREFIX + "scout_wizard_form_page_context";
  String SCOUT_PAGE_NEW_WIZARD_PAGE = PREFIX + "scout_wizard_page_page_context";
  String SCOUT_ENTRY_WIZARD_PAGE = PREFIX + "scout_wizard_nls-entry_page_context";
  String SCOUT_LOOKUPCALL_NEW_WIZARD_PAGE = PREFIX + "scout_wizard_lookupcall_page_context";
  String SCOUT_CODETYPE_NEW_WIZARD_PAGE = PREFIX + "scout_wizard_code-type_page_context";
  String SCOUT_PERMISSION_NEW_WIZARD_PAGE = PREFIX + "scout_wizard_permission_page_context";
  String SCOUT_LANGUAGE_NEW_WIZARD_PAGE = PREFIX + "scout_wizard_language_page_context";
}
