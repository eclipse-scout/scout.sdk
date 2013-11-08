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
package org.eclipse.scout.sdk.ui.extensions.bundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.scout.sdk.ui.internal.view.properties.model.links.LinksPresenterModel;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.ProductLaunchPresenter;

/**
 * <h3>{@link IProductLauncherContributor}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 09.09.2013
 */
public interface IProductLauncherContributor {
  /**
   * Callback to contribute bundle type specific additional links in the product launch presenter.
   * 
   * @param model
   * @throws CoreException
   * @see ProductLaunchPresenter
   */
  void contributeLinks(IFile productFile, LinksPresenterModel model) throws CoreException;

  /**
   * Called when the launch state of the presenter changes. E.g. when stopping a running application.
   * 
   * @param mode
   *          The new state. See {@link ProductLaunchPresenter} and {@link ILaunchManager} for possible constant values.
   */
  void refreshLaunchState(String mode);
}
