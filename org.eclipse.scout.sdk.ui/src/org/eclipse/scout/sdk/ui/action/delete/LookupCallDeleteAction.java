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
 * <h3>LookupCallDeleteAction</h3> The ui action to delete a lookup call. If a process service has the same name pattern
 * (e.g. CompanyLookupCall -> (I)CompanyLookupService the user will be asked to delete
 * also the service.
 */
public class LookupCallDeleteAction extends AbstractScoutHandler {

  public LookupCallDeleteAction() {
    super(Texts.get("DeleteLookupCall") + "...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.LookupCallRemove), "Delete", false, Category.DELETE);
  }
}
