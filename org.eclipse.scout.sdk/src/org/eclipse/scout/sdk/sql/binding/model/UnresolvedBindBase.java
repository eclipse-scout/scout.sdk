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

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * <h3>{@link UnresolvedBindBase}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 26.02.2011
 */
public class UnresolvedBindBase implements IBindBase {

  private final ASTNode m_node;

  public UnresolvedBindBase(ASTNode node) {
    m_node = node;
  }

  @Override
  public int getType() {
    return TYPE_UNRESOLVED;
  }

  /**
   * @return the node
   */
  public ASTNode getNode() {
    return m_node;
  }

  @Override
  public String toString() {
    return getNode().toString();
  }

}
