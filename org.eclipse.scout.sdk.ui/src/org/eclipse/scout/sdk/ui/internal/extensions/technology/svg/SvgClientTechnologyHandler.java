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
package org.eclipse.scout.sdk.ui.internal.extensions.technology.svg;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class SvgClientTechnologyHandler extends AbstractScoutTechnologyHandler {

  private final static String CLIENT_SVG_BUNDLE_NAME = "org.eclipse.scout.svg.client";

  public final static String[] COMMON_SVG_PLUGINS = new String[]{
      "org.apache.batik.bridge",
      "org.apache.batik.css",
      "org.apache.batik.dom",
      "org.apache.batik.dom.svg",
      "org.apache.batik.ext.awt",
      "org.apache.batik.parser",
      "org.apache.batik.svggen",
      "org.apache.batik.swing",
      "org.apache.batik.transcoder",
      "org.apache.batik.util",
      "org.apache.batik.util.gui",
      "org.apache.batik.xml",
      CLIENT_SVG_BUNDLE_NAME,
      "org.w3c.css.sac",
      "org.w3c.dom.smil",
      "org.w3c.dom.svg"};

  public SvgClientTechnologyHandler() {
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedManifest(resources, selected, CLIENT_SVG_BUNDLE_NAME);
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(RuntimeClasses.IFormField)).invalidate();
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false) != null;
  }

  @Override
  public TriState getSelection(IScoutBundle project) {
    return getSelectionManifests(getClientBundlesBelow(project), CLIENT_SVG_BUNDLE_NAME);
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) {
    contributeManifestFiles(getClientBundlesBelow(project), list);
  }

  private IScoutBundle[] getClientBundlesBelow(IScoutBundle start) {
    return start.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), true);
  }
}
