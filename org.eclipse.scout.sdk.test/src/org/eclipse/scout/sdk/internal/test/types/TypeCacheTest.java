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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.helper.ScoutProjectHelper;
import org.eclipse.scout.sdk.test.AbstractScoutSdkTest;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TypeCacheTest extends AbstractScoutSdkTest {

  @Test
  public void testTypeExistanceAfterClearWorkspace() throws Exception {
    ScoutProjectHelper.setupNewProject("abc.test", true, true, true);
    IType iform01 = TypeUtility.getType(RuntimeClasses.IForm);
    Assert.assertTrue(TypeUtility.exists(iform01));
    clearWorkspace();
    IType iform02 = TypeUtility.getType(RuntimeClasses.IForm);
    assertFalse(TypeUtility.exists(iform02));
    assertFalse(iform01.equals(iform02));
  }

  @Test
  public void testTypeExistanceOverTwoProjects() throws Exception {
    IScoutProject project = ScoutProjectHelper.setupNewProject("abc.test", true, true, true);
    IType iform01 = TypeUtility.getType(RuntimeClasses.IForm);
    Assert.assertTrue(TypeUtility.exists(iform01));
    Assert.assertEquals(project.getClientBundle().getBundleName(), iform01.getJavaProject().getElementName());
    clearWorkspace();
    IScoutProject project2 = ScoutProjectHelper.setupNewProject("zyx.test", true, true, true);
    IType iform02 = TypeUtility.getType(RuntimeClasses.IForm);
    Assert.assertTrue(TypeUtility.exists(iform02));
    Assert.assertEquals(project2.getClientBundle().getBundleName(), iform02.getJavaProject().getElementName());
    // how funny, project are not equals, but types are equals (IType.equals compares only project names!!!)
    Assert.assertFalse(iform01.getJavaProject().equals(iform02.getJavaProject()));
    Assert.assertTrue(iform01.equals(iform02));
  }

}
