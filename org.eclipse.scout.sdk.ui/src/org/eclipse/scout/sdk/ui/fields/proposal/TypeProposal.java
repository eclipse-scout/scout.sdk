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
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

public class TypeProposal implements ITypeProposal {

  private final String m_simpleName;
  private final String m_fullyQualifiedName;
  private final String m_packageName;
  // lazy created type
  private IType m_type;

  public TypeProposal(String fullyQuallifiedName) {
    this(fullyQuallifiedName.substring(fullyQuallifiedName.lastIndexOf(".") + 1), fullyQuallifiedName, fullyQuallifiedName.substring(0, fullyQuallifiedName.lastIndexOf(".")));
  }

  public TypeProposal(IType type) {
    this(type.getElementName(), type.getFullyQualifiedName(), type.getPackageFragment().getElementName());
    m_type = type;
  }

  public TypeProposal(String simpleName, String fullyQualifiedName, String packageName) {
    m_simpleName = simpleName;
    m_fullyQualifiedName = fullyQualifiedName;
    m_packageName = packageName;
  }

  @Override
  public int getProposalType() {
    return TYPE_BCTYPE;
  }

  @Override
  public String getLabel(boolean selected, boolean expertMode) {
    String name = "";
    String packagePostfix = "";
    if (expertMode) {
      name = getSimpleName();
      packagePostfix = "  (" + getPackageName() + ")";
    }
    else {
      name = getSimpleName();
      packagePostfix = "  (" + getFullyQualifiedName() + ")";
    }
    if (selected) {
      name = name + packagePostfix;
    }
    return name;
  }

  @Override
  public Image getImage(boolean selected, boolean expertMode) {
    Image img = null;
    img = ScoutSdkUi.getImage(ScoutSdkUi.FieldPublic);
    return img;
  }

  @Override
  public int getCursorPosition(boolean selected, boolean expertMode) {
    if (expertMode) {
      return getSimpleName().length();
    }
    else {
      return getSimpleName().length();
    }
  }

  public String getFullyQualifiedName() {
    return m_fullyQualifiedName;
  }

  public String getSimpleName() {
    return m_simpleName;
  }

  public String getPackageName() {
    return m_packageName;
  }

  @Override
  public IType getType() {
    if (m_type == null) {
      m_type = ScoutSdk.getType(getFullyQualifiedName());
    }
    return m_type;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TypeProposal) {
      return CompareUtility.equals(((TypeProposal) obj).getType(), getType());
    }
    return false;
  }
}
