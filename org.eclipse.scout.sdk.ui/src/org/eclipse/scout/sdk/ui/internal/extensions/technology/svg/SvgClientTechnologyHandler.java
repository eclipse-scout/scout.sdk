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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.project.add.ScoutProjectAddOperation;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

public class SvgClientTechnologyHandler extends AbstractScoutTechnologyHandler {

  public static final String[] SCOUT_ONLY_SVG_PLUGINS = new String[]{
      "org.apache.batik.bridge",
      "org.apache.batik.dom",
      "org.apache.batik.dom.svg",
      "org.apache.batik.ext.awt",
      "org.apache.batik.parser",
      "org.apache.batik.svggen",
      "org.apache.batik.swing",
      "org.apache.batik.transcoder",
      "org.apache.batik.xml",
      ScoutProjectAddOperation.CLIENT_SVG_BUNDLE_NAME
  };

  public static final String[] CORE_SVG_PLUGINS = new String[]{
      "org.apache.batik.css",
      "org.apache.batik.util",
      "org.apache.batik.util.gui",
      "org.w3c.dom.svg",
      "org.w3c.dom.smil",
      "org.w3c.css.sac"
  };

  private static final String[] BATIK_17_CORE_SVG_PLUGINS = new String[]{
      "org.w3c.dom.events"
  };

  private static final String[] BATIK_17_SCOUT_ONLY_SVG_PLUGINS = new String[]{
      "org.eclipse.scout.org.w3c.dom.svg.fragment"
  };

  public SvgClientTechnologyHandler() {
  }

  public static String[] getAdditionalBatik17ScoutPlugins() {
    if (JdtUtility.isBatik17OrNewer()) {
      return Arrays.copyOf(BATIK_17_SCOUT_ONLY_SVG_PLUGINS, BATIK_17_SCOUT_ONLY_SVG_PLUGINS.length);
    }
    return null;
  }

  public static String[] getAdditionalBatik17CorePlugins() {
    if (JdtUtility.isBatik17OrNewer()) {
      return Arrays.copyOf(BATIK_17_CORE_SVG_PLUGINS, BATIK_17_CORE_SVG_PLUGINS.length);
    }
    return null;
  }

  @Override
  public void selectionChanged(Set<IScoutTechnologyResource> resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedManifest(resources, selected, ScoutProjectAddOperation.CLIENT_SVG_BUNDLE_NAME);
    selectionChangedManifestImportPackage(resources, selected, new String[]{ScoutProjectAddOperation.W3C_DOM_SVG_PACKAGE}, new String[]{"[1.1.0,2.0.0)"});
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(IRuntimeClasses.IFormField)).invalidate();
  }

  @Override
  public boolean isActive(IScoutBundle project) {
    return project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false) != null;
  }

  @Override
  public TriState getSelection(IScoutBundle project) {
    IScoutBundle[] clientBundlesBelow = getClientBundlesBelow(project);
    TriState t1 = getSelectionManifests(clientBundlesBelow, ScoutProjectAddOperation.CLIENT_SVG_BUNDLE_NAME);
    TriState t2 = getSelectionManifestsImportPackage(clientBundlesBelow, ScoutProjectAddOperation.W3C_DOM_SVG_PACKAGE);
    if (t1.equals(t2)) {
      return t1;
    }
    else {
      return TriState.UNDEFINED;
    }
  }

  @Override
  protected void contributeResources(IScoutBundle project, List<IScoutTechnologyResource> list) {
    contributeManifestFiles(getClientBundlesBelow(project), list);
  }

  private IScoutBundle[] getClientBundlesBelow(IScoutBundle start) {
    return start.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), true);
  }
}
