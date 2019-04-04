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
package org.eclipse.scout.sdk.core.model.api;

import java.util.function.Supplier;

/**
 * <h3>{@link AnnotationWithDefaultValues}</h3> managed wrapper for {@link AnnotationWithDefaultValues}
 *
 * @since 5.1.0
 */
public class AnnotationWithDefaultValues extends AbstractManagedAnnotation {
  public static final String TYPE_NAME = org.eclipse.scout.sdk.core.fixture.AnnotationWithDefaultValues.class.getName();

  public int num() {
    return num(null);
  }

  public int num(int optionalCustomDefaultValue) {
    return num(() -> optionalCustomDefaultValue);
  }

  public int num(Supplier<Integer> optionalCustomDefaultValue) {
    return getValue("num", int.class, optionalCustomDefaultValue);
  }

  public String string() {
    return string((Supplier<String>) null);
  }

  public String string(String optionalCustomDefaultValue) {
    return string(() -> optionalCustomDefaultValue);
  }

  public String string(Supplier<String> optionalCustomDefaultValue) {
    return getValue("string", String.class, optionalCustomDefaultValue);
  }

  public IField enumValue() {
    return enumValue((Supplier<IField>) null);
  }

  public IField enumValue(IField optionalCustomDefaultValue) {
    return enumValue(() -> optionalCustomDefaultValue);
  }

  public IField enumValue(Supplier<IField> optionalCustomDefaultValue) {
    return getValue("enumValue", IField.class, optionalCustomDefaultValue);
  }

  public IType type() {
    return type((Supplier<IType>) null);
  }

  public IType type(IType optionalCustomDefaultValue) {
    return type(() -> optionalCustomDefaultValue);
  }

  public IType type(Supplier<IType> optionalCustomDefaultValue) {
    return getValue("type", IType.class, optionalCustomDefaultValue);
  }

  public IAnnotation anno() {
    return anno((Supplier<IAnnotation>) null);
  }

  public IAnnotation anno(IAnnotation optionalCustomDefaultValue) {
    return anno(() -> optionalCustomDefaultValue);
  }

  public IAnnotation anno(Supplier<IAnnotation> optionalCustomDefaultValue) {
    return getValue("anno", IAnnotation.class, optionalCustomDefaultValue);
  }

  //coercion

  public String enumValueCoercedToString() {
    return getValue("enumValue", String.class, null);
  }

  public String typeCoercedToString() {
    return getValue("type", String.class, null);
  }

  public int numFromBoxedType() {
    return getValue("num", Integer.class, null);
  }
}
