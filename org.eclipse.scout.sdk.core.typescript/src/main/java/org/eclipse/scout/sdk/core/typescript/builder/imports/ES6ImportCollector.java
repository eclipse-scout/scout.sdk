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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.util.Ensure;

public class ES6ImportCollector implements IES6ImportCollector {
  private final Map<String, ES6ImportDescriptor> m_imports;
  private final Map<IDataType, ES6ImportDescriptor> m_descriptors;

  public ES6ImportCollector() {
    m_imports = new LinkedHashMap<>();
    m_descriptors = new HashMap<>();
  }

  @Override
  public ES6ImportDescriptor add(String usedName, IDataType element, String alias) {
    var d = new ES6ImportDescriptor(Ensure.notNull(element), alias);
    var previous = m_imports.put(Ensure.notBlank(usedName), d);
    Ensure.isTrue(previous == null);
    m_descriptors.put(element, d);
    return d;
  }

  @Override
  public ES6ImportDescriptor descriptorFor(IDataType element) {
    return m_descriptors.get(element);
  }

  @Override
  public Set<String> usedNames() {
    return m_imports.keySet();
  }

  @Override
  public Collection<ES6ImportDescriptor> imports() {
    return m_imports.values().stream()
        .filter(Objects::nonNull) // remove reserved elements
        .toList();
  }

  @Override
  public void registerReservedName(String name) {
    m_imports.put(name, null);
  }
}
