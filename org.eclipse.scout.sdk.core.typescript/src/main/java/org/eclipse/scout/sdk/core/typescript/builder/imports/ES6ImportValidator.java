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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.scout.sdk.core.typescript.model.api.DataTypeNameEvaluator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;

public class ES6ImportValidator implements IES6ImportValidator {

  @SuppressWarnings("StaticCollection")
  private static final Collection<String> IGNORE_TYPES = new HashSet<>(Arrays.asList(
      "any", "unknown", "never", "void", "object", "IArguments", "Date", "RegExp", "Error", "Array",
      "PromiseLike", "Promise", "ArrayLike", "Partial", "Required", "Readonly", "Pick", "Record",
      "Exclude", "Extract", "Omit", "NonNullable", "this", "ThisType"));

  private IES6ImportCollector m_collector;

  public ES6ImportValidator() {
    m_collector = new ES6ImportCollector();
  }

  @Override
  public CharSequence use(IDataType dataType) {
    return new UniqueNameEvaluator().eval(dataType);
  }

  private class UniqueNameEvaluator extends DataTypeNameEvaluator {
    @Override
    protected String nameForLeafType(IDataType type) {
      return computeUniqueNameAndRegisterUsage(type);
    }
  }

  public static boolean isBuiltInType(String name) {
    return IGNORE_TYPES.contains(name);
  }

  protected String computeUniqueNameAndRegisterUsage(IDataType type) {
    var name = type.name();
    if (type.isPrimitive()) {
      return name;
    }
    if (isBuiltInType(name)) {
      return name; // no import required
    }
    var collector = importCollector();
    var usedNamesForSource = collector.usedNames();
    if (!usedNamesForSource.contains(name)) {
      // first with that name: use without alias
      return collector.add(type, null).nameForSource();
    }
    var existing = collector.descriptorFor(type);
    if (existing != null) {
      // already registered element
      return existing.nameForSource();
    }

    // a new element with an already used name: register with new alias
    var newAlias = getUniqueAlias(name, usedNamesForSource);
    return collector.add(type, newAlias).nameForSource();
  }

  public static String getUniqueAlias(String origName, Collection<String> usedNames) {
    var uniqueName = origName;
    var counter = 0;
    do {
      uniqueName = origName + counter;
      counter++;
    }
    while (usedNames.contains(uniqueName));
    return uniqueName;
  }

  @Override
  public IES6ImportCollector importCollector() {
    return m_collector;
  }

  public void setImportCollector(IES6ImportCollector collector) {
    m_collector = collector;
  }
}
