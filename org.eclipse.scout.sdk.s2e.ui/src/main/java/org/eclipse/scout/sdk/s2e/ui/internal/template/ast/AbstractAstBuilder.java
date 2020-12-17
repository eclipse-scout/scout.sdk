/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.template.ast;

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.scout.sdk.core.util.Ensure;

/**
 * <h3>{@link AbstractAstBuilder}</h3>
 *
 * @since 5.2.0
 */
@SuppressWarnings("unchecked")
public abstract class AbstractAstBuilder<INSTANCE extends AbstractAstBuilder<INSTANCE>> {
  private final AstNodeFactory m_owner;
  private final Set<ModifierKeyword> m_modifiers;
  private final INSTANCE m_return;

  private boolean m_createLinks;
  private TypeDeclaration m_declaringType;

  protected AbstractAstBuilder(AstNodeFactory owner) {
    m_owner = Ensure.notNull(owner);
    m_modifiers = new LinkedHashSet<>();
    m_return = (INSTANCE) this;
    m_createLinks = true;
  }

  public INSTANCE withCreateLinks(boolean createLinks) {
    m_createLinks = createLinks;
    return m_return;
  }

  public INSTANCE withModifiers(ModifierKeyword... keywords) {
    if (keywords == null || keywords.length < 1) {
      return (INSTANCE) this;
    }

    addAll(m_modifiers, keywords);
    return m_return;
  }

  public INSTANCE in(TypeDeclaration declaringType) {
    m_declaringType = declaringType;
    return m_return;
  }

  public AstNodeFactory getFactory() {
    return m_owner;
  }

  public boolean isCreateLinks() {
    return m_createLinks;
  }

  protected Set<ModifierKeyword> getModifiers() {
    return unmodifiableSet(m_modifiers);
  }

  protected TypeDeclaration getDeclaringType() {
    return m_declaringType;
  }

  public abstract INSTANCE insert();
}
