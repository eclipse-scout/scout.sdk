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
package org.eclipse.scout.sdk.operation.service;

import java.util.ArrayList;
import java.util.Arrays;

public class ParameterArgument {
  private String m_name;
  private String m_type;
  private ArrayList<String> m_fullyQualifiedImports = new ArrayList<String>();

  public ParameterArgument() {

  }

  public ParameterArgument(String name, String type) {
    m_name = name;
    m_type = type;
  }

  public String getName() {
    return m_name;
  }

  public void setName(String name) {
    m_name = name;
  }

  public String getType() {
    return m_type;
  }

  public void setType(String type) {
    m_type = type;
  }

  public ArrayList<String> getFullyQualifiedImports() {
    return m_fullyQualifiedImports;
  }

  public void addFullyQualifiedImport(String fullyQualifiedImport) {
    m_fullyQualifiedImports.add(fullyQualifiedImport);
  }

  public void setFullyQualifiedImports(String[] allImports) {
    m_fullyQualifiedImports.clear();
    m_fullyQualifiedImports.addAll(Arrays.asList(allImports));
  }

}
