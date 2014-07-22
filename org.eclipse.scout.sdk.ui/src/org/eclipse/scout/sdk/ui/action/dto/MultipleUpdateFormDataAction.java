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
package org.eclipse.scout.sdk.ui.action.dto;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractOperationAction;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>{@link MultipleUpdateFormDataAction}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 03.03.2011
 */
public class MultipleUpdateFormDataAction extends AbstractOperationAction {

  public MultipleUpdateFormDataAction() {
    super(Texts.get("UpdateAllFormdatas"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolLoading), null, false, Category.UDPATE);
  }

  @Override
  public boolean isVisible() {
    return getOperationCount() > 0;
  }
}
