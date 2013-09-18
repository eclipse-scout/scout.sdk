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
package org.eclipse.scout.sdk;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.sdk.internal.test._SuiteInternalTest;
import org.eclipse.scout.sdk.testing.TestUtility;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({_SuiteInternalTest.class})
public class _SuiteAllTests {

  @BeforeClass
  public static void setupTarget() throws Exception {
    if (System.getProperty("buildingWithTycho") != null) {
      TestUtility.loadRunningOsgiAsTarget("testingTarget", new NullProgressMonitor());
    }
  }
}
