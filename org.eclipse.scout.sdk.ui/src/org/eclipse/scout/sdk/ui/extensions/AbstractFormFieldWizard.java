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
package org.eclipse.scout.sdk.ui.extensions;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;

public abstract class AbstractFormFieldWizard extends AbstractWorkspaceWizard {

  private IType m_declaringType;

  public AbstractFormFieldWizard() {
    setWindowTitle(Texts.get("NewFormField"));
  }

  public void initWizard(IType declaringType) {
    m_declaringType = declaringType;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public abstract void setSuperType(IType superType);

}
