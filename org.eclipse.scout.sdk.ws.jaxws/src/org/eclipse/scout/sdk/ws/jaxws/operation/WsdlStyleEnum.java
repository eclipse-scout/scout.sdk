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
package org.eclipse.scout.sdk.ws.jaxws.operation;

import org.eclipse.scout.sdk.ws.jaxws.Texts;

public enum WsdlStyleEnum {
  DocumentLiteralWrapped(Texts.get("DocumentLiteralWrapped")),
  DocumentLiteral(Texts.get("DocumentLiteral"));

  private String m_label;

  private WsdlStyleEnum(String label) {
    m_label = label;
  }

  public String getLabel() {
    return m_label;
  }
}
