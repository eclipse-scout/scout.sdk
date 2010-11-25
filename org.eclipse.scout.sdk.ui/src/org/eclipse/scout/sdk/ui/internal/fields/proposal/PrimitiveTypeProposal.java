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
package org.eclipse.scout.sdk.ui.internal.fields.proposal;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.fields.proposal.AbstractContentProposalEx;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>PrimitiveTypeProposal</h3> A proposal for a primitive data type or String
 */
public class PrimitiveTypeProposal extends AbstractContentProposalEx {
  private final IType m_primitiveType;

  public PrimitiveTypeProposal(Image image, IType type) {
    super(type.getElementName(), image);
    this.m_primitiveType = type;
  }

  public IType getPrimitiveType() {
    return m_primitiveType;
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    if (selected && m_primitiveType != null) {
      return getText() + " (" + m_primitiveType.getFullyQualifiedName() + ")";
    }
    return super.getLabel(selected, expertMode);
  }

  @Override
  public int hashCode() {
    long h = super.hashCode() ^ m_primitiveType.hashCode();
    return (int) h;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PrimitiveTypeProposal)) return false;
    if (!super.equals(obj)) return false;
    return compareTo((PrimitiveTypeProposal) obj) == 0;
  }

  public int compareTo(PrimitiveTypeProposal o) {
    int c = super.compareTo(o);
    if (c != 0) return c;
    c = compareImpl(o.m_primitiveType, m_primitiveType);
    return c;
  }
}
