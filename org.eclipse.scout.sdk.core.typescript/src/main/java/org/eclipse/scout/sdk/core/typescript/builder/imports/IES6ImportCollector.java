/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.builder.imports;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

/**
 * Collects ES6 imports
 */
public interface IES6ImportCollector {

  /**
   * Adds a new {@link IDataType} element to this collector.
   * 
   * @param element
   *          The {@link IDataType} to register.
   * @param alias
   *          The import alias or {@code null} if no alias should be used (in that case the name of the data type is
   *          used directly).
   * @return The {@link ES6ImportDescriptor} added. Never returns {@code null}.
   * @throws IllegalArgumentException
   *           if for this alias already a data type is registered.
   */
  ES6ImportDescriptor add(IDataType element, String alias);

  /**
   * @return An unmodifiable {@link Set} of all used data type names (or aliases).
   */
  Set<String> usedNames();

  /**
   * Gets an already registered {@link ES6ImportDescriptor} for the given {@link IDataType}
   * 
   * @param element
   *          The {@link IDataType} for which the descriptor should be returned.
   * @return The corresponding {@link ES6ImportDescriptor} or {@code null} no descriptor is registered yet.
   */
  ES6ImportDescriptor descriptorFor(IDataType element);

  /**
   * Consumes the given name without linking it to an {@link IDataType}.
   * 
   * @param name
   *          The name to reserve. Must not be {@code null}.
   */
  void registerReservedName(String name);

  /**
   * @return All registered {@link ES6ImportDescriptor imports}.
   */
  Collection<ES6ImportDescriptor> imports();

  /**
   * @param element
   *          Must not be {@code null}
   * @param alias
   *          May be {@code null}
   */
  record ES6ImportDescriptor(IDataType element, String alias) {

    /**
     * @return The name to use in the source for this element. This is the alias (if present) or the data type name
     *         otherwise.
     */
    public String nameForSource() {
      return alias == null ? element.name() : alias;
    }

    /**
     * @return The import specifier. This is the data type name (if no alias is present) or 'dataTypeName as alias'
     *         otherwise.
     */
    public String importSpecifier() {
      var name = element.name();
      if (alias == null) {
        return name;
      }
      return name + " as " + alias;
    }

    @Override
    public String toString() {
      return nameForSource();
    }
  }
}
