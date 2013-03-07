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
package org.eclipse.scout.sdk.ui.extensions.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.TargetPackageEntry;
import org.eclipse.scout.sdk.ui.extensions.preferences.IScoutProjectScrolledContent.IModelLoadProgressObserver;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link TargetPackagePreferencePage}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 17.12.2012
 */
public class TargetPackagePreferencePage extends AbstractScoutProjectPreferencePage<TargetPackagePreferenceScrolledContent, TargetPackageModel> {

  public TargetPackagePreferencePage() {
    super(Texts.get("ScoutSDKDefaultPackagePreferences"), TargetPackagePreferenceScrolledContent.class,
        IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SERVER, IScoutBundle.TYPE_SHARED);
  }

  @Override
  protected void loadAllModels(IModelLoadProgressObserver<TargetPackageModel> observer) {
    Set<TargetPackageEntry> defaultPackages = DefaultTargetPackage.getAllDefaults();
    for (Entry<IScoutBundle, TargetPackagePreferenceScrolledContent> e : getProjectModelMap().entrySet()) {
      List<TargetPackageModel> list = new ArrayList<TargetPackageModel>();
      for (TargetPackageEntry entry : defaultPackages) {
        if (e.getKey().getType().equals(entry.getBundleType())) {
          list.add(new TargetPackageModel(entry.getId(), entry.getDefaultSuffix(), e.getKey()));
        }
      }
      Collections.sort(list);

      e.getValue().loadModel(list, observer);
    }
  }

  @Override
  protected int getTotalWork() {
    return DefaultTargetPackage.getAllDefaults().size();
  }
}
