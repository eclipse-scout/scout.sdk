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
package org.eclipse.scout.sdk.internal.test.types;

import static org.junit.Assert.assertFalse;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.helper.ScoutProjectHelper;
import org.eclipse.scout.sdk.internal.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.testing.SdkAssert;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TypeCacheTest extends AbstractScoutSdkTest {

  @Test
  public void testTypeExistanceAfterClearWorkspace() throws Exception {
    try {
      ScoutProjectHelper.setupNewProject("abc.test", true, true, true);
      IType iform01 = TypeUtility.getType(RuntimeClasses.IForm);
      SdkAssert.assertExist(iform01);
      clearWorkspace();
      IType iform02 = TypeUtility.getType(RuntimeClasses.IForm);
      assertFalse(TypeUtility.exists(iform02));
      assertFalse(iform01.equals(iform02));
    }
    finally {
      clearWorkspace();
    }
  }

  @Test
  public void testTypeExistanceOverTwoProjects() throws Exception {
    try {
      IScoutBundle project = ScoutProjectHelper.setupNewProject("abc.test", true, true, true);
      IType iform01 = SdkAssert.assertTypeExists(RuntimeClasses.IForm);
      Assert.assertEquals(project.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false).getSymbolicName(), iform01.getJavaProject().getElementName());
      clearWorkspace();
      IScoutBundle project2 = ScoutProjectHelper.setupNewProject("zyx.test", true, true, true);
      IType iform02 = SdkAssert.assertTypeExists(RuntimeClasses.IForm);
      Assert.assertEquals(project2.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false).getSymbolicName(), iform02.getJavaProject().getElementName());
      // how funny, project are not equals, but types are equals (IType.equals compares only project names!!!)
      Assert.assertFalse(iform01.getJavaProject().equals(iform02.getJavaProject()));
      Assert.assertTrue(iform01.equals(iform02));
    }
    finally {
      clearWorkspace();
    }
  }

}
