/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.widgetmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.typescript.builder.imports.ES6ImportValidator;
import org.eclipse.scout.sdk.core.util.Strings;

public abstract class Type {

  private static final Pattern NOT_ALLOWED_CLASS_NAME_CHARS = Pattern.compile("\\W");

  private String m_newClassName;
  private final Collection<String> m_usedNames;

  protected Type(CharSequence newClassName, Collection<String> usedNames) {
    m_usedNames = usedNames != null ? usedNames : new HashSet<>();
    m_newClassName = ensureValidName(newClassName, usedNames());
  }

  protected Type(Collection<String> usedNames) {
    this(null, usedNames);
  }

  public Optional<String> newClassName() {
    return Optional.ofNullable(m_newClassName);
  }

  public Type withNewClassName(CharSequence name) {
    newClassName().ifPresent(usedNames()::remove);
    m_newClassName = ensureValidName(name, usedNames());
    newClassName().ifPresent(usedNames()::add);

    return this;
  }

  public static String ensureValidName(CharSequence name, Collection<String> usedNames) {
    var candidate = ensureValidName(name);
    if (candidate == null || !usedNames.contains(candidate)) {
      return candidate;
    }

    return ES6ImportValidator.getUniqueAlias(candidate, usedNames);
  }

  public static String ensureValidName(CharSequence name) {
    if (name == null) {
      return null;
    }
    return Strings.capitalize(NOT_ALLOWED_CLASS_NAME_CHARS.matcher(name).replaceAll("")).toString();
  }

  protected Collection<String> usedNames() {
    return m_usedNames;
  }
}
