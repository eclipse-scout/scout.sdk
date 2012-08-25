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
package org.eclipse.scout.sdk.operation.form.formdata;

/**
 *
 */
public class MethodParameter {

  private String m_signature;
  private String m_name;

  public MethodParameter(String signature, String name) {
    m_signature = signature;
    m_name = name;
  }

  public void setSignature(String signature) {
    m_signature = signature;
  }

  public String getSignature() {
    return m_signature;
  }

  public void setName(String name) {
    m_name = name;
  }

  public String getName() {
    return m_name;
  }
}
