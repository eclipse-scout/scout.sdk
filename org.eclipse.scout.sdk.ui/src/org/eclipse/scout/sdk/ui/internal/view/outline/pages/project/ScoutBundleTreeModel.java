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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.bundles.RuntimeBundles;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleExtensionPoint;
import org.eclipse.scout.sdk.ui.extensions.bundle.ScoutBundleUiExtension;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleComparators;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;

/**
 * <h3>{@link ScoutBundleTreeModel}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 11.02.2013
 */
public class ScoutBundleTreeModel {

  private Set<ScoutBundleNodeGroup> m_model;

  public void build() {
    m_model = buildProjectGraph();
  }

  public ScoutBundleNodeGroup[] getRoots() {
    return m_model.toArray(new ScoutBundleNodeGroup[m_model.size()]);
  }

  private ScoutBundleNodeGroup createProjects(IScoutBundle bundle, String curType) {
    if (curType.equals(bundle.getType()) && ScoutExplorerSettingsBundleFilter.get().accept(bundle)) {
      ScoutBundleUiExtension extension = ScoutBundleExtensionPoint.getExtension(bundle);
      if (extension != null) {
        ScoutBundleNodeGroup sbg = new ScoutBundleNodeGroup(new ScoutBundleNode(bundle, extension));
        for (IScoutBundle child : bundle.getDirectChildBundles()) {
          if (ScoutExplorerSettingsBundleFilter.get().accept(child)) {
            ScoutBundleNodeGroup childProject = createProjects(child, curType);
            if (childProject == null) {
              ScoutBundleUiExtension childExt = ScoutBundleExtensionPoint.getExtension(child);
              if (childExt != null) {
                sbg.addChildBundle(new ScoutBundleNode(child, childExt));
              }
            }
            else {
              sbg.addChildGroup(childProject);
            }
          }
        }
        return sbg;
      }
    }
    return null;
  }

  private void removeImplicitChildren(ScoutBundleNodeGroup scoutProject) {
    for (ScoutBundleNode node : scoutProject.getChildBundles()) {
      Iterator<ScoutBundleNode> iterator = node.getChildBundles().iterator();
      while (iterator.hasNext()) {
        ScoutBundleNode childNode = iterator.next();

        if (scoutProject.containsBundle(childNode)) {
          iterator.remove();
        }
      }
    }
    for (ScoutBundleNodeGroup childGroup : scoutProject.getChildGroups()) {
      removeImplicitChildren(childGroup);
    }
  }

  private Set<ScoutBundleNodeGroup> buildProjectGraph() {
    Set<ScoutBundleNodeGroup> scoutProjects = new TreeSet<ScoutBundleNodeGroup>();
    List<String> types = RuntimeBundles.getTypes();

    for (int i = types.size() - 1; i >= 0; i--) {
      // create projects recursively
      for (IScoutBundle root : ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(
          ScoutBundleFilters.getFilteredRootBundlesFilter(ScoutExplorerSettingsBundleFilter.get()), ScoutBundleComparators.getSymbolicNameAscComparator())) {
        ScoutBundleNodeGroup sbg = createProjects(root, types.get(i));
        if (sbg != null) {
          scoutProjects.add(sbg);
        }
      }
    }

    // remove implicit children
    for (ScoutBundleNodeGroup rootGroup : scoutProjects) {
      removeImplicitChildren(rootGroup);
    }

    return scoutProjects;
  }
}
