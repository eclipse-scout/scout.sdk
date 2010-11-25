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
///*******************************************************************************
// * Copyright (c) 2010 BSI Business Systems Integration AG.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     BSI Business Systems Integration AG - initial API and implementation
// ******************************************************************************/
//package org.eclipse.scout.sdk.ui.internal.extensions;
//
//import java.security.BasicPermission;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import org.eclipse.scout.sdk.RuntimeClasses;
//import org.eclipse.scout.sdk.ui.ScoutSdkUi;
//
///**
// * <h3>BCTypeNameUtility</h3> ...
// */
//public class ScoutTypeDecorators {
//
//  public static final String PHANTOM_CLASS_NAME_RANGE_BOX_DOUBLE = RuntimeClasses.AbstractSequenceBox + "#double";
//  public static final String PHANTOM_CLASS_NAME_RANGE_BOX_BIG_DECIMAL = RuntimeClasses.AbstractSequenceBox + "#bigDecimal";
//  public static final String PHANTOM_CLASS_NAME_RANGE_BOX_DATE = RuntimeClasses.AbstractSequenceBox + "#date";
//  public static final String PHANTOM_CLASS_NAME_RANGE_BOX_INTEGER = RuntimeClasses.AbstractSequenceBox + "#integer";
//  public static final String PHANTOM_CLASS_NAME_RANGE_BOX_LONG = RuntimeClasses.AbstractSequenceBox + "#long";
//  public static final String PHANTOM_CLASS_NAME_RANGE_BOX_TIME = RuntimeClasses.AbstractSequenceBox + "#time";
//  public static final String PHANTOM_CLASS_NAME_RANGE_BOX_DATE_TIME = RuntimeClasses.AbstractSequenceBox + "#dateTime";
//  public static final String PHANTOM_CLASS_NAME_DATE_TIME_FIELD = RuntimeClasses.AbstractDateField + "#dateTime";
//  public static final String PHANTOM_CLASS_NAME_MAIN_BOX = RuntimeClasses.AbstractGroupBox + "#main";
//
//  public static final int INDEX_BOOLEAN_FIELD = 1;
//  public static final int INDEX_BUTTON = 2;
//  public static final int INDEX_BACK_BUTTON = 3;
//  public static final int INDEX_CANCEL_BUTTON = 4;
//  public static final int INDEX_CLOSE_BUTTON = 5;
//  public static final int INDEX_FINISH_BUTTON = 6;
//  public static final int INDEX_NEXT_BUTTON = 7;
//  public static final int INDEX_OK_BUTTON = 8;
//  public static final int INDEX_RESET_BUTTON = 9;
//  public static final int INDEX_SAVE_BUTTON = 10;
//  public static final int INDEX_SEARCH_BUTTON = 11;
//  public static final int INDEX_CALENDAR_FIELD = 12;
//  public static final int INDEX_CHART_BOX = 13;
//  public static final int INDEX_CHECK_BOX = 14;
//  public static final int INDEX_COMPOSER_FIELD = 15;
//  public static final int INDEX_CUSTOM_FIELD = 16;
//  public static final int INDEX_DATE_FIELD = 17;
//  public static final int INDEX_DOUBLE_FIELD = 18;
//  public static final int INDEX_BIG_DECIMAL_FIELD = 122;
//  public static final int INDEX_FILE_CHOOSER_FIELD = 19;
//  public static final int INDEX_FORM = 20;
//  public static final int INDEX_GROUP_BOX = 21;
//  public static final int INDEX_HTML_FIELD = 22;
//  public static final int INDEX_IMAGE_FIELD = 23;
//  public static final int INDEX_INTEGER_FIELD = 27;
//  public static final int INDEX_LABEL_FIELD = 24;
//  public static final int INDEX_LIST_BOX = 25;
//  public static final int INDEX_LONG_FIELD = 26;
//  public static final int INDEX_MATRIX_FIELD = 28;
//  public static final int INDEX_PLANNER_FIELD = 29;
//  public static final int INDEX_RADIO_BUTTON_GROUP = 30;
//  public static final int INDEX_SEQUENCE_BOX = 31;
//  public static final int INDEX_SMART_FIELD = 32;
//  public static final int INDEX_STRING_FIELD = 33;
//  public static final int INDEX_TAB_BOX = 34;
//  public static final int INDEX_TABLE_FIELD = 35;
//  public static final int INDEX_TIME_FIELD = 36;
//  public static final int INDEX_TREE_BOX = 37;
//  public static final int INDEX_TREE_FIELD = 38;
//  // NOT REAL TYPES
//  public static final int INDEX_SEQUENCE_BOX_DOUBLE = 39;
//  public static final int INDEX_SEQUENCE_BOX_DATE = 40;
//  public static final int INDEX_SEQUENCE_BOX_LONG = 41;
//  public static final int INDEX_RANGE_BOX_TIME = 42;
//  public static final int INDEX_SEQUENCE_BOX_DATE_TIME = 43;
//  public static final int INDEX_DATE_TIME_FIELD = 44;
//  public static final int INDEX_SEQUENCE_BOX_INTEGER = 45;
//  public static final int INDEX_SEQUENCE_BOX_BIG_DECIMAL = 123;
//
//  public static final int INDEX_OUTLINE = 46;
//  public static final int INDEX_PAGE_WITH_TABLE = 47;
//  public static final int INDEX_PAGE_WITH_NODES = 48;
//  public static final int INDEX_WORKFLOW_PAGE = 49;
//  public static final int INDEX_MENU = 50;
//  public static final int INDEX_MENU_BOOKMARK = 51;
//  public static final int INDEX_MENU_CHECKBOX = 52;
//  public static final int INDEX_COLUMN_STRING = 53;
//  public static final int INDEX_COLUMN_LONG = 54;
//  public static final int INDEX_COLUMN_INTEGER = 55;
//  public static final int INDEX_COLUMN_DOUBLE = 56;
//  public static final int INDEX_COLUMN_DATE = 57;
//  public static final int INDEX_COLUMN_TIME = 58;
//  public static final int INDEX_MAIN_BOX = 59;
//  public static final int INDEX_COLUMN_BIG_DECIMAL = 60;
//  public static final int INDEX_COLUMN_SMART = 61;
//
//  public static final int INDEX_CALENDAR_ITEM = 100;
//  public static final int INDEX_CALENDAR = 101;
//  public static final int INDEX_CODE = 102;
//  public static final int INDEX_CODE_TYPE = 103;
//  public static final int INDEX_COLUMN = 104;
//  public static final int INDEX_DESKTOP = 105;
//  public static final int INDEX_KEY_STROKE = 106;
//  public static final int INDEX_PERMISSION = 107;
//  public static final int INDEX_SERVICE = 108;
//  public static final int INDEX_LOOKUP_SERVICE = 109;
//  public static final int INDEX_TABLE = 111;
//  public static final int INDEX_TOOL = 112;
//  public static final int INDEX_TREE = 113;
//
//  public static final int INDEX_WIZARD = 116;
//  public static final int INDEX_WIZARD_STEP = 117;
//  public static final int INDEX_WORKFLOW = 118;
//  public static final int INDEX_WORKFLOW_STEP = 119;
//
//  public static final int INDEX_LOOKUP_CALL = 121;
//
//  // next number is 124
//
//  private static ScoutTypeDecorators instance = new ScoutTypeDecorators();
//  private HashMap<String, ScoutTypeDecorator> m_qualifiedNameCache;
//  private HashMap<Integer, ScoutTypeDecorator> m_indexedNameCache;
//
//  private ScoutTypeDecorators() {
//    init();
//  }
//
//  private void init() {
//    ArrayList<ScoutTypeDecorator> list = new ArrayList<ScoutTypeDecorator>();
//    list.add(new ScoutTypeDecorator(INDEX_BOOLEAN_FIELD, RuntimeClasses.AbstractBooleanField, "Boolean Field"));
//    list.add(new ScoutTypeDecorator(INDEX_BUTTON, RuntimeClasses.AbstractButton, "Button"));
//    list.add(new ScoutTypeDecorator(INDEX_CANCEL_BUTTON, RuntimeClasses.AbstractCancelButton, "Button Cancel"));
//    list.add(new ScoutTypeDecorator(INDEX_CLOSE_BUTTON, RuntimeClasses.AbstractCloseButton, "Button Close"));
//    list.add(new ScoutTypeDecorator(INDEX_OK_BUTTON, RuntimeClasses.AbstractOkButton, "Button Ok"));
//    list.add(new ScoutTypeDecorator(INDEX_RESET_BUTTON, RuntimeClasses.AbstractResetButton, "Button Reset"));
//    list.add(new ScoutTypeDecorator(INDEX_SAVE_BUTTON, RuntimeClasses.AbstractSaveButton, "Button Save"));
//    list.add(new ScoutTypeDecorator(INDEX_SEARCH_BUTTON, RuntimeClasses.AbstractSearchButton, "Button Search"));
//    list.add(new ScoutTypeDecorator(INDEX_CALENDAR_FIELD, RuntimeClasses.AbstractCalendarField, "Calendar Field"));
//    list.add(new ScoutTypeDecorator(INDEX_CHART_BOX, RuntimeClasses.AbstractChartBox, "Chart Box"));
//    list.add(new ScoutTypeDecorator(INDEX_CHECK_BOX, RuntimeClasses.AbstractCheckBox, "Check Box"));
//    list.add(new ScoutTypeDecorator(INDEX_COMPOSER_FIELD, RuntimeClasses.AbstractComposerField, "Composer Field"));
//    list.add(new ScoutTypeDecorator(INDEX_CUSTOM_FIELD, RuntimeClasses.AbstractCustomField, "Custom Field"));
//    list.add(new ScoutTypeDecorator(INDEX_DATE_FIELD, RuntimeClasses.AbstractDateField, "Date Field"));
//    list.add(new ScoutTypeDecorator(INDEX_DOUBLE_FIELD, RuntimeClasses.AbstractDoubleField, "Double Field"));
//    list.add(new ScoutTypeDecorator(INDEX_BIG_DECIMAL_FIELD, RuntimeClasses.AbstractBigDecimalField, "BigDecimal Field"));
//    list.add(new ScoutTypeDecorator(INDEX_FILE_CHOOSER_FIELD, RuntimeClasses.AbstractFileChooserField, "FileChooser Field"));
//    list.add(new ScoutTypeDecorator(INDEX_GROUP_BOX, RuntimeClasses.AbstractGroupBox, "Group Box"));
//    list.add(new ScoutTypeDecorator(INDEX_HTML_FIELD, RuntimeClasses.AbstractHtmlField, "Html Field"));
//    list.add(new ScoutTypeDecorator(INDEX_IMAGE_FIELD, RuntimeClasses.AbstractImageField, "Image Field"));
//    list.add(new ScoutTypeDecorator(INDEX_INTEGER_FIELD, RuntimeClasses.AbstractIntegerField, "Integer Field"));
//    list.add(new ScoutTypeDecorator(INDEX_LABEL_FIELD, RuntimeClasses.AbstractLabelField, "Label Field"));
//    list.add(new ScoutTypeDecorator(INDEX_LIST_BOX, RuntimeClasses.AbstractListBox, "List Box"));
//    list.add(new ScoutTypeDecorator(INDEX_LONG_FIELD, RuntimeClasses.AbstractLongField, "Long Field"));
//    list.add(new ScoutTypeDecorator(INDEX_MATRIX_FIELD, RuntimeClasses.AbstractMatrixField, "Matrix Field"));
//    list.add(new ScoutTypeDecorator(INDEX_OUTLINE, RuntimeClasses.AbstractOutline, "Outline"));
//    list.add(new ScoutTypeDecorator(INDEX_PAGE_WITH_NODES, RuntimeClasses.AbstractPageWithNodes, "Page with Nodes"));
//    list.add(new ScoutTypeDecorator(INDEX_PAGE_WITH_TABLE, RuntimeClasses.AbstractPageWithTable, "Page with Table"));
//    list.add(new ScoutTypeDecorator(INDEX_WORKFLOW_PAGE, RuntimeClasses.ExampleWorkflowTablePage, "Workflow Page"));
//    list.add(new ScoutTypeDecorator(INDEX_PLANNER_FIELD, RuntimeClasses.AbstractPlannerField, "Planner Field"));
//    list.add(new ScoutTypeDecorator(INDEX_RADIO_BUTTON_GROUP, RuntimeClasses.AbstractRadioButtonGroup, "RadioButton Group"));
//    list.add(new ScoutTypeDecorator(INDEX_SEQUENCE_BOX, RuntimeClasses.AbstractSequenceBox, "Range Box"));
//    list.add(new ScoutTypeDecorator(INDEX_SMART_FIELD, RuntimeClasses.AbstractSmartField, "Smart Field"));
//    list.add(new ScoutTypeDecorator(INDEX_STRING_FIELD, RuntimeClasses.AbstractStringField, "String Field"));
//    list.add(new ScoutTypeDecorator(INDEX_TAB_BOX, RuntimeClasses.AbstractTabBox, "Tab Box"));
//    list.add(new ScoutTypeDecorator(INDEX_TABLE_FIELD, RuntimeClasses.AbstractTableField, "Table Field"));
//    list.add(new ScoutTypeDecorator(INDEX_TIME_FIELD, RuntimeClasses.AbstractTimeField, "Time Field"));
//    list.add(new ScoutTypeDecorator(INDEX_TREE_BOX, RuntimeClasses.AbstractTreeBox, "Tree Box"));
//    list.add(new ScoutTypeDecorator(INDEX_TREE_FIELD, RuntimeClasses.AbstractTreeField, "Tree Field"));
//    list.add(new ScoutTypeDecorator(INDEX_MENU, RuntimeClasses.AbstractMenu, "Menu"));
//    list.add(new ScoutTypeDecorator(INDEX_MENU_BOOKMARK, RuntimeClasses.AbstractBookmarkMenu, "Bookmark menu"));
//    list.add(new ScoutTypeDecorator(INDEX_MENU_CHECKBOX, RuntimeClasses.AbstractCheckBoxMenu, "Checkbox menu"));
//    list.add(new ScoutTypeDecorator(INDEX_COLUMN_STRING, RuntimeClasses.AbstractStringColumn, "String Column"));
//    list.add(new ScoutTypeDecorator(INDEX_COLUMN_SMART, RuntimeClasses.AbstractSmartColumn, "Smart Column"));
//    list.add(new ScoutTypeDecorator(INDEX_COLUMN_LONG, RuntimeClasses.AbstractLongColumn, "Long Column"));
//    list.add(new ScoutTypeDecorator(INDEX_COLUMN_INTEGER, RuntimeClasses.AbstractIntegerColumn, "Integer Column"));
//    list.add(new ScoutTypeDecorator(INDEX_COLUMN_DOUBLE, RuntimeClasses.AbstractDoubleColumn, "Double Column"));
//    list.add(new ScoutTypeDecorator(INDEX_COLUMN_BIG_DECIMAL, RuntimeClasses.AbstractBigDecimalColumn, "BigDecimal Column"));
//    list.add(new ScoutTypeDecorator(INDEX_COLUMN_DATE, RuntimeClasses.AbstractDateColumn, "Date Column"));
//    list.add(new ScoutTypeDecorator(INDEX_COLUMN_TIME, RuntimeClasses.AbstractTimeColumn, "Time Column"));
//    list.add(new ScoutTypeDecorator(INDEX_FORM, RuntimeClasses.AbstractForm, "Form"));
//
//    list.add(new ScoutTypeDecorator(INDEX_CALENDAR_ITEM, RuntimeClasses.AbstractCalendarItem, "Calendar Item"));
//    list.add(new ScoutTypeDecorator(INDEX_CALENDAR, RuntimeClasses.AbstractCalendar, "Calendar"));
//    list.add(new ScoutTypeDecorator(INDEX_CODE, RuntimeClasses.AbstractCode, "Code"));
//    list.add(new ScoutTypeDecorator(INDEX_CODE_TYPE, RuntimeClasses.AbstractCodeType, "Code Type"));
//    list.add(new ScoutTypeDecorator(INDEX_COLUMN, RuntimeClasses.AbstractColumn, "Column"));
//    list.add(new ScoutTypeDecorator(INDEX_DESKTOP, RuntimeClasses.AbstractDesktop, "Desktop"));
//    list.add(new ScoutTypeDecorator(INDEX_KEY_STROKE, RuntimeClasses.AbstractKeyStroke, "Key Stroke"));
//    list.add(new ScoutTypeDecorator(INDEX_PERMISSION, BasicPermission.class, "Permission"));
//    list.add(new ScoutTypeDecorator(INDEX_SERVICE, RuntimeClasses.AbstractService, "Service"));
//    list.add(new ScoutTypeDecorator(INDEX_LOOKUP_SERVICE, RuntimeClasses.AbstractLookupService, "Lookup Service"));
//    list.add(new ScoutTypeDecorator(INDEX_LOOKUP_CALL, RuntimeClasses.LookupCall, "Lookup Call"));
//    list.add(new ScoutTypeDecorator(INDEX_TABLE, RuntimeClasses.AbstractTable, "Table"));
//    list.add(new ScoutTypeDecorator(INDEX_TOOL, RuntimeClasses.AbstractToolButton, "Tool Button"));
//    list.add(new ScoutTypeDecorator(INDEX_TREE, RuntimeClasses.AbstractTree, "Tree"));
//    list.add(new ScoutTypeDecorator(INDEX_WIZARD, RuntimeClasses.AbstractWizard, "Wizard"));
//    list.add(new ScoutTypeDecorator(INDEX_WIZARD_STEP, RuntimeClasses.AbstractWizardStep, "Wizard Step"));
//
//    list.add(new ScoutTypeDecorator(INDEX_SEQUENCE_BOX_DOUBLE, PHANTOM_CLASS_NAME_RANGE_BOX_DOUBLE, "Sequence Box Double"));
//    list.add(new ScoutTypeDecorator(INDEX_SEQUENCE_BOX_BIG_DECIMAL, PHANTOM_CLASS_NAME_RANGE_BOX_BIG_DECIMAL, "Sequence Box BigDecimal"));
//    list.add(new ScoutTypeDecorator(INDEX_SEQUENCE_BOX_DATE, PHANTOM_CLASS_NAME_RANGE_BOX_DATE, "Sequence Box Date"));
//    list.add(new ScoutTypeDecorator(INDEX_SEQUENCE_BOX_INTEGER, PHANTOM_CLASS_NAME_RANGE_BOX_INTEGER, "Sequence Box Integer"));
//    list.add(new ScoutTypeDecorator(INDEX_SEQUENCE_BOX_LONG, PHANTOM_CLASS_NAME_RANGE_BOX_LONG, "Sequence Box Long"));
//    list.add(new ScoutTypeDecorator(INDEX_RANGE_BOX_TIME, PHANTOM_CLASS_NAME_RANGE_BOX_TIME, "Sequence Box Time"));
//    list.add(new ScoutTypeDecorator(INDEX_SEQUENCE_BOX_DATE_TIME, PHANTOM_CLASS_NAME_RANGE_BOX_DATE_TIME, "Sequence Box Date-Time"));
//    list.add(new ScoutTypeDecorator(INDEX_DATE_TIME_FIELD, PHANTOM_CLASS_NAME_DATE_TIME_FIELD, "Date-Time Field"));
//    list.add(new ScoutTypeDecorator(INDEX_MAIN_BOX, PHANTOM_CLASS_NAME_MAIN_BOX, "Main Box"));
//
//    //
//    m_indexedNameCache = new HashMap<Integer, ScoutTypeDecorator>(list.size());
//    m_qualifiedNameCache = new HashMap<String, ScoutTypeDecorator>(list.size());
//    for (ScoutTypeDecorator tuple : list) {
//      m_indexedNameCache.put(tuple.getIndex(), tuple);
//      m_qualifiedNameCache.put(tuple.getFullyQualifiedName(), tuple);
//    }
//    list.clear();
//    list = null;
//  }
//
//  public static String getText(String fullyQualifiedClassName) {
//    return instance.getTextImpl(fullyQualifiedClassName);
//  }
//
//  private String getTextImpl(String fullyQualifiedClassName) {
//    ScoutTypeDecorator tuple = null;//m_qualifiedNameCache.get(fullyQualifiedClassName);
//    if (tuple != null) {
//      return tuple.getText();
//    }
//    else {
//      // ScoutSdkUi.logInfo("could not find simple name for class '"+fullyQualifiedClassName+"'");
//      // build simple name
//      int i = fullyQualifiedClassName.lastIndexOf('.');
//      if (i > 0) {
//        return fullyQualifiedClassName.substring(i + 1);
//      }
//      return fullyQualifiedClassName;
//    }
//
//  }
//
//  public static String getText(int decoratorIndex) {
//    return instance.getTextImpl(decoratorIndex);
//  }
//
//  private String getTextImpl(int decoratorIndex) {
//    ScoutTypeDecorator tuple = m_indexedNameCache.get(decoratorIndex);
//    if (tuple != null) {
//      return tuple.getText();
//    }
//    else {
//      ScoutSdkUi.logInfo("could not find simple name for index '" + decoratorIndex + "'");
//      return "no name for index '" + decoratorIndex + "'";
//    }
//  }
//
//  public static ScoutTypeDecorator get(int decoratorIndex) {
//    return instance.getDecoratorImpl(decoratorIndex);
//  }
//
//  private ScoutTypeDecorator getDecoratorImpl(int decoratorIndex) {
//    return m_indexedNameCache.get(decoratorIndex);
//  }
//
//}
