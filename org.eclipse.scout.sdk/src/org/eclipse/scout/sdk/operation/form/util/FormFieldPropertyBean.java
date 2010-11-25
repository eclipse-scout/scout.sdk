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
package org.eclipse.scout.sdk.operation.form.util;

import java.util.ArrayList;

public class FormFieldPropertyBean {

  private String m_signatureType;
  private String m_name;
  private ArrayList<String> imports = new ArrayList<String>(2);

  public FormFieldPropertyBean(String name) {
    m_name = name;
  }

  public void setSignatureType(String string) {
    m_signatureType = string;

  }

  public String getName() {
    return m_name;
  }

  public String getSignatureType() {
    return m_signatureType;
  }

  public void addImport(String paramType) {
    imports.add(paramType);
  }

  public String[] getAllImports() {
    return imports.toArray(new String[imports.size()]);
  }
}
