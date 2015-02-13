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
public class FormFieldApiTest extends AbstractApiTest {

  /**
   * @throws Exception
   * @see {@link LabelPositionPresenter}
   */
  @Test
  public void testLabelPositionPresenter() throws Exception {
    IType type = TypeUtility.getType(RuntimeClasses.IFormField);
    IFieldFilter labelPositionFIlter = FieldFilters.getCompositeFilter(FieldFilters.getFlagsFilter(Flags.AccFinal | Flags.AccStatic),
        FieldFilters.getNameRegexFilter(Pattern.compile("^LABEL\\_POSITION.*$")));
    Set<IField> fields = TypeUtility.getFields(type, labelPositionFIlter);
    HashMap<String, IField> fieldMap = new HashMap<>();
    for (IField f : fields) {
      fieldMap.put(f.getElementName(), f);
    }

    ApiAssert.assertConstantValue(fieldMap.remove("LABEL_POSITION_DEFAULT"), 0);
    ApiAssert.assertConstantValue(fieldMap.remove("LABEL_POSITION_LEFT"), 1);
    ApiAssert.assertConstantValue(fieldMap.remove("LABEL_POSITION_ON_FIELD"), 2);
    ApiAssert.assertConstantValue(fieldMap.remove("LABEL_POSITION_RIGHT"), 3);
    ApiAssert.assertConstantValue(fieldMap.remove("LABEL_POSITION_TOP"), 4);
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
