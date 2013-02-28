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
package org.eclipse.scout.sdk.ui.wizard.project;

import java.beans.PropertyChangeListener;

/**
 *
 */
public interface IScoutProjectWizardPage {
  String PROP_PROJECT_NAME = "projectName";
  String PROP_PROJECT_ALIAS = "projectAlias";
  String PROP_PROJECT_NAME_POSTFIX = "projectNamePostfix";
  String PROP_SELECTED_BUNDLES = "selectedBundles";
  String PROP_USE_DEFAULT_JDT_PREFS = "useDefaultJdtPrefs";

  void addPropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

  boolean isBundleNodesSelected(String... extensionIds);

  boolean hasSelectedBundle(String... types);

  String getProjectName();

  String getProjectAlias();

  String getProjectNamePostfix();

  void setBundleNodeAvailable(boolean available, boolean visible, String... extensionIds);
}
