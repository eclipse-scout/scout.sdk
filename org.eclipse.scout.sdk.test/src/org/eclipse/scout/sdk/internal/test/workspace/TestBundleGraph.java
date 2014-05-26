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
package org.eclipse.scout.sdk.internal.test.workspace;

import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.internal.test.Activator;
import org.eclipse.scout.sdk.internal.workspace.ScoutBundleGraph;
import org.eclipse.scout.sdk.testing.TestWorkspaceUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleGraph;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestBundleGraph extends AbstractScoutSdkTest {

  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    // workspace containing direct cycles and fragment cycles
    TestWorkspaceUtility.setupWorkspace(Platform.getBundle(Activator.PLUGIN_ID), "resources/bundleGraph",
        "org.bundlegraph.test.client",
        "org.bundlegraph.test.server",
        "org.bundlegraph.test.shared",
        "org.bundlegraph.test.shared.fragment",
        "org.bundlegraph.test.ui.swing");
  }

  @Test
  public final void testCyclicDependencies() throws Exception {
    // will trigger the bundle-graph creation
    IScoutBundleGraph bundleGraph = ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    bundleGraph.waitFor();
    Set<IScoutBundle> bundles = bundleGraph.getBundles(ScoutBundleFilters.getRootBundlesFilter());
    Assert.assertEquals(5, bundles.size()); // all bundles are root bundles because the cycles are broken by the graph

    ScoutBundleGraph g = (ScoutBundleGraph) ScoutSdkCore.getScoutWorkspace().getBundleGraph();
    Assert.assertEquals(5, g.getDependencyIssues().length);
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    clearWorkspace();
  }
}
