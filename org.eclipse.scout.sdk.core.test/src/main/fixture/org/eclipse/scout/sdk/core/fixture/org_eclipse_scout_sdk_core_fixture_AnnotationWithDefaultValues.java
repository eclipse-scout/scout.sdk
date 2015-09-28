/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.fixture;

import org.eclipse.scout.sdk.core.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.model.sugar.AbstractManagedAnnotation;

/**
 * <h3>{@link org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues}</h3> managed wrapper for
 * {@link AnnotationWithDefaultValues}
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class org_eclipse_scout_sdk_core_fixture_AnnotationWithDefaultValues extends AbstractManagedAnnotation {
  public static final String TYPE_NAME = AnnotationWithDefaultValues.class.getName();

  public int num(int... optionalCustomDefaultValue) {
    return getValue("num", int.class, optionalCustomDefaultValue);
  }

  public String string(String... optionalCustomDefaultValue) {
    return getValue("string", String.class, optionalCustomDefaultValue);
  }

  public IField enumValue(IField... optionalCustomDefaultValue) {
    return getValue("enumValue", IField.class, optionalCustomDefaultValue);
  }

  public IType type(IType... optionalCustomDefaultValue) {
    return getValue("type", IType.class, optionalCustomDefaultValue);
  }

  public IAnnotation anno(IAnnotation... optionalCustomDefaultValue) {
    return getValue("anno", IAnnotation.class, optionalCustomDefaultValue);
  }

  //coercion

  public String enumValueCoercedToString(String... optionalCustomDefaultValue) {
    return getValue("enumValue", String.class, optionalCustomDefaultValue);
  }

  public String typeCoercedToString(String... optionalCustomDefaultValue) {
    return getValue("type", String.class, optionalCustomDefaultValue);
  }

  public int numFromBoxedType(int... optionalCustomDefaultValue) {
    return getValue("num", Integer.class, optionalCustomDefaultValue);
  }

}
