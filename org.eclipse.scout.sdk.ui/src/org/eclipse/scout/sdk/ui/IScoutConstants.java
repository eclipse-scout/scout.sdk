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
package org.eclipse.scout.sdk.ui;

import org.eclipse.scout.sdk.ui.internal.ScoutPerspective;
import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerPart;
import org.eclipse.scout.sdk.ui.internal.view.properties.ScoutPropertyView;

/**
 * <h3>IScoutConstants</h3>
 */
public interface IScoutConstants {
  String SCOUT_PERSPECTIVE_ID = ScoutPerspective.class.getName();
  String SCOUT_EXPLORER_VIEW = ScoutExplorerPart.class.getName();
  String SCOUT_PROPERTY_VIEW = ScoutPropertyView.class.getName();
}
