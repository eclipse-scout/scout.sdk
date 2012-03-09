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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <h3>{@link SdkProperties}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 24.10.2008
 */
public final class SdkProperties {

  public static final int UI_STRATEGY_SWT = 1 << 0;
  public static final int UI_STRATEGY_SWING = 1 << 1;

  public static final String PRODUCT_FOLDER = "products";
  public static final String PRODUCT_PRODUCTION_FOLDER = "products/production";
  public static final String PRODUCT_FOLDER_DEVELOPMENT = "products/development";

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
  public static final String SUFFIX_TEXT_SERVICE = "TextProviderService";
  public static final String SUFFIX_TABLE_COLUMN = "Column";
  public static final String SUFFIX_TOOL = "Tool";
  public static final String SUFFIX_TREE = "Tree";
  public static final String SUFFIX_WIZARD_FORM = "Wizardform";
  public static final String SUFFIX_WIZARD = "Wizard";
  public static final String SUFFIX_WIZARD_STEP = "Step";
  public static final String SUFFIX_FORM_HANDLER = "Handler";
  public static final String SUFFIX_VIEW_BUTTON = "ViewButton";
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

  public static final String PREFIX_INITIAL_MENU_SUPER_TYPE = "Abstract";
  public static final String PREFIX_INITIAL_FIELD_SUPER_TYPE = "Abstract";
  public static final String NUMBER_MAX = "inf";
  public static final String NUMBER_MIN = "-inf";
  public static final String NULL = "None";
  public static final String INPUT_MULTI_UNDEFINED = "###";

  private static final String PROJECT_PROD_LAUNCHERS = "pref_scout_project_prod_launcher";

  private static final Pattern REGEX_METHOD_PRESENTER_NAME = Pattern.compile("([A-Z])");

  private SdkProperties() {
  }

  public static void saveProjectProductLaunchers(String projectName, IFile[] productFiles) {
    StringBuilder mementoString = new StringBuilder();
    for (int i = 0; i < productFiles.length; i++) {
      mementoString.append(productFiles[i].getFullPath());
      if (i < productFiles.length - 1) {
        mementoString.append(",");
      }
    }
    IEclipsePreferences node = new InstanceScope().getNode(ScoutSdk.getDefault().getBundle().getSymbolicName());
    node.put(PROJECT_PROD_LAUNCHERS + "_" + projectName, mementoString.toString());
    try {
      node.flush();
    }
    catch (BackingStoreException e) {
      ScoutSdk.logError("unable to persist project product launcher settings.", e);
    }
  }

  public static void addProjectProductLauncher(String projectName, IFile productFile) {
    IFile[] existingLaunchers = getProjectProductLaunchers(projectName);
    IPath path = productFile.getFullPath();
    for (IFile existing : existingLaunchers) {
      if (existing.getFullPath().equals(path)) {
        return; /* this entry already exists */
      }
    }

    IFile[] newProdFiles = new IFile[existingLaunchers.length + 1];
    System.arraycopy(existingLaunchers, 0, newProdFiles, 0, existingLaunchers.length);
    newProdFiles[existingLaunchers.length] = productFile;
    saveProjectProductLaunchers(projectName, newProdFiles);
  }

  public static IFile[] getProjectProductLaunchers(String projectName) {
    ArrayList<IFile> products = new ArrayList<IFile>();
    IEclipsePreferences node = new InstanceScope().getNode(ScoutSdk.getDefault().getBundle().getSymbolicName());
    String mementoProducts = node.get(PROJECT_PROD_LAUNCHERS + "_" + projectName, "");
    if (!StringUtility.isNullOrEmpty(mementoProducts)) {
      String[] productLocations = mementoProducts.split(",\\s*");
      if (productLocations != null && productLocations.length > 0) {
        for (String productPath : productLocations) {
          if (!StringUtility.isNullOrEmpty(productPath)) {
            IFile productFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(productPath));
            if (productFile != null && productFile.exists()) {
              products.add(productFile);
            }
          }
        }
      }
    }
    return products.toArray(new IFile[products.size()]);
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
