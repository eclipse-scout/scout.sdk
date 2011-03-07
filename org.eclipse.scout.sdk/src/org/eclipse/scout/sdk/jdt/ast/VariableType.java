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
package org.eclipse.scout.sdk.jdt.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <h3>{@link VariableType}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 27.02.2011
 */
public class VariableType {

  private final String m_variableName;
  private String m_typeSignature;
  private List<String /*signature*/> m_assignedTypeSignatures;

  public VariableType(String variableName) {
    m_variableName = variableName;
    m_assignedTypeSignatures = new ArrayList<String>();
  }

  /**
   * @return the variableName
   */
  public String getVariableName() {
    return m_variableName;
  }

  public String[] getAssignedTypeSignatures() {
    return m_assignedTypeSignatures.toArray(new String[m_assignedTypeSignatures.size()]);
  }

  void addAssignedSignature(String signature) {
    m_assignedTypeSignatures.add(signature);
  }

  void addAssignedSignatures(String[] signatures) {
    m_assignedTypeSignatures.addAll(Arrays.asList(signatures));
  }

  /**
   * @param typeSignature
   *          the typeSignature to set
   */
  void setTypeSignature(String typeSignature) {
    m_typeSignature = typeSignature;
  }

  /**
   * @return the typeSignature
   */
  public String getTypeSignature() {
    return m_typeSignature;
  }

}
