/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.prop;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.s.model.js.datatypedetect.PropertyDataTypeDetector;
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject;
import org.eclipse.scout.sdk.core.typescript.model.api.IField;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.FinalValue;

/**
 * Represents a property of an {@link IScoutJsObject}. Use {@link IScoutJsObject#properties()} or
 * {@link IScoutJsObject#findProperties()} to access properties of such an object.
 */
public class ScoutJsProperty {

  private final FinalValue<ScoutJsPropertyType> m_type;
  private final PropertyDataTypeDetector m_detector; // may be null
  private final IScoutJsObject m_scoutJsObject;
  private final IField m_field;

  protected ScoutJsProperty(IScoutJsObject scoutJsObject, IField field, PropertyDataTypeDetector dataTypeDetector) {
    m_scoutJsObject = Ensure.notNull(scoutJsObject);
    m_field = Ensure.notNull(field);
    m_type = new FinalValue<>();
    m_detector = dataTypeDetector;
    if (dataTypeDetector != null) {
      dataTypeDetector.markUsed(field().name());
    }
  }

  protected ScoutJsProperty(IScoutJsObject scoutJsObject, IField field) {
    m_scoutJsObject = Ensure.notNull(scoutJsObject);
    m_field = Ensure.notNull(field);
    m_type = new FinalValue<>();
    //noinspection ThisEscapedInObjectConstruction
    m_type.set(new ScoutJsPropertyType(field.dataType().orElse(null), this));
    m_detector = null;
  }

  /**
   * @return The {@link IField} this {@link ScoutJsProperty} is based on.
   */
  public IField field() {
    return m_field;
  }

  /**
   * @return The owner {@link IScoutJsObject}.
   */
  public IScoutJsObject scoutJsObject() {
    return m_scoutJsObject;
  }

  /**
   * @return The {@link ScoutJsPropertyType data type} of this property.
   */
  public ScoutJsPropertyType type() {
    return m_type.computeIfAbsentAndGet(this::detectType);
  }

  protected ScoutJsPropertyType detectType() {
    return m_detector.detect(this);
  }

  /**
   * @return The property name. Is the same as the {@link IField#name()}.
   */
  public String name() {
    return field().name();
  }

  /**
   * @return {@code true} if this property is the Scout {@value ScoutJsCoreConstants#PROPERTY_NAME_OBJECT_TYPE}
   *         property.
   */
  public boolean isObjectType() {
    return ScoutJsCoreConstants.PROPERTY_NAME_OBJECT_TYPE.equals(name());
  }

  /**
   * Properties might be declared on several levels in the class hierarchy. E.g. a property 'x' may be declared on
   * Widget and on FormField. In that case basically the FormField declaration (lower in the hierarchy) should win
   * (might be narrowed) unless the specification of the Widget element (higher in the hierarchy) is more specific.
   */
  public static ScoutJsProperty choose(ScoutJsProperty higher, ScoutJsProperty lower) {
    if (lower == null) {
      return higher; // first occurrence
    }
    if (lower.type().dataType().isEmpty() && higher.type().dataType().isPresent()) {
      // higher level is more specific
      return higher;
    }
    return lower;
  }

  /**
   * Computes possible values for this {@link ScoutJsProperty} if possible.
   * 
   * @param scope
   *          The {@link ScoutJsModel} scope for which the values should be computed. It specifies which possible values
   *          are accessible. These are all elements in this scope model and all exported elements from all
   *          dependencies. Must not be {@code null}.
   * @return A {@link Stream} with possible values for this property based on the {@link ScoutJsModel} scope given.
   */
  public Stream<? extends IScoutJsPropertyValue> computePossibleValues(ScoutJsModel scope) {
    var type = type();
    if (type.isBoolean()) {
      return Stream.of(true, false)
          .map(v -> new ScoutJsBooleanPropertyValue(v, this));
    }
    if (type.isEnumLike()) {
      return type.scoutJsEnums().flatMap(scoutJsEnum -> scoutJsEnum.createPropertyValues(this));
    }

    return scope.findScoutObjects()
        .withoutModifier(Modifier.ABSTRACT)
        .withIncludeDependencies(true)
        .stream()
        .filter(o -> !o.declaringClass().isInterface())
        .filter(o -> o.scoutJsModel() == scope || o.declaringClass().isExportedFromModule())
        .filter(o -> type.isAssignableFrom(o.declaringClass()))
        .map(o -> new ScoutJsObjectPropertyValue(o, this));
  }

  @Override
  public String toString() {
    return name() + " [type=" + type() + ", object=" + scoutJsObject() + ']';
  }
}
