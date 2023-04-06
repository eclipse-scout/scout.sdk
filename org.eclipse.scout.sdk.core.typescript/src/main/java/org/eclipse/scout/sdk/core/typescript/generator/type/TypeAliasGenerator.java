/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.generator.type;

import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Optional;

import org.eclipse.scout.sdk.core.typescript.builder.ITypeScriptSourceBuilder;
import org.eclipse.scout.sdk.core.typescript.builder.nodeelement.NodeElementBuilder;
import org.eclipse.scout.sdk.core.typescript.generator.nodeelement.AbstractNodeElementGenerator;

/**
 * <h3>{@link TypeAliasGenerator}</h3>
 *
 * @since 13.0
 */
public class TypeAliasGenerator<TYPE extends ITypeAliasGenerator<TYPE>> extends AbstractNodeElementGenerator<TYPE> implements ITypeAliasGenerator<TYPE> {

  private IAliasedTypeGenerator<?> m_aliasedTypeGenerator;

  protected TypeAliasGenerator() {
  }

  /**
   * @return A new empty {@link ITypeAliasGenerator}.
   */
  public static TypeAliasGenerator<?> create() {
    return new TypeAliasGenerator<>();
  }

  @Override
  protected void build(ITypeScriptSourceBuilder<?> builder) {
    super.build(builder);
    NodeElementBuilder.create(builder)
        .appendModifiers(modifiers())
        .append("type ")
        .append(elementName().orElseThrow(() -> newFail("Type must have a name.")))
        .equalSign()
        .append(aliasedType().orElseThrow(() -> newFail("Type must have an aliased type.")))
        .semicolon();
  }

  @Override
  public Optional<IAliasedTypeGenerator<?>> aliasedType() {
    return Optional.ofNullable(m_aliasedTypeGenerator);
  }

  @Override
  public TYPE withAliasedType(IAliasedTypeGenerator<?> generator) {
    m_aliasedTypeGenerator = generator;
    return thisInstance();
  }
}
