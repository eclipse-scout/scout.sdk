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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project;

import org.eclipse.scout.sdk.ui.internal.view.outline.ScoutExplorerSettingsSupport;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;

/**
 * <h3>{@link ScoutExplorerSettingsBundleFilter}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 21.03.2013
 */
public final class ScoutExplorerSettingsBundleFilter implements IScoutBundleFilter {

  private static final IScoutBundleFilter INSTANCE = new ScoutExplorerSettingsBundleFilter();

  private ScoutExplorerSettingsBundleFilter() {
  }

  public static IScoutBundleFilter get() {
    return INSTANCE;
  }

  @Override
  public boolean accept(IScoutBundle bundle) {
    if (bundle == null) {
      return false;
    }
    return acceptBinaryBundleFilter(bundle) && acceptFragmentFilter(bundle) && acceptBundleTypesFilter(bundle);
  }

  private boolean acceptBundleTypesFilter(IScoutBundle bundle) {
    return !ScoutExplorerSettingsSupport.get().isBundleTypeHidden(bundle.getType());
  }

  private boolean acceptBinaryBundleFilter(IScoutBundle bundle) {
    if (!bundle.isBinary()) {
      return true;
    }
    return ScoutExplorerSettingsSupport.get().isShowBinaryBundles();
  }

  private boolean acceptFragmentFilter(IScoutBundle bundle) {
    if (!bundle.isFragment()) {
      return true;
    }
    return ScoutExplorerSettingsSupport.get().isShowFragments();
  }
}
