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

import org.eclipse.jdt.core.IField;
import org.eclipse.scout.sdk.ui.fields.proposal.AbstractContentProposalEx;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>ButtonDisplayTypeProposal</h3> ...
 */
public class ConstantFieldProposal<T> extends AbstractContentProposalEx {
  private final IField m_field;
  private final T m_constantValue;

  public ConstantFieldProposal(String label, Image image, IField field, T constantValue) {
    super(label, image);
    m_field = field;
    m_constantValue = constantValue;
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    if (selected && m_field != null) {
      return getText() + " (" + m_field.getElementName() + " = " + m_constantValue + " )";
    }
    return super.getLabel(selected, expertMode);
  }

  public IField getField() {
    return m_field;
  }

  public T getConstantValue() {
    return m_constantValue;
  }

  @Override
  public int hashCode() {
    long h = super.hashCode() ^ m_field.hashCode() ^ m_constantValue.hashCode();
    return (int) h;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ConstantFieldProposal)) return false;
    if (!super.equals(obj)) return false;
    return compareTo((ConstantFieldProposal<?>) obj) == 0;
  }

  public int compareTo(ConstantFieldProposal<?> o) {
    int c = super.compareTo(o);
    if (c != 0) return c;
    c = compareImpl(o.m_field, m_field);
    if (c != 0) return c;
    c = compareImpl(o.m_constantValue, m_constantValue);

    return c;
  }

}
