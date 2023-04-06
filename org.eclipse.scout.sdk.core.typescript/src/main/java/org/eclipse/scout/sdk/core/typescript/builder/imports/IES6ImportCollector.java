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

public interface IES6ImportCollector {

  ES6ImportDescriptor add(String usedName, IDataType element, String alias);

  Set<String> usedNames();

  ES6ImportDescriptor descriptorFor(IDataType element);

  void registerReservedName(String name);

  Collection<ES6ImportDescriptor> imports();

  /**
   * @param alias
   *          May be {@code null}
   */
  record ES6ImportDescriptor(IDataType element, String alias) {

    String nameForSource() {
      return alias == null ? element.name() : alias;
    }

    String importSpecifier() {
      if (alias == null) {
        return element.name();
      }
      return element().name() + " as " + alias;
    }
  }
}
