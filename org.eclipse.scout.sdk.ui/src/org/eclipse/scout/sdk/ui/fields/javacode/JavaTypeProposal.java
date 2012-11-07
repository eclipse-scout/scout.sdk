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
package org.eclipse.scout.sdk.ui.fields.javacode;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.fieldassist.IContentProposal;

public class JavaTypeProposal implements IContentProposal {

  private final String m_simpleName;
  private IType m_type;

  public JavaTypeProposal(String simpleName) {
    m_simpleName = simpleName;
  }

  public JavaTypeProposal(IType type) {
    this(type.getElementName());
    m_type = type;
  }

  @Override
  public String getContent() {
    return m_simpleName;
  }

  @Override
  public int getCursorPosition() {
    return m_simpleName.length();
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getLabel() {
    String name = m_simpleName;
    if (m_type != null) {
      name = name + " (" + m_type.getFullyQualifiedName() + ")";
    }
    return name;
  }

  public boolean isPrimitive() {
    return m_type == null;
  }

  public IType getType() {
    return m_type;
  }

}
