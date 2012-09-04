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

import org.eclipse.scout.sdk.ui.extensions.project.IScoutBundleExtension.BundleTypes;

/**
 *
 */
public interface IScoutProjectWizardPage {
  public static final String PROP_PROJECT_NAME = "projectName";
  public static final String PROP_PROJECT_ALIAS = "projectAlias";
  public static final String PROP_PROJECT_NAME_POSTFIX = "projectNamePostfix";
  public static final String PROP_SELECTED_BUNDLES = "selectedBundles";

  void addPropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * @param extensionIds
   * @return
   */
  boolean isBundleNodesSelected(String... extensionIds);

  boolean hasSelectedBundle(BundleTypes... types);

  String getProjectName();

  String getProjectAlias();

  String getProjectNamePostfix();

  /**
   * @param available
   * @param extensionIds
   */
  void setBundleNodeAvailable(boolean available, String... extensionIds);
}
