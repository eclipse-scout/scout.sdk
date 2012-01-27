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
 * Proposal for all subclasses of a type
 */
public class JavaClassProposal extends AbstractContentProposalEx {
  private final IType m_javaClass;

  public JavaClassProposal(String label, Image image, IType javaClass) {
    super(label, image);
    m_javaClass = javaClass;
  }

  public IType getJavaClass() {
    return m_javaClass;
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    if (selected && m_javaClass != null) {
      return getText() + " (" + m_javaClass.getFullyQualifiedName() + ")";
    }
    return super.getLabel(selected, expertMode);
  }

  @Override
  public int hashCode() {
    long h = super.hashCode() ^ getJavaClass().hashCode();
    return (int) h;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JavaClassProposal)) return false;
    if (!super.equals(obj)) return false;
    return compareTo((JavaClassProposal) obj) == 0;
  }

  public int compareTo(JavaClassProposal o) {
    int c = super.compareTo(o);
    if (c != 0) return c;
    c = compareImpl(o.m_javaClass, m_javaClass);
    return c;
  }

}
