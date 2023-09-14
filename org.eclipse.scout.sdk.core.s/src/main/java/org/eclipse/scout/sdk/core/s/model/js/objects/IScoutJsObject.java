/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.objects;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.s.model.js.IScoutJsElement;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModel;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsProperty;
import org.eclipse.scout.sdk.core.s.model.js.prop.ScoutJsPropertyQuery;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.typescript.model.api.IFunction;

/**
 * Represents a JavaScript or TypeScript Scout JS object (an element based on an {@link IES6Class} and having an
 * objectType).
 */
public interface IScoutJsObject extends IScoutJsElement {

  /**
   * @return The shortest name possible for this object. Is the {@link #name()} if the owning {@link ScoutJsModel} uses
   *         the 'scout' {@link ScoutJsModel#namespace() namespace} or the {@link #qualifiedName()} otherwise.
   */
  default String shortName() {
    if (ScoutJsCoreConstants.NAMESPACE.equals(scoutJsModel().namespace().orElse(null))) {
      return name();
    }
    return qualifiedName();
  }

  /**
   * @return The qualified name of this object. This is the {@link ScoutJsModel#namespace()} (if available) + '.' +
   *         {@link #name()}.
   */
  default String qualifiedName() {
    return IScoutJsElement.toQualifiedName(scoutJsModel().namespace().orElse(null), name());
  }

  /**
   * Checks if this object or one of its super objects (object of super classes or super interfaces) declares a
   * {@link ScoutJsProperty} having given name.
   * 
   * @param name
   *          The property name to check. Must not be {@code null}.
   * @return {@code true} if this object or one of its super classes declares a property with given name. {@code false}
   *         otherwise.
   */
  default boolean hasProperty(String name) {
    return findProperties()
        .withSupers(true)
        .withName(name)
        .existsAny();
  }

  /**
   * Gets the {@link ScoutJsProperty properties} of this {@link IScoutJsObject} without its supers. Use
   * {@link #findProperties()} to also include the properties of the super objects.
   * 
   * @return An unmodifiable {@link Map} holding all {@link ScoutJsProperty properties} grouped by name which are
   *         directly declared in this {@link IScoutJsObject} (ignoring properties of super classes or super
   *         interfaces).
   */
  Map<String, ScoutJsProperty> properties();

  /**
   * Finds {@link ScoutJsProperty properties} of this {@link IScoutJsObject}. By default, all {@link ScoutJsProperty
   * properties} directly declared in this {@link IScoutJsObject} are returned (ignoring all properties of super
   * objects).
   * 
   * @return A {@link ScoutJsPropertyQuery} to get properties available to this {@link IScoutJsObject}.
   */
  default ScoutJsPropertyQuery findProperties() {
    return new ScoutJsPropertyQuery(this);
  }

  /**
   * @return An unmodifiable {@link List} holding all methods in this {@link IScoutJsObject} having the name
   *         {@value ScoutJsCoreConstants#FUNCTION_NAME_INIT}.
   */
  List<IFunction> _inits();
}
