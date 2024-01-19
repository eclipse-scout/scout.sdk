/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.fixture.managed;

import java.util.function.Supplier;

import org.eclipse.scout.sdk.core.java.apidef.ApiFunction;
import org.eclipse.scout.sdk.core.java.apidef.ITypeNameSupplier;
import org.eclipse.scout.sdk.core.java.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IAnnotation;
import org.eclipse.scout.sdk.core.java.model.api.IField;
import org.eclipse.scout.sdk.core.java.model.api.IType;

/**
 * <h3>{@link AnnotationWithSingleValues}</h3> managed wrapper for {@link AnnotationWithSingleValues}
 *
 * @since 5.1.0
 */
public class AnnotationWithSingleValues extends AbstractManagedAnnotation {
  protected static final ApiFunction<?, ITypeNameSupplier> TYPE_NAME = new ApiFunction<>(ITypeNameSupplier.of(org.eclipse.scout.sdk.core.java.fixture.AnnotationWithSingleValues.class.getName()));

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

  // direct convert to wrapped type

  public ArrayValueAnnot arrayValueAnnot() {
    return getValue("anno", ArrayValueAnnot.class, null);
  }
}
