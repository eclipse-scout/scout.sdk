/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s;

import java.util.regex.Pattern;

/**
 * Defaults used for scout objects
 */
public interface ISdkConstants {
  String SUFFIX_FORM_FIELD = "Field";
  String SUFFIX_LOOKUP_CALL = "LookupCall";
  String SUFFIX_FORM = "Form";
  String SUFFIX_PAGE_WITH_TABLE = "TablePage";
  String SUFFIX_PAGE_WITH_NODES = "NodePage";
  String SUFFIX_OUTLINE_PAGE = "Page";
  String SUFFIX_DTO = "Data";
  String SUFFIX_DTO_PROPERTY = "Property";
  String SUFFIX_BUTTON = "Button";
  String SUFFIX_FORM_HANDLER = "Handler";
  String SUFFIX_COMPOSITE_FIELD = "Box";
  String SUFFIX_MENU = "Menu";
  String SUFFIX_KEY_STROKE = "KeyStroke";
  String SUFFIX_CODE = "Code";
  String SUFFIX_COLUMN = "Column";
  String SUFFIX_EXTENSION = "Extension";
  String SUFFIX_CALENDAR_ITEM_PROVIDER = "Provider";
  String SUFFIX_SERVICE = "Service";
  String SUFFIX_PERMISSION = "Permission";
  String SUFFIX_CODE_TYPE = "CodeType";
  String SUFFIX_TEST = "Test";
  String SUFFIX_WS_CLIENT = "WebServiceClient";
  String SUFFIX_WS_PROVIDER = "WebService";
  String SUFFIX_WS_URL_PROPERTY = "WebServiceUrlProperty";
  String SUFFIX_WS_PORT_TYPE = "PortType";
  String SUFFIX_WS_SERVICE = SUFFIX_SERVICE;
  String SUFFIX_WS_ENTRY_POINT = "WebServiceEntryPoint";
  String SUFFIX_WS_ENTRY_POINT_DEFINITION = SUFFIX_WS_ENTRY_POINT + "Definition";
  String SUFFIX_TEXT_PROVIDER_SERVICE = "TextProviderService";
  String SUFFIX_PERMISSION_TEXT_PROVIDER_SERVICE = "PermissionDescriptionTextProviderService";
  String SUFFIX_ROW_DATA = "RowData";
  String SUFFIX_TABLE_ROW_DATA = "Table" + SUFFIX_ROW_DATA;
  String SUFFIX_DO = "Do";

  String PREFIX_ABSTRACT = "Abstract";
  String PREFIX_CREATE_PERMISSION = "Create";
  String PREFIX_READ_PERMISSION = "Read";
  String PREFIX_UPDATE_PERMISSION = "Update";
  String PREFIX_DELETE_PERMISSION = "Delete";

  String INNER_TABLE_TYPE_NAME = "Table";
  String INNER_TREE_TYPE_NAME = "Tree";
  String INNER_CALENDAR_TYPE_NAME = "Calendar";

  /**
   * The default view order.
   * <p>
   * Must match the default order number used in the Scout runtime. See
   * org.eclipse.scout.rt.platform.IOrdered.DEFAULT_ORDER
   */
  double DEFAULT_VIEW_ORDER = 98_765_432_123_456_789.0;

  /**
   * The default bean order if no @Order annotation is present (according to the Scout Runtime)
   */
  double DEFAULT_BEAN_ORDER = 5000.0;

  /**
   * Value to add to view orders for new elements.
   */
  int VIEW_ORDER_ANNOTATION_VALUE_STEP = 1000;

  /**
   * A regular expression matching a single dot.
   */
  Pattern REGEX_DOT = Pattern.compile("\\.");
}
