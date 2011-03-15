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
package org.eclipse.scout.sdk.jdt.signature;

/**
 * <h3>{@link Import}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 15.03.2011
 */
public class Import {
  private final String m_simpleName;
  private final String m_packageName;

  public Import(String simpleName, String packageName) {
    m_simpleName = simpleName;
    m_packageName = packageName;
  }

}
