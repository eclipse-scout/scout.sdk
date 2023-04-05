/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.nodeelement;

import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scout.sdk.core.typescript.generator.AbstractTypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.generator.ITypeScriptElementGenerator;
import org.eclipse.scout.sdk.core.typescript.model.api.Modifier;

/**
 * <h3>{@link AbstractNodeElementGenerator}</h3>
 *
 * @since 13.0
 */
public abstract class AbstractNodeElementGenerator<TYPE extends INodeElementGenerator<TYPE>> extends AbstractTypeScriptElementGenerator<TYPE> implements INodeElementGenerator<TYPE> {

  private final Set<Modifier> m_modifiers = new HashSet<>();
  private ITypeScriptElementGenerator<?> m_declaringGenerator;

  protected AbstractNodeElementGenerator() {
  }

  @Override
  public TYPE withModifiers(Collection<Modifier> modifiers) {
    m_modifiers.addAll(modifiers);
    return thisInstance();
  }

  @Override
  public Collection<Modifier> modifiers() {
    return unmodifiableSet(m_modifiers);
  }

  @Override
  public TYPE withoutModifiers(Collection<Modifier> modifiers) {
    m_modifiers.removeAll(modifiers);
    return thisInstance();
  }

  @Override
  public Optional<ITypeScriptElementGenerator<?>> declaringGenerator() {
    return Optional.ofNullable(m_declaringGenerator);
  }

  /**
   * Sets the declaring {@link ITypeScriptElementGenerator} of this {@link AbstractNodeElementGenerator}.
   *
   * @param parent
   *          The declaring generator.
   * @return This generator.
   */
  public TYPE withDeclaringGenerator(ITypeScriptElementGenerator<?> parent) {
    m_declaringGenerator = parent;
    return thisInstance();
  }
}
