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
package org.eclipse.scout.sdk.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;

/**
 * <h3>{@link SdkProperties}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 24.10.2008
 */
public final class SdkProperties {

  public static final int TOOL_BUTTON_SIZE = 22;
  public static final String TAB = "  ";

  public static final String SUFFIX_BOX = "Box";
  public static final String SUFFIX_BOOKMARK_STORAGE_SERVICE = "BookmarkStorageService";
  public static final String SUFFIX_ID = "Nr";
  public static final String SUFFIX_BUTTON = "Button";
  public static final String SUFFIX_CALENDAR_ITEM_PROVIDER = "ItemProvider";
  public static final String SUFFIX_CODE = "Code";
  public static final String SUFFIX_CODE_TYPE = "CodeType";
  public static final String SUFFIX_COLUMN = "Column";
  public static final String SUFFIX_COMPOSER_ATTRIBUTE = "Attribute";
  public static final String SUFFIX_COMPOSER_ENTRY = "Entry";
  public static final String SUFFIX_FORM = "Form";
  public static final String SUFFIX_FORM_DATA = "FormData";
  public static final String SUFFIX_SEARCH_FORM = "SearchForm";
  public static final String SUFFIX_SEARCH_FORM_DATA = "SearchFormData";
  public static final String SUFFIX_FORM_FIELD = "Field";
  public static final String SUFFIX_BUTTON_GROUP = "Group";
  public static final String SUFFIX_GROUP_BOX = "Box";
  public static final String SUFFIX_KEY_STROKE = "KeyStroke";
  public static final String SUFFIX_LOOKUP_SERVICE = "LookupService";
  public static final String SUFFIX_LOOKUP_CALL = "LookupCall";
  public static final String SUFFIX_MENU = "Menu";
  public static final String SUFFIX_OUTLINE = "Outline";
  public static final String SUFFIX_OUTLINE_TABLE_PAGE = "TablePage";
  public static final String SUFFIX_OUTLINE_NODE_PAGE = "NodePage";
  public static final String SUFFIX_OUTLINE_PAGE = "Page";
  public static final String SUFFIX_PERMISSION = "Permission";
  public static final String SUFFIX_SERVICE = "Service";
  public static final String SUFFIX_ACCESS_CONTROL_SERVICE = "AccessControlService";
  public static final String SUFFIX_CALENDAR_SERVICE = "CalendarService";
  public static final String SUFFIX_SMTP_SERVICE = "SmtpService";
  public static final String SUFFIX_SQL_SERVICE = "SqlService";
  public static final String SUFFIX_TEXT_SERVICE = "TextProviderService";
  public static final String SUFFIX_TABLE_COLUMN = "Column";
  public static final String SUFFIX_TOOL = "Tool";
  public static final String SUFFIX_WIZARD = "Wizard";
  public static final String SUFFIX_WIZARD_STEP = "Step";
  public static final String SUFFIX_FORM_HANDLER = "Handler";
  public static final String SUFFIX_VIEW_BUTTON = "ViewButton";

  // non field suffixes
  public static final String SUFFIX_FROM = "From";
  public static final String SUFFIX_TO = "To";

  public static final String TYPE_NAME_MAIN_BOX = "MainBox";
  public static final String TYPE_NAME_MODIFY_HANDLER_PREFIX = "Modify";
  public static final String TYPE_NAME_MODIFY_HANDLER = TYPE_NAME_MODIFY_HANDLER_PREFIX + SUFFIX_FORM_HANDLER;
  public static final String TYPE_NAME_NEW_HANDLER_PREFIX = "New";
  public static final String TYPE_NAME_NEW_HANDLER = TYPE_NAME_NEW_HANDLER_PREFIX + SUFFIX_FORM_HANDLER;
  public static final String TYPE_NAME_SEARCH_HANDLER_PREFIX = "Search";
  public static final String TYPE_NAME_SEARCH_HANDLER = TYPE_NAME_SEARCH_HANDLER_PREFIX + SUFFIX_FORM_HANDLER;
  public static final String TYPE_NAME_VIEW_HANDLER_PREFIX = "View";
  public static final String TYPE_NAME_VIEW_HANDLER = TYPE_NAME_VIEW_HANDLER_PREFIX + SUFFIX_FORM_HANDLER;
  public static final String TYPE_NAME_OK_BUTTON = "OkButton";
  public static final String TYPE_NAME_CANCEL_BUTTON = "CancelButton";

  public static final String TYPE_NAME_CALENDARFIELD_CALENDAR = "Calendar";
  public static final String TYPE_NAME_TABLEFIELD_TABLE = "Table";
  public static final String TYPE_NAME_TREEBOX_TREE = "Tree";
  public static final String TYPE_NAME_PLANNERFIELD_TABLE = "ResourceTable";
  public static final String TYPE_NAME_PLANNERFIELD_ACTIVITYMAP = "ActivityMap";
  public static final String TYPE_NAME_OUTLINE_WITH_TABLE_TABLE = "Table";

  public static final String METHOD_NAME_GET_CONFIGURED_LABEL = "getConfiguredLabel";
  public static final String METHOD_NAME_GET_CONFIGURED_TEXT = "getConfiguredText";
  public static final String METHOD_NAME_GET_CONFIGURED_TITLE = "getConfiguredTitle";
  public static final String METHOD_NAME_GET_CONFIGURED_HEADER_TEXT = "getConfiguredHeaderText";

  public static final String NUMBER_MAX = "inf";
  public static final String NUMBER_MIN = "-inf";
  public static final String INPUT_MULTI_UNDEFINED = "###";

  private static final Pattern REGEX_METHOD_PRESENTER_NAME = Pattern.compile("([A-Z])");

  private SdkProperties() {
  }

  public static String getMethodPresenterName(IMethod method) {
    String name = method.getElementName();
    Matcher m = Regex.REGEX_PROPERTY_METHOD_TRIM.matcher(name);
    if (m.find()) {
      name = m.group(1);
    }
    name = REGEX_METHOD_PRESENTER_NAME.matcher(name).replaceAll(" $1").trim();
    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
    return name;
  }
}
