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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ui.extensions.technology.AbstractScoutTechnologyHandler;
import org.eclipse.scout.sdk.ui.extensions.technology.IScoutTechnologyResource;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

public class SvgClientTechnologyHandler extends AbstractScoutTechnologyHandler {

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
      "org.eclipse.scout.svg.client",
      "org.w3c.css.sac",
      "org.w3c.dom.smil",
      "org.w3c.dom.events",
      "org.w3c.dom.svg"};

  public SvgClientTechnologyHandler() {
  }

  @Override
  public void selectionChanged(IScoutTechnologyResource[] resources, boolean selected, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    selectionChangedManifest(resources, selected, RuntimeClasses.ScoutClientSvgBundleId);
  }

  @Override
  public void postSelectionChanged(boolean selected, IProgressMonitor monitor) throws CoreException {
    TypeUtility.getPrimaryTypeHierarchy(TypeUtility.getType(RuntimeClasses.IFormField)).invalidate();
  }

  @Override
  public boolean isActive(IScoutProject project) {
    return project.getClientBundle() != null && project.getClientBundle().getProject().exists();
  }

  @Override
  public TriState getSelection(IScoutProject project) {
    return getSelectionManifest(project.getClientBundle(), RuntimeClasses.ScoutClientSvgBundleId);
  }

  @Override
  protected void contributeResources(IScoutProject project, List<IScoutTechnologyResource> list) {
    contributeManifestFile(project.getClientBundle(), list);
  }
}
