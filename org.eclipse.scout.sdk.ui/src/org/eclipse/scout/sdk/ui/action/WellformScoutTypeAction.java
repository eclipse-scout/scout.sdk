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
import org.eclipse.scout.sdk.operation.util.wellform.WellformScoutTypeOperation;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.page.PageWithTableNodePage;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 *
 */
public class WellformScoutTypeAction extends WellformAction {

  private IType m_type;

  @Override
  public boolean isVisible() {
    return !TypeUtility.exists(TypeUtility.getMethod(m_type, PageWithTableNodePage.METHOD_EXEC_CREATE_CHILD_PAGE));
  }

  public void setType(IType type) {
    m_type = type;
    setOperation(new WellformScoutTypeOperation(type, true));
  }
}
