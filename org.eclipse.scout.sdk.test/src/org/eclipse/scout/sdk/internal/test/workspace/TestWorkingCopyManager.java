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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.helper.ScoutProjectHelper;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TestWorkingCopyManager extends AbstractScoutSdkTest {

  @Test
  public void testCommitWorkingCopy() throws Exception {
    try {
      IWorkingCopyManager manager = TypeCacheAccessor.createWorkingCopyManger();
      NullProgressMonitor monitor = new NullProgressMonitor();
      ScoutProjectHelper.setupNewProject("abc", true, true, false);
      IType type = TypeUtility.getType("abc.client.ui.desktop.Desktop");
      ICompilationUnit icu = type.getCompilationUnit();
      Assert.assertFalse(icu.isWorkingCopy());
      manager.register(icu, monitor);
      Assert.assertTrue(icu.isWorkingCopy());
      type = TypeUtility.getType("abc.client.ui.desktop.Desktop");
      type.createType("public class A{}", null, true, monitor);
      manager.unregisterAll(monitor);
      Assert.assertFalse(icu.isWorkingCopy());
      Assert.assertTrue(TypeUtility.exists(type.getType("A")));
    }
    finally {
      clearWorkspace();
    }
  }

  @Test
  public void testDiscardWorkingCopy() throws Exception {
    try {
      NullProgressMonitor monitor = new NullProgressMonitor();
      ScoutProjectHelper.setupNewProject("def", true, true, false);
      IType type = TypeUtility.getType("def.client.ui.desktop.Desktop");
      ICompilationUnit icu = type.getCompilationUnit();
      String source = icu.getSource();
      Assert.assertFalse(icu.isWorkingCopy());
      IWorkingCopyManager manager = TypeCacheAccessor.createWorkingCopyManger();
      manager.register(icu, monitor);
      Assert.assertTrue(icu.isWorkingCopy());
      type = TypeUtility.getType("def.client.ui.desktop.Desktop");
      type.createType("public class A{}", null, true, monitor);
      monitor.setCanceled(true);
      manager.unregisterAll(monitor);
      Assert.assertFalse(icu.isWorkingCopy());
      Assert.assertEquals(source, icu.getSource());
      Assert.assertFalse(TypeUtility.exists(type.getType("A")));
    }
    finally {
      clearWorkspace();
    }
  }

}
