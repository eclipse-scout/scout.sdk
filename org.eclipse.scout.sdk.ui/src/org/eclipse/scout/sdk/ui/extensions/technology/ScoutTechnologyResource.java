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
package org.eclipse.scout.sdk.ui.extensions.technology;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * <h3>{@link ScoutTechnologyResource}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 11.02.2012
 */
public class ScoutTechnologyResource implements IScoutTechnologyResource {

  private final IScoutBundle m_bundle;
  private final IFile m_resource;
  private IScoutTechnologyHandler m_handler;
  private final boolean m_defaultSelection;

  public ScoutTechnologyResource(IScoutBundle bundle, IFile resource) {
    this(bundle, resource, true);
  }

  public ScoutTechnologyResource(IScoutBundle bundle, IFile resource, boolean defaultSelection) {
    m_bundle = bundle;
    m_resource = resource;
    m_defaultSelection = defaultSelection;
  }

  @Override
  public boolean getDefaultSelection() {
    return m_defaultSelection;
  }

  @Override
  public IScoutBundle getBundle() {
    return m_bundle;
  }

  @Override
  public ImageDescriptor getBundleImage() {
    switch (getBundle().getType()) {
      case IScoutBundle.BUNDLE_CLIENT: {
        return ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ClientBundle);
      }
      case IScoutBundle.BUNDLE_SERVER: {
        return ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServerBundle);
      }
      case IScoutBundle.BUNDLE_SHARED: {
        return ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SharedBundle);
      }
      default: {
        return ScoutSdkUi.getImageDescriptor(ScoutSdkUi.SwingBundle);
      }
    }
  }

  @Override
  public void setHandler(IScoutTechnologyHandler handler) {
    m_handler = handler;
  }

  @Override
  public IScoutTechnologyHandler getHandler() {
    return m_handler;
  }

  @Override
  public IFile getResource() {
    return m_resource;
  }
}
