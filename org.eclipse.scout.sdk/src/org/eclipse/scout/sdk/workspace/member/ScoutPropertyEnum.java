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
package org.eclipse.scout.sdk.workspace.member;

/**
 * Important: when editing this file, also update org.eclipse.scout.rt.shared.bsicase.ConfigProperty
 */
public enum ScoutPropertyEnum {
  /**
   * Boolean
   */
  BOOLEAN,
    /**
     * Double
     */
    DOUBLE,

    /**
     * String
     */
    DRAG_AND_DROP_TYPE,
    /**
     * Integer
     */
    INTEGER,
    /**
     * Long
     */
    LONG,
    /**
     * Plain-String
     */
    STRING,
    /**
     * e.g. arial,bold,11
     */
    FONT,

    /**
     * HEX COLOR e.g. FFFFFF
     */
    COLOR,
    /**
     * Object
     */
    OBJECT,
    /**
     * int
     */
    BUTTON_DISPLAY_STYLE,
    /**
     * int
     */
    BUTTON_SYSTEM_TYPE,
    /**
     * Class&lt;? extends ICodeType&gt;
     */
    CODE_TYPE,
    /**
     * int
     */
    COMPOSER_ATTRIBUTE_TYPE,
    /**
     * String[]
     */
    FILE_EXTENSIONS,
    /**
     * int
     */
    FORM_DISPLAY_HINT,

    /**
     * String
     */
    FORM_VIEW_ID,

    /**
     * int
     */
    HORIZONTAL_ALIGNMENT,
    /**
     * String
     */
    ICON_ID,
    /**
     * Class&lt;? extends IKeyStroke&gt;
     */
    KEY_STROKE,
    /**
     * int
     * 
     * @deprecated
     */
    @Deprecated
    KEY_STROKE_WHEN,
    /**
     * Class&lt;? extends LookupCall&gt;
     */
    LOOKUP_CALL,
    /**
     * Class&lt;? extends ILookupService&gt;
     */
    LOOKUP_SERVICE,
    /**
     * Class&lt;? extends IValueField&gt;
     */
    MASTER_FIELD,
    /**
     * Class&lt;? extends IPage&gt;
     */
    OUTLINE_ROOT_PAGE,
    /**
     * Class&lt;? extends IOutline&gt;
     */
    OUTLINE,
    /**
     * Class&lt;? extends IOutline&gt;[]
     */
    OUTLINES,
    /**
     * Class&lt;? extends IForm&gt;
     */
    FORM,
    /**
     * Class&lt;? extends ISearchForm&gt;
     */
    SEARCH_FORM,
    /**
     * Class&lt;? extends DynamicNls;
     */
    NLS_PROVIDER,
    /**
     * Class&lt;? extends ISqlStyle&gt;
     */
    SQL_STYLE,
    /**
     * an String representing an SQL statement
     */
    SQL,
    /**
     * NLS translated String
     */
    TEXT,
    /**
     * int
     */
    VERTICAL_ALIGNMENT,
    /**
     * Fully qualified class name of a ISwingChartProvider class with bundle symbolic name prefix
     * Example: "com.bsiag.crm.ui.swing/com.bsiag.crm.ui.swing.chart.ForecastChart"
     */
    CHART_QNAME,
    /**
     * {@link java.util.Calendar#MONDAY} ... {@link java.util.Calendar#SUNDAY}
     */
    HOUR_OF_DAY,
    /**
     * duration as type Long in minutes
     */
    DURATION_MINUTES,
    /**
     * class of a local IMenu (used in table and tree)
     * for example NewCompanyMenu.class
     */
    MENU_CLASS,
    /**
     * Class&lt;?&gt; but must be a primitive wrapper type: String, Double, Float, Long, Integer, Byte
     */
    PRIMITIVE_TYPE,
}
