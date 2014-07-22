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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * <h3>{@link PropertyBasedBindBase}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 27.02.2011
 */
public class PropertyBasedBindBase implements IBindBase {

  private ArrayList<String> m_typeAssignmentSignatures;

  public PropertyBasedBindBase() {
    m_typeAssignmentSignatures = new ArrayList<String>();
  }

  @Override
  public int getType() {
    return TYPE_PROPERTY_BASE;
  }

  public void addAssignedSignature(String assignedSignature) {
    m_typeAssignmentSignatures.add(assignedSignature);
  }

  public void addAssignedSignatures(String[] assignedSignatures) {
    m_typeAssignmentSignatures.addAll(Arrays.asList(assignedSignatures));
  }

  public String[] getAssignedSignatures() {
    return m_typeAssignmentSignatures.toArray(new String[m_typeAssignmentSignatures.size()]);
  }

}
