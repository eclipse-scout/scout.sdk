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

  public static final String PRODUCT_FOLDER = "products";
  public static final String PRODUCT_PRODUCTION_FOLDER = "products/production";
  public static final String PRODUCT_FOLDER_DEVELOPMENT = "products/development";

  public static final String DEFAULT_SOURCE_FOLDER_NAME = "src";

  public static final int TOOL_BUTTON_SIZE = 22;

  public static final String TAB = "  ";

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
  public static final String SUFFIX_ACCESS_CONTROL_SERVICE = "AccessControlService";
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

  public static final String TYPE_NAME_MAIN_BOX = "MainBox";
  public static final String TYPE_NAME_MODIFY_HANDLER_PREFIX = "Modify";
  public static final String TYPE_NAME_MODIFY_HANDLER = TYPE_NAME_MODIFY_HANDLER_PREFIX + SUFFIX_FORM_HANDLER;
  public static final String TYPE_NAME_NEW_HANDLER_PREFIX = "New";
  public static final String TYPE_NAME_NEW_HANDLER = TYPE_NAME_NEW_HANDLER_PREFIX + SUFFIX_FORM_HANDLER;
  public static final String TYPE_NAME_SEARCH_HANDLER_PREFIX = "Search";
  public static final String TYPE_NAME_SEARCH_HANDLER = TYPE_NAME_SEARCH_HANDLER_PREFIX + SUFFIX_FORM_HANDLER;

  public static final String TYPE_NAME_CALENDARFIELD_CALENDAR = "Calendar";
  public static final String TYPE_NAME_HTTP_PROXY_HANDER_SERVLET = "HttpProxyHandlerServlet";
  public static final String TYPE_NAME_TABLEFIELD_TABLE = "Table";
  public static final String TYPE_NAME_TREEBOX_TREE = "Tree";
  public static final String TYPE_NAME_PLANNERFIELD_TABLE = "ResourceTable";
  public static final String TYPE_NAME_PLANNERFIELD_ACTIVITYMAP = "ActivityMap";
  public static final String TYPE_NAME_OUTLINE_WITH_TABLE_TABLE = "Table";

  public static final String NUMBER_MAX = "inf";
  public static final String NUMBER_MIN = "-inf";
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
    IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ScoutSdk.getDefault().getBundle().getSymbolicName());
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
    IEclipsePreferences node = InstanceScope.INSTANCE.getNode(ScoutSdk.getDefault().getBundle().getSymbolicName());
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
