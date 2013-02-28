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
import java.util.regex.Pattern;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonDisplayStylePresenter;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonSystemTypePresenter;
import org.eclipse.scout.sdk.util.type.FieldFilters;
import org.eclipse.scout.sdk.util.type.IFieldFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class ButtonApiTest extends AbstractApiTest {

  /**
   * @throws Exception
   * @see {@link ButtonDisplayStylePresenter}
   */
  @Test
  public void testLabelPositionPresenter() throws Exception {
    IType type = TypeUtility.getType(RuntimeClasses.IButton);
    IFieldFilter filter = FieldFilters.getCompositeFilter(FieldFilters.getFlagsFilter(Flags.AccStatic | Flags.AccFinal),
        FieldFilters.getNameRegexFilter(Pattern.compile("^DISPLAY\\_STYLE.*$")));
    IField[] fields = TypeUtility.getFields(type, filter);
    HashMap<String, IField> fieldMap = new HashMap<String, IField>();
    for (IField f : fields) {
      fieldMap.put(f.getElementName(), f);
    }

    Assert.assertTrue(hasFieldValue(fieldMap.remove("DISPLAY_STYLE_DEFAULT"), 0));
    Assert.assertTrue(hasFieldValue(fieldMap.remove("DISPLAY_STYLE_TOGGLE"), 1));
    Assert.assertTrue(hasFieldValue(fieldMap.remove("DISPLAY_STYLE_RADIO"), 2));
    Assert.assertTrue(hasFieldValue(fieldMap.remove("DISPLAY_STYLE_LINK"), 3));
    if (!fieldMap.isEmpty()) {
      StringBuilder message = new StringBuilder("Fields are not considered of SDK '");
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
   * @see {@link ButtonSystemTypePresenter}
   */
  @Test
  public void testButtonStylePresenter() throws Exception {
    IType type = TypeUtility.getType(RuntimeClasses.IButton);
    IFieldFilter filter = FieldFilters.getCompositeFilter(FieldFilters.getFlagsFilter(Flags.AccStatic | Flags.AccFinal),
        FieldFilters.getNameRegexFilter(Pattern.compile("^SYSTEM\\_TYPE.*$")));
    IField[] fields = TypeUtility.getFields(type, filter);
    HashMap<String, IField> fieldMap = new HashMap<String, IField>();
    for (IField f : fields) {
      fieldMap.put(f.getElementName(), f);
    }

    Assert.assertTrue(hasFieldValue(fieldMap.remove("SYSTEM_TYPE_NONE"), 0));
    Assert.assertTrue(hasFieldValue(fieldMap.remove("SYSTEM_TYPE_CANCEL"), 1));
    Assert.assertTrue(hasFieldValue(fieldMap.remove("SYSTEM_TYPE_CLOSE"), 2));
    Assert.assertTrue(hasFieldValue(fieldMap.remove("SYSTEM_TYPE_OK"), 3));
    Assert.assertTrue(hasFieldValue(fieldMap.remove("SYSTEM_TYPE_RESET"), 4));
    Assert.assertTrue(hasFieldValue(fieldMap.remove("SYSTEM_TYPE_SAVE"), 5));
    Assert.assertTrue(hasFieldValue(fieldMap.remove("SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE"), 6));

    if (!fieldMap.isEmpty()) {
      StringBuilder message = new StringBuilder("Fields are not considered of SDK '");
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
