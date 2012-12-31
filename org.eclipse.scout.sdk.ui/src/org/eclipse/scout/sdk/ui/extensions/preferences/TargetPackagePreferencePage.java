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
import org.eclipse.scout.sdk.ui.extensions.preferences.IScoutProjectScrolledContent.IModelLoadProgressObserver;
import org.eclipse.scout.sdk.workspace.DefaultTargetPackage;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link TargetPackagePreferencePage}</h3> ...
 * 
 * @author mvi
 * @since 3.9.0 17.12.2012
 */
public class TargetPackagePreferencePage extends AbstractScoutProjectPreferencePage<TargetPackagePreferenceScrolledContent, TargetPackageModel> {

  public TargetPackagePreferencePage() {
    super(Texts.get("ScoutSDKSuperTypePreferences"), TargetPackagePreferenceScrolledContent.class);
  }

  @Override
  protected void loadAllModels(IModelLoadProgressObserver<TargetPackageModel> observer) {
    Set<Entry<String, String>> defaultPackages = DefaultTargetPackage.getAllDefaults().entrySet();
    for (Entry<IScoutProject, TargetPackagePreferenceScrolledContent> e : getProjectModelMap().entrySet()) {
      List<TargetPackageModel> list = new ArrayList<TargetPackageModel>();
      for (Entry<String, String> entry : defaultPackages) {
        list.add(new TargetPackageModel(entry.getKey(), entry.getValue(), e.getKey()));
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
