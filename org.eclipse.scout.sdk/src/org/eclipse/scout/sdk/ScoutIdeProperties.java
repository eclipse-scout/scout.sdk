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
package org.eclipse.scout.sdk;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.sdk.util.Regex;

/**
 * <h3>{@link ScoutIdeProperties}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 24.10.2008
 */
public class ScoutIdeProperties {

  public static final int UI_STRATEGY_SWT = 1 << 0;
  public static final int UI_STRATEGY_SWING = 1 << 1;
  public static final int UI_STRATEGY_DEMO = 1 << 2;

  public static final String PRODUCT_FOLDER = "products";
  public static final String PRODUCT_PRODUCTION_FOLDER = "products/production";
  public static final String PRODUCT_FOLDER_DEVELOPMENT = "products/development";
  public static final String PRODUCT_FOLDER_TEST = "products/test";

  // public static final String SCOUT_BUNDLE_TYPE_CLIENT = "client";

  public static final String DEFAULT_SOURCE_FOLDER_NAME = "src";

  // XXX move client/shared/server to the new project wizard
  public static final int BUNDLE_TYPE_CLIENT_APPLICATION = 1 << 0;
  public static final int BUNDLE_TYPE_CLIENT = 1 << 1;
  public static final int BUNDLE_TYPE_SHARED = 1 << 2;
  public static final int BUNDLE_TYPE_SERVER = 1 << 3;
  public static final int BUNDLE_TYPE_SERVER_APPLICATION = 1 << 4;
  public static final int BUNDLE_TYPE_UI_SWT = 1 << 5;
  public static final int BUNDLE_TYPE_UI_SWT_APPLICATION = 1 << 6;

  public static final int BUNDLE_TYPE_UI_SWING = 1 << 7;
  public static final int BUNDLE_TYPE_TEST_CLIENT = 1 << 8;

  public static final int TOOL_BUTTON_SIZE = 22;

  public static final String TAB = "  ";
  public static final String ICON_PATH = "resources/icons/";

  public static final String TEXT_AUTHORIZATION_FAILED = "AuthorizationFailed";

  public static final String SUFFIX_BOX = "Box";
  public static final String SUFFIX_BOOKMARK_STORAGE_SERVICE = "BookmarkStorageService";

  public static final String SUFFIX_ID = "Nr";
  public static final String SUFFIX_BUTTON = "Button";
  public static final String SUFFIX_CALENDAR_ITEM = "Item";
  public static final String SUFFIX_CALENDAR_ITEM_PROVIDER = "ItemProvider";
  public static final String SUFFIX_CODE = "Code";
  public static final String SUFFIX_CODE_TYPE = "CodeType";
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
  public static final String SUFFIX_CUSTOM_SERVICE = "CustomService";
  public static final String SUFFIX_PROCESS_SERVICE = "ProcessService";
  public static final String SUFFIX_OUTLINE_SERVICE = "OutlineService";
  public static final String SUFFIX_CALENDAR_SERVICE = "CalendarService";
  public static final String SUFFIX_SMTP_SERVICE = "SmtpService";
  public static final String SUFFIX_SQL_SERVICE = "SqlService";
  public static final String SUFFIX_TABLE_COLUMN = "Column";
  public static final String SUFFIX_TOOL = "Tool";
  public static final String SUFFIX_TREE = "Tree";
  public static final String SUFFIX_WIZARD_FORM = "Wizardform";
  public static final String SUFFIX_WIZARD = "Wizard";
  public static final String SUFFIX_WIZARD_STEP = "Step";
  private static final String SUFFIX_WORKFLOW_TABLE_PAGE = "WorkflowPage";
  public static final String SUFFIX_FORM_HANDLER = "Handler";
  // non field suffixes
  public static final String SUFFIX_FROM = "From";
  public static final String SUFFIX_TO = "To";

  // report data service suffixes
  public static final String SUFFIX_COLUMN = "Column";
  public static final String SUFFIX_PARAMETER = "Parameter";

  public static final String TYPE_NAME_CLIENT_SESSION = "ClientSession";
  public static final String TYPE_NAME_SERVER_SESSION = "ServerSession";
  public static final String TYPE_NAME_DESKTOP = "Desktop";
  public static final String TYPE_NAME_MAIN_BOX = "MainBox";
  public static final String TYPE_NAME_TAB_BOX = "TabBox";
  public static final String TYPE_NAME_MODIFY_HANDLER = "Modify" + SUFFIX_FORM_HANDLER;
  public static final String TYPE_NAME_NEW_HANDLER = "New" + SUFFIX_FORM_HANDLER;
  public static final String TYPE_NAME_SEARCH_HANDLER = "Search" + SUFFIX_FORM_HANDLER;

  public static final String TYPE_NAME_CALENDARFIELD_CALENDAR = "Calendar";
  public static final String TYPE_NAME_HTTP_PROXY_HANDER_SERVLET = "HttpProxyHandlerServlet";
  public static final String TYPE_NAME_SERVICES = "SERVICES";
  public static final String TYPE_NAME_TABLEFIELD_TABLE = "Table";
  public static final String TYPE_NAME_TREEBOX_TREE = "Tree";
  public static final String TYPE_NAME_TREEFIELD_TREE = "Tree";
  public static final String TYPE_NAME_PLANNERFIELD_TABLE = "ResourceTable";
  public static final String TYPE_NAME_PLANNERFIELD_ACTIVITYMAP = "ActivityMap";
  public static final String TYPE_NAME_CHART_DATA_FIELD = "ChartData";
  public static final String TYPE_NAME_OUTLINE_WITH_TABLE_TABLE = "Table";

  public static final String TYPE_NAME_BUTTON_WIZARD_BACK = "BackButton";
  public static final String TYPE_NAME_BUTTON_WIZARD_NEXT = "NextButton";
  public static final String TYPE_NAME_BUTTON_WIZARD_FINISH = "FinishButton";
  public static final String TYPE_NAME_BUTTON_WIZARD_CANCEL = "CancelButton";

  private static HashMap<String, String> suffixMapping = new HashMap<String, String>();
  static {
    suffixMapping.put(RuntimeClasses.AbstractCalendarItem, SUFFIX_CALENDAR_ITEM);
    suffixMapping.put(RuntimeClasses.AbstractCodeType, SUFFIX_CODE_TYPE);
    suffixMapping.put(RuntimeClasses.AbstractBooleanField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractButton, SUFFIX_BUTTON);
    suffixMapping.put(RuntimeClasses.AbstractCancelButton, SUFFIX_BUTTON);
    suffixMapping.put(RuntimeClasses.AbstractCloseButton, SUFFIX_BUTTON);
    suffixMapping.put(RuntimeClasses.AbstractOkButton, SUFFIX_BUTTON);
    suffixMapping.put(RuntimeClasses.AbstractResetButton, SUFFIX_BUTTON);
    suffixMapping.put(RuntimeClasses.AbstractSaveButton, SUFFIX_BUTTON);
    suffixMapping.put(RuntimeClasses.AbstractSearchButton, SUFFIX_BUTTON);
    suffixMapping.put(RuntimeClasses.AbstractCalendarField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractChartBox, SUFFIX_BOX);
    suffixMapping.put(RuntimeClasses.AbstractCheckBox, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractComposerField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractCustomField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractDateField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractDoubleField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractFileChooserField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractForm, SUFFIX_FORM);
    suffixMapping.put(RuntimeClasses.AbstractGroupBox, SUFFIX_BOX);
    suffixMapping.put(RuntimeClasses.AbstractHtmlField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractImageField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractLabelField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractListBox, SUFFIX_BOX);
    suffixMapping.put(RuntimeClasses.AbstractLongField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractMatrixField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractPageWithNodes, SUFFIX_OUTLINE_NODE_PAGE);
    suffixMapping.put(RuntimeClasses.AbstractPageWithTable, SUFFIX_OUTLINE_TABLE_PAGE);
    suffixMapping.put(RuntimeClasses.AbstractPlannerField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractRadioButtonGroup, SUFFIX_BUTTON_GROUP);
    suffixMapping.put(RuntimeClasses.AbstractSequenceBox, SUFFIX_BOX);
    suffixMapping.put(RuntimeClasses.AbstractSmartField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractStringField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractTabBox, SUFFIX_BOX);
    suffixMapping.put(RuntimeClasses.AbstractTableField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractTimeField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.AbstractTreeBox, SUFFIX_BOX);
    suffixMapping.put(RuntimeClasses.AbstractTreeField, SUFFIX_FORM_FIELD);
    suffixMapping.put(RuntimeClasses.ExampleWorkflowTablePage, SUFFIX_WORKFLOW_TABLE_PAGE);
  }

  public static final String PREFIX_INITIAL_MENU_SUPER_TYPE = "Abstract";
  public static final String PREFIX_INITIAL_FIELD_SUPER_TYPE = "Abstract";
  public static final String NUMBER_MAX = "inf";
  public static final String NUMBER_MIN = "-inf";
  public static final String NULL = "None";
  public static final String INPUT_MULTI_UNDEFINED = "###";

  private static Pattern propertyMethodTrim = Pattern.compile(Regex.REGEX_PROPERTY_METHOD_TRIM);

  private ScoutIdeProperties() {
  }

  // public static String getClientApplicationSuffix() {
  // return "client.app.core";
  // }
  // public static String getClientSuffix(){
  // return "client.core";
  // }
  // public static String getSharedSuffix(){
  // return "shared.core";
  // }
  // public static String getServerSuffix(){
  // return "server.core";
  // }

  //
  // public static String getManifestGroupId(){
  // return "BsiCase-ProjectGroupId";
  // }
  //
  // public static String getManifestAlias(){
  // return "BsiCase-Alias";
  // }
  //
  // public static String getManifestIdenifier(){
  // return "BsiCase-BundleType";
  // }
  //
  // public static String getScoutPluginPrefix(){
  // return "org.eclipse.scout.rt";
  // }

  public static String getMappedSuffix(String fullyQualifiedBcType) {
    return suffixMapping.get(fullyQualifiedBcType);
  }

  public static String getMethodPresenterName(IMethod method) {
    String name = method.getElementName();
    Matcher m = propertyMethodTrim.matcher(name);
    if (m.find()) {
      name = m.group(1);
    }
    name = name.replaceAll("([A-Z])", " $1").trim();
    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
    return name;
  }

}
