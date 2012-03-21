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
package org.eclipse.scout.sdk.ui.viewer;

import org.eclipse.scout.sdk.ui.fields.proposal.styled.SearchRangeStyledLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link ScoutBundleLableProvider}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 19.03.2012
 */
public class ScoutBundleLableProvider extends SearchRangeStyledLabelProvider {

  @Override
  public String getText(Object element) {
    return ((IScoutBundle) element).getBundleName();
  }

  @Override
  public Image getImage(Object element) {
    switch (((IScoutBundle) element).getType()) {
      case IScoutBundle.BUNDLE_UI_SWING:
        return ScoutSdkUi.getImage(ScoutSdkUi.SwingBundle);
      case IScoutBundle.BUNDLE_UI_SWT:
        return ScoutSdkUi.getImage(ScoutSdkUi.SwtBundle);
      case IScoutBundle.BUNDLE_CLIENT:
        return ScoutSdkUi.getImage(ScoutSdkUi.ClientBundle);
      case IScoutBundle.BUNDLE_SHARED:
        return ScoutSdkUi.getImage(ScoutSdkUi.SharedBundle);
      case IScoutBundle.BUNDLE_SERVER:
        return ScoutSdkUi.getImage(ScoutSdkUi.ServerBundle);
      default:
        // TODO default bundle img
        return ScoutSdkUi.getImage(ScoutSdkUi.SharedBundle);
    }

  }
}
