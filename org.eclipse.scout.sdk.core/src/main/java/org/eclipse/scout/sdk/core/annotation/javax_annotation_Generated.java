/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.annotation;

import org.eclipse.scout.sdk.core.TypeNames;
import org.eclipse.scout.sdk.core.model.sugar.AbstractManagedAnnotation;

public class javax_annotation_Generated extends AbstractManagedAnnotation {
  public static final String TYPE_NAME = TypeNames.javax_annotation_Generated;

  public String[] value() {
    return getValue("value", String[].class, null);
  }

  public String date(String... optionalCustomDefaultValue) {
    return getValue("date", String.class, optionalCustomDefaultValue);
  }

  public String comments(String... optionalCustomDefaultValue) {
    return getValue("comments", String.class, optionalCustomDefaultValue);
  }

}
