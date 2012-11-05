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

import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestBundleGraph extends AbstractScoutSdkTest {
  @BeforeClass
  public static void setUpWorkspace() throws Exception {
    // workspace containing direct cycles and fragment cycles
    setupWorkspace("bundleGraph",
        "org.bundlegraph.test.client",
        "org.bundlegraph.test.server",
        "org.bundlegraph.test.shared",
        "org.bundlegraph.test.shared.fragment",
        "org.bundlegraph.test.ui.swing");
  }

  @Test
  public final void testCyclicDependencies() throws Exception {
    // will trigger the bundle-graph creation
    ScoutSdkCore.getScoutWorkspace().getRootProjects();
  }

  @AfterClass
  public static void cleanUpWorkspace() throws Exception {
    clearWorkspace();
  }
}
