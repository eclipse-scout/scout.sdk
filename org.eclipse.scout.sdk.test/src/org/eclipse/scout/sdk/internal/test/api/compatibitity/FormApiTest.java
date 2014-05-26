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
package org.eclipse.scout.sdk.internal.test.api.compatibitity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.testing.ApiAssert;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.LabelPositionPresenter;
import org.eclipse.scout.sdk.util.type.FieldFilters;
import org.eclipse.scout.sdk.util.type.IFieldFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class FormApiTest extends AbstractApiTest {

  /**
   * @throws Exception
   * @see {@link LabelPositionPresenter}
   */
  @Test
  public void testDisplayHintPresenter() throws Exception {
    IType type = TypeUtility.getType(RuntimeClasses.IForm);
    IFieldFilter labelPositionFIlter = FieldFilters.getCompositeFilter(FieldFilters.getFlagsFilter(Flags.AccFinal | Flags.AccStatic),
        FieldFilters.getNameRegexFilter(Pattern.compile("^DISPLAY\\_HINT.*$")));
    Set<IField> fields = TypeUtility.getFields(type, labelPositionFIlter);
    HashMap<String, IField> fieldMap = new HashMap<String, IField>();
    for (IField f : fields) {
      fieldMap.put(f.getElementName(), f);
    }
    ApiAssert.assertConstantValue(fieldMap.remove("DISPLAY_HINT_DIALOG"), 0);
    ApiAssert.assertConstantValue(fieldMap.remove("DISPLAY_HINT_POPUP_WINDOW"), 10);
    ApiAssert.assertConstantValue(fieldMap.remove("DISPLAY_HINT_POPUP_DIALOG"), 12);
    ApiAssert.assertConstantValue(fieldMap.remove("DISPLAY_HINT_VIEW"), 20);
    if (!fieldMap.isEmpty()) {
      StringBuilder message = new StringBuilder("Field are not considered of SDK '");
      Iterator<String> it = fieldMap.keySet().iterator();
      message.append(it.next());
      while (it.hasNext()) {
        message.append(", ").append(it.next());
      }
      message.append("'.");
      Assert.fail(message.toString());
    }
  }

  /**
   * @throws Exception
   * @see {@link LabelPositionPresenter}
   */
  @Test
  public void testViewIdPresenter() throws Exception {
    IType type = TypeUtility.getType(RuntimeClasses.IForm);
    IFieldFilter labelPositionFIlter = FieldFilters.getCompositeFilter(FieldFilters.getFlagsFilter(Flags.AccFinal | Flags.AccStatic),
        FieldFilters.getNameRegexFilter(Pattern.compile("^VIEW\\_ID.*$")));
    Set<IField> fields = TypeUtility.getFields(type, labelPositionFIlter);
    HashMap<String, IField> fieldMap = new HashMap<String, IField>();
    for (IField f : fields) {
      fieldMap.put(f.getElementName(), f);
    }
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_N"), "N");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_NE"), "NE");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_E"), "E");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_SE"), "SE");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_S"), "S");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_SW"), "SW");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_W"), "W");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_NW"), "NW");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_CENTER"), "C");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_OUTLINE"), "OUTLINE");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_OUTLINE_SELECTOR"), "OUTLINE_SELECTOR");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_PAGE_DETAIL"), "PAGE_DETAIL");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_PAGE_SEARCH"), "PAGE_SEARCH");
    ApiAssert.assertConstantValue(fieldMap.remove("VIEW_ID_PAGE_TABLE"), "PAGE_TABLE");
    if (!fieldMap.isEmpty()) {
      StringBuilder message = new StringBuilder("Field are not considered of SDK '");
      Iterator<String> it = fieldMap.keySet().iterator();
      message.append(it.next());
      while (it.hasNext()) {
        message.append(", ").append(it.next());
      }
      message.append("'.");
      Assert.fail(message.toString());
    }
  }

}
