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

import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

public class SignatureProposal implements IContentProposalEx {

  private final String m_signature;
  private String m_simpleTypeName;
  private String m_fullyQuallifiedName;
  private String m_packageName;
  private boolean m_isPrimitive = false;

  public SignatureProposal(String signature) {
    m_signature = signature;
    m_isPrimitive = ScoutSdkUtility.getSignatureType(signature) == Signature.BASE_TYPE_SIGNATURE;
    m_simpleTypeName = ScoutSdkUtility.getSimpleTypeSignature(signature);
    if (!m_isPrimitive) {
      m_packageName = Signature.getSignatureQualifier(m_signature);
      m_fullyQuallifiedName = ScoutSdkUtility.getNonGenericSimpleName(m_signature);
    }
  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    return m_simpleTypeName.length();
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    Image img = null;
    img = ScoutSdkUi.getImage(ScoutSdkUi.FieldPublic);
    return img;
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    if (m_isPrimitive) {
      return getPrimitiveLabel(selected, expertMode);
    }
    else {
      return getTypeLabel(selected, expertMode);
    }
  }

  private String getPrimitiveLabel(boolean selected, boolean expertMode) {
    String label = m_simpleTypeName;
    return label;
  }

  private String getTypeLabel(boolean selected, boolean expertMode) {
    String name = "";
    String packagePostfix = "";
    if (expertMode) {
      name = m_simpleTypeName;
      packagePostfix = "  (" + m_packageName + ")";
    }
    else {
      name = m_simpleTypeName;
      packagePostfix = "  (" + m_fullyQuallifiedName + ")";
    }
    if (selected) {
      name = name + packagePostfix;
    }
    return name;
  }

  public String getSignature() {
    return m_signature;
  }

  @Override
  public int hashCode() {
    return getSignature().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SignatureProposal) {
      SignatureProposal comp = (SignatureProposal) obj;
      return getSignature().equals(comp.getSignature());
    }
    return false;
  }

}
