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

/**
 * <h3>{@link IgnoredBindBase}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 02.03.2011
 */
public class IgnoredBindBase implements IBindBase {

  private final String m_bindVar;

  /**
   * @param v
   */
  public IgnoredBindBase(String bindVar) {
    m_bindVar = bindVar;
  }

  @Override
  public int getType() {
    return TYPE_IGNORED;

  }

  /**
   * @return the bindVar
   */
  public String getBindVar() {
    return m_bindVar;
  }
}
