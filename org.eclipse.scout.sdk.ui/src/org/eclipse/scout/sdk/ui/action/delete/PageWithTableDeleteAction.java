/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
 * <h3>{@link PageWithTableDeleteAction}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 16.10.2014
 */
public class PageWithTableDeleteAction extends AbstractScoutHandler {

  public PageWithTableDeleteAction() {
    super(Texts.get("DeletePage"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.PageRemove), "Delete", true, Category.DELETE);
  }
}
