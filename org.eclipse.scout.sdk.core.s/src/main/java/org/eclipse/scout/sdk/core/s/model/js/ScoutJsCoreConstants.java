/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("StaticCollection")
public final class ScoutJsCoreConstants {

  /**
   * Scout JS Chart module name including namespace
   */
  public static final String SCOUT_JS_CHART_MODULE_NAME = "@eclipse-scout/chart";

  /**
   * Scout JS Core module name including namespace
   */
  public static final String SCOUT_JS_CORE_MODULE_NAME = "@eclipse-scout/core";

  public static final String JQUERY = "JQuery";
  public static final String NAMESPACE = "scout";

  public static final String PROPERTY_NAME_OBJECT_TYPE = "objectType";
  public static final String PROPERTY_NAME_ID = "id";
  public static final String PROPERTY_NAME_MODEL = "model";
  public static final String PROPERTY_NAME_EVENT_MAP = "eventMap";
  public static final String PROPERTY_NAME_SELF = "self";
  public static final String PROPERTY_NAME_WIDGET_MAP = "widgetMap";

  public static final String FUNCTION_NAME_INIT = "_init";
  public static final String FUNCTION_NAME_RESOLVE_TEXT_KEYS = "resolveTextKeys";
  public static final String FUNCTION_NAME_RESOLVE_TEXT_PROPERTY = "resolveTextProperty";
  public static final String FUNCTION_NAME_ADD_PRESERVE_ON_PROPERTY_CHANGE_PROPERTIES = "_addPreserveOnPropertyChangeProperties";
  public static final String FUNCTION_NAME_ADD_WIDGET_PROPERTIES = "_addWidgetProperties";

  public static final String CLASS_NAME_WIDGET = "Widget";
  public static final String CLASS_NAME_GROUP_BOX = "GroupBox";
  public static final String CLASS_NAME_TAG_FIELD = "TagField";
  public static final String CLASS_NAME_SMART_FIELD = "SmartField";
  public static final String CLASS_NAME_TABLE = "Table";
  public static final String CLASS_NAME_TREE = "Tree";
  public static final String CLASS_NAME_TILE_GRID = "TileGrid";
  public static final String CLASS_NAME_BUTTON = "Button";
  public static final String CLASS_NAME_DATE_FIELD = "DateField";
  public static final String CLASS_NAME_MENU = "Menu";
  public static final String CLASS_NAME_VIEW_MENU_TAB = "ViewMenuTab";
  public static final String CLASS_NAME_MODEL_ADAPTER = "ModelAdapter";
  public static final String CLASS_NAME_STATUS_OR_MODEL = "StatusOrModel";
  public static final String CLASS_NAME_STATUS = "Status";
  public static final String CLASS_NAME_LOOKUP_CALL = "LookupCall";
  public static final String CLASS_NAME_LOOKUP_CALL_OR_MODEL = "LookupCallOrModel";
  public static final String CLASS_NAME_ENUM_OBJECT = "EnumObject";

  public static final Set<String> CLASS_NAMES_MODEL_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("ObjectOrChildModel", "ChildModelOf", "ObjectOrModel", "FullModelOf", "ModelOf", "InitModelOf")));

  private static final Map<String /* class-name */, Set<String /* property-name */>> EXCLUDED_PROPERTIES = new HashMap<>();
  static {
    EXCLUDED_PROPERTIES.put(CLASS_NAME_WIDGET, Set.of("enabledComputed", "events", "attached", "children", "cloneOf", "destroyed", "destroying",
        "eventDelegators", "htmlComp", "initialized", "removalPending", "removing", "rendered", "rendering", "owner"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_GROUP_BOX, Set.of("controls", "processButtons", "processMenus", "systemButtons", "customButtons"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_TAG_FIELD, Set.of("fieldHtmlComp", "popup"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_SMART_FIELD, Set.of("lookupSeqNo", "popup"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_TABLE, Set.of("rootRows", "rowBorderLeftWidth", "rowBorderRightWidth", "rowBorderWidth", "rowHeight", "rowsMap", "rowWidth",
        "visibleRows", "visibleRowsMap", "columnLayoutDirty", "viewRangeDirty", "viewRangeRendered", "contextMenu"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_TREE, Set.of("groupedNodes", "runningAnimationsFinishFunc", "startAnimationFunc", "visibleNodesFlat", "visibleNodesMap",
        "maxNodeWidth", "nodeHeight", "nodesMap", "nodeWidth", "nodeWidthDirty", "viewRangeDirty", "viewRangeRendered", "contextMenu"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_BUTTON, Set.of("popup"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_TILE_GRID, Set.of("tileRemovalPendingCount", "filteredTiles", "filteredTilesDirty", "viewRangeRendered", "contextMenu"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_DATE_FIELD, Set.of("popup"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_MENU, Set.of("popup"));
    EXCLUDED_PROPERTIES.put(CLASS_NAME_VIEW_MENU_TAB, Set.of("popup"));
  }

  private ScoutJsCoreConstants() {
  }

  public static Set<String> getExcludedProperties(String className) {
    var exclusionsForClass = EXCLUDED_PROPERTIES.get(className);
    if (exclusionsForClass == null) {
      return Collections.emptySet();
    }
    return exclusionsForClass;
  }

  public static boolean isExcludedPropertyName(String className, String propertyName) {
    var exclusionsForClass = getExcludedProperties(className);
    return exclusionsForClass.contains(propertyName);
  }
}
