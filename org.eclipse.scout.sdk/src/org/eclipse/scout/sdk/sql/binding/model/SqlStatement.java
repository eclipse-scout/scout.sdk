/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.sql.binding.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>{@link SqlStatement}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 25.02.2011
 */
public class SqlStatement {
  private List<ISqlToken> m_tokens;
  private List<IBindBase> m_bindBases;
  private int m_offset;
  private int m_length;

  public SqlStatement() {
    m_tokens = new ArrayList<>();
    m_bindBases = new ArrayList<>();
  }

  public void addToken(ISqlToken token) {
    m_tokens.add(token);
  }

  public boolean hasTokens() {
    return !m_tokens.isEmpty();
  }

  public ISqlToken[] getTokens() {
    return m_tokens.toArray(new ISqlToken[m_tokens.size()]);
  }

  public boolean replaceToken(ISqlToken existingToken, ISqlToken newToken) {
    int index = m_tokens.indexOf(existingToken);
    if (index > -1) {
      m_tokens.remove(index);
      m_tokens.add(index, newToken);
      return true;
    }
    return false;
  }

  public void addBindBase(IBindBase bindBase) {
    m_bindBases.add(bindBase);
  }

  public IBindBase[] getBindBases() {
    return m_bindBases.toArray(new IBindBase[m_bindBases.size()]);
  }

  public UnresolvedBindBase[] getUnresolvedBindBases() {
    ArrayList<UnresolvedBindBase> unresolvedBindBases = new ArrayList<>(2);
    for (IBindBase b : m_bindBases) {
      if (b.getType() == IBindBase.TYPE_UNRESOLVED) {
        unresolvedBindBases.add((UnresolvedBindBase) b);
      }
    }
    return unresolvedBindBases.toArray(new UnresolvedBindBase[unresolvedBindBases.size()]);
  }

  public String buildStatement() {
    StringBuilder builder = new StringBuilder();
    for (ISqlToken t : m_tokens) {
      if (t.getType() == ISqlToken.TYPE_STRING_FRAGMENT) {
        builder.append(((StringFragmentToken) t).getValue());
      }
    }
    return builder.toString();
  }

  /**
   * @param offset
   *          the offset to set
   */
  public void setOffset(int offset) {
    m_offset = offset;
  }

  /**
   * @return the offset
   */
  public int getOffset() {
    return m_offset;
  }

  /**
   * @param length
   *          the length to set
   */
  public void setLength(int length) {
    m_length = length;
  }

  /**
   * @return the length
   */
  public int getLength() {
    return m_length;
  }
}
