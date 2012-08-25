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
package org.eclipse.scout.sdk.workspace;

import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.icon.IIconProvider;

public interface IScoutProject extends IScoutContainer {

  /**
   * @return
   */
  IScoutWorkspace getScoutWorkspace();

  String getProjectName();

  /**
   * @return
   */
  IScoutProject[] getSubProjects();

  IScoutProject getParentProject();

  /**
   * @return
   */
  boolean hasParentOrSubProjects();

  IScoutBundle[] getAllScoutBundles();

  IScoutBundle getUiSwingBundle();

  IScoutBundle getUiSwtBundle();

  IScoutBundle getClientBundle();

  IScoutBundle getSharedBundle();

  IScoutBundle getServerBundle();

  INlsProject getNlsProject();

  void clearNlsProjectCache();

  INlsProject getDocsNlsProject();

  /**
   * @return the nls provider of the shared bundle in this project or null if this project
   *         does not contain a shared bundle or the shared bundle does not have a nls support.
   */
  INlsProject findNlsProject();

  /**
   * @return the icon provider of the shared bundle in this project or null if this project
   *         does not contain a shared bundle or the shared bundle does not have an Icon support.
   */
  IIconProvider getIconProvider();

  /**
   * @return the best match icon provider if this project does not have an icon support, it will return
   *         the icon provider of the parent project.
   */
  IIconProvider findIconProvider();

  /**
   * @param type
   * @return
   */
  IScoutBundle[] getAllBundles(int type);

  /**
   * @param filter
   * @return
   */
  IScoutBundle[] getBundles(IScoutBundleFilter filter);

}
