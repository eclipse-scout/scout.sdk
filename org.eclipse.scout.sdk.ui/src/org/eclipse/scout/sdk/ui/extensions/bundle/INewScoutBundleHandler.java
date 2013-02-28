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
package org.eclipse.scout.sdk.ui.extensions.bundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;
import org.eclipse.scout.sdk.util.PropertyMap;

/**
 * <h3>{@link INewScoutBundleHandler}</h3> Controls the behavior when a new bundle of a certain type should be created.
 * Classes of this type are contributed using the extension point 'org.eclipse.scout.sdk.ui.scoutBundle'.
 * 
 * @author mvi
 * @since 3.9.0 27.02.2013
 */
public interface INewScoutBundleHandler {

  /**
   * invoked when the user checks or unchecks a bundle node in the wizard. Allows e.g. to add other wizard pages.
   * 
   * @param wizard
   *          The wizard in which the event occured.
   * @param selected
   *          The new selection
   */
  void bundleSelectionChanged(IScoutProjectWizard wizard, boolean selected);

  /**
   * Called during validation. Allows to contribute messages or validation checks.
   * 
   * @param wizard
   *          the wizard that is validating.
   * @return the status
   */
  IStatus getStatus(IScoutProjectWizard wizard);

  /**
   * Called during initialization of the wizard. Allows e.g. to hide/show nodes based on the selection of the wizard.
   * 
   * @param wizard
   *          the wizard that is starting.
   * @param extension
   *          the ui extension that belongs to this node.
   */
  void init(IScoutProjectWizard wizard, ScoutBundleUiExtension extension);

  /**
   * Called before the bundle creation operation starts. Allows to pass properties to the operation.
   * 
   * @param wizard
   *          The wizard that wants to create bundles.
   * @param properties
   *          the current properties (may be modified)
   */
  void putProperties(IScoutProjectWizard wizard, PropertyMap properties);
}
