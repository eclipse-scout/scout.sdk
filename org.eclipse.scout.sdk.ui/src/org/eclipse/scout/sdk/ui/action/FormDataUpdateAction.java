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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.formdata.FormDataUpdateOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;

/**
 *
 */
public class FormDataUpdateAction extends AbstractOperationAction {
  private IType m_type;

  public FormDataUpdateAction() {
    super(Texts.get("UpdateFormData"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolLoading), null, false, Category.UDPATE);
  }

  @Override
  public boolean isVisible() {
    return m_type != null && m_type.getDeclaringType() == null;
  }

  public void setType(IType type) {
    m_type = type;
    setOperation(new FormDataUpdateOperation(type));
  }
}
