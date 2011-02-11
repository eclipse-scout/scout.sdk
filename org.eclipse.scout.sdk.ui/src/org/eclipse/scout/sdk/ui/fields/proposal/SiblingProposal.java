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
package org.eclipse.scout.sdk.ui.fields.proposal;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.ITypeSibling;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>SiblingProposal</h3> ...
 */
public class SiblingProposal implements IContentProposalEx, ITypeSibling {
  public static final SiblingProposal SIBLING_BEGINNING = new SiblingProposal("first", TYPE_BEGINNING);
  public static final SiblingProposal SIBLING_END = new SiblingProposal("last", TYPE_END);

  private String m_text;

  private IType m_type;

  private int m_siblingType;

  public SiblingProposal(String text, int siblingType) {
    m_text = text;
    m_siblingType = siblingType;
  }

  public SiblingProposal(IType sibling) {
    m_type = sibling;
    m_text = sibling.getElementName();//ScoutSourceUtilities.getTranslatedMethodStringValue(sibling, "getConfiguredLabel");

    m_text = m_text + " [before]";
    m_siblingType = TYPE_SIBLING;
  }

  public int getCursorPosition(boolean selected, boolean expertMode) {
    return m_text.length();
  }

  public Image getImage(boolean selected, boolean expertMode) {
    return ScoutSdkUi.getImage(ScoutSdkUi.FormField);
  }

  public String getLabel(boolean selected, boolean expertMode) {
    return m_text;
  }

  @Override
  public IType getScoutType() {
    return m_type;
  }

  @Override
  public int getSiblingType() {
    return m_siblingType;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SiblingProposal) {
      return getScoutType().equals(((SiblingProposal) obj).getScoutType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getScoutType().hashCode();
  }

}
