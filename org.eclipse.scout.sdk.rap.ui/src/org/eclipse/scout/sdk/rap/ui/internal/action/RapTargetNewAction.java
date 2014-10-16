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
package org.eclipse.scout.sdk.rap.ui.internal.action;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>{@link RapTargetNewAction}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 15.04.2013
 */
public class RapTargetNewAction extends AbstractScoutHandler {
  public RapTargetNewAction() {
    super(Texts.get("CreateNewRAPTarget") + "...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolAdd), null, false, Category.NEW);
  }
}
