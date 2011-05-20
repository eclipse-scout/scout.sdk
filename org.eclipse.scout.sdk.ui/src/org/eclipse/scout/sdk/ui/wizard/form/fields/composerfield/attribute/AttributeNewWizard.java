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
package org.eclipse.scout.sdk.ui.wizard.form.fields.composerfield.attribute;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

public class AttributeNewWizard extends AbstractWorkspaceWizard {

  private AttributeNewWizardPage m_page1;
  private IType m_declaringType;

  public AttributeNewWizard(IType declaringType) {
    setWindowTitle("New Attribute");
    m_declaringType = declaringType;
  }

  @Override
  public void addPages() {
    m_page1 = new AttributeNewWizardPage(getDeclaringType());
    addPage(m_page1);
  }

  /**
   * @return the declaringType
   */
  public IType getDeclaringType() {
    return m_declaringType;
  }

  @Override
  protected void postFinishDisplayThread() {
    IType createdField = m_page1.getCreatedAttribute();
    if (TypeUtility.exists(createdField)) {
      ScoutSdkUi.showJavaElementInEditor(createdField, false);
    }
  }
}
