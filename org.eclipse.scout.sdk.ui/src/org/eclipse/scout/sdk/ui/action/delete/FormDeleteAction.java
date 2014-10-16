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
package org.eclipse.scout.sdk.ui.action.delete;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>FormDeleteAction</h3> The ui action to delete a form. If a process service has the same name pattern
 * (e.g. CompanyForm -> (I)CompanyProcessService the user will be asked to delete
 * also the service.
 */
public class FormDeleteAction extends AbstractScoutHandler {

  public FormDeleteAction() {
    super(Texts.get("DeleteForm"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormRemove), "Delete", false, Category.DELETE);
  }
}
