/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.part;

public class AnnotationProperty {

  private boolean m_inherited;
  private String m_fullyQualifiedName;

  public boolean isInherited() {
    return m_inherited;
  }

  public void setInherited(boolean inherited) {
    m_inherited = inherited;
  }

  public String getFullyQualifiedName() {
    return m_fullyQualifiedName;
  }

  public void setFullyQualifiedName(String fullyQualifiedName) {
    m_fullyQualifiedName = fullyQualifiedName;
  }
}
