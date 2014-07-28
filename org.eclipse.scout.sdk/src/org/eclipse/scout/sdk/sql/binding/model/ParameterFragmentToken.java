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
 * <h3>{@link ParameterFragmentToken}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 25.02.2011
 */
public class ParameterFragmentToken implements ISqlToken {

  private final String m_parameterName;

  public ParameterFragmentToken(String parameterName) {
    m_parameterName = parameterName;
  }

  @Override
  public int getType() {
    return TYPE_PARAMETER;
  }

  /**
   * @return the parameterName
   */
  public String getParameterName() {
    return m_parameterName;
  }
}
