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

import org.eclipse.jdt.core.IType;

/**
 * <h3>{@link ServerSessionBindBase}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 27.02.2011
 */
public class ServerSessionBindBase implements IBindBase {

  private final String m_bindVarName;
  private final IType m_serverSession;

  public ServerSessionBindBase(String bindVarName, IType serverSession) {
    m_bindVarName = bindVarName;
    m_serverSession = serverSession;
  }

  @Override
  public int getType() {
    return TYPE_SERVER_SESSION;
  }

  /**
   * @return the serverSession
   */
  public IType getServerSession() {
    return m_serverSession;
  }

  /**
   * @return the bindVarName
   */
  public String getBindVarName() {
    return m_bindVarName;
  }

}
