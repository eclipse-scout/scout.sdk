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
package org.eclipse.scout.sdk.ui.internal.view.properties.model.links;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>TypeOpenLink</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.02.2010
 */
public class TypeOpenLink extends AbstractLink {

  private final IType m_type;

  public TypeOpenLink(IType type) {
    super(type.getElementName());
    m_type = type;
    Image img = ScoutSdkUi.getImage(ScoutSdkUi.Class);

    try {
      if (Flags.isInterface(m_type.getFlags())) {
        img = ScoutSdkUi.getImage(ScoutSdkUi.Interface);
      }
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logWarning("could not read flags of java element '" + m_type.getElementName() + "'.", e);
    }
    setImage(img);

  }

  /**
   * @return the scoutType
   */
  public IType getType() {
    return m_type;
  }

  @Override
  public void execute() {
    try {
      JavaUI.openInEditor(getType());
    }
    catch (Exception ex) {
      ScoutSdkUi.logWarning("could not open java element '" + getType().getFullyQualifiedName() + "' in editor.", ex);
    }

  }

}
