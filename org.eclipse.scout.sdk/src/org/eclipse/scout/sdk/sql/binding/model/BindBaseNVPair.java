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
 * <h3>{@link BindBaseNVPair}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 25.02.2011
 */
public class BindBaseNVPair implements IBindBase {

  private final String m_bindVar;
  private ASTNode m_valueNode;

  public BindBaseNVPair(String bindVar, ASTNode valueNode) {
    m_bindVar = bindVar;
    m_valueNode = valueNode;
  }

  @Override
  public int getType() {
    return TYPE_NVPAIR;
  }

  /**
   * @return the bindVar
   */
  public String getBindVar() {
    return m_bindVar;
  }

  /**
   * @return the valueNode
   */
  public ASTNode getValueNode() {
    return m_valueNode;
  }

  @Override
  public String toString() {
    return "NVPair with bindvar '" + getBindVar() + "'.";
  }
}
