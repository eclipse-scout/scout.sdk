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
package org.eclipse.scout.sdk.operation.project;

import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link ScoutBundleNewOperation}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 02.03.2012
 */
public class ScoutBundleNewOperation extends ScoutProjectNewOperation {

  private final IScoutProject m_project;

  public ScoutBundleNewOperation(IScoutProject project) {
    m_project = project;
  }

  private void putExistingProjectProperties() {
    IScoutBundle uiSwing = m_project.getUiSwingBundle();

    IScoutBundle client = m_project.getClientBundle();
    if (client != null) {
      getProperties().setProperty(CreateClientPluginOperation.PROP_BUNDLE_CLIENT_NAME, client.getProject().getName());
      addCreatedBundle(client.getJavaProject());
    }

    IScoutBundle shared = m_project.getSharedBundle();
    if (shared != null) {
      getProperties().setProperty(CreateSharedPluginOperation.PROP_BUNDLE_SHARED_NAME, shared.getProject().getName());
      addCreatedBundle(shared.getJavaProject());
    }

    IScoutBundle server = m_project.getServerBundle();
    if (server != null) {
      getProperties().setProperty(CreateServerPluginOperation.PROP_BUNDLE_SERVER_NAME, server.getProject().getName());
      addCreatedBundle(server.getJavaProject());
    }
    if (uiSwing != null) {
      getProperties().setProperty(CreateUiSwingPluginOperation.PROP_BUNDLE_SWING_NAME, uiSwing.getProject().getName());
      addCreatedBundle(uiSwing.getJavaProject());
    }

    IScoutBundle uiSwt = m_project.getUiSwtBundle();
    if (uiSwt != null) {
      getProperties().setProperty(CreateUiSwtPluginOperation.PROP_BUNDLE_SWT_NAME, uiSwt.getProject().getName());
      addCreatedBundle(uiSwt.getJavaProject());
    }
  }

  @Override
  protected void putInitialProperties() {
    super.putInitialProperties();
    putExistingProjectProperties();
  }
}
