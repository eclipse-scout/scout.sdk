/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.model;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.scout.sdk.core.CoreTestingUtils;
import org.eclipse.scout.sdk.core.fixture.ChildClass;
import org.eclipse.scout.sdk.core.signature.Signature;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class PackageTest {

  private static final String FIXTURE_PACKAGE = Signature.getQualifier(ChildClass.class.getName());

  @Test
  public void testPackageName() {
    ICompilationUnit childClassIcu = CoreTestingUtils.getChildClassIcu();
    Assert.assertNotNull(childClassIcu);

    IPackage pck = childClassIcu.getPackage();
    Assert.assertNotNull(pck);

    Assert.assertEquals(FIXTURE_PACKAGE, pck.getName());
    Assert.assertEquals("package " + FIXTURE_PACKAGE, pck.toString());
  }

  @Test
  public void testToString() {
    ICompilationUnit childClassIcu = CoreTestingUtils.getChildClassIcu();
    Assert.assertNotNull(childClassIcu);

    IPackage pck = childClassIcu.getPackage();
    Assert.assertFalse(StringUtils.isBlank(pck.toString()));
  }

  @Test
  public void testPackageNameFromType() {
    IType childClass = CoreTestingUtils.getChildClassType();
    Assert.assertNotNull(childClass);

    IPackage pck = childClass.getPackage();
    Assert.assertNotNull(pck);

    Assert.assertEquals(FIXTURE_PACKAGE, pck.getName());
    Assert.assertEquals("package " + FIXTURE_PACKAGE, pck.toString());
  }

  @Test
  public void testPackageNameFromSuperType() {
    IType childClass = CoreTestingUtils.getBaseClassType();
    Assert.assertNotNull(childClass);

    IPackage pck = childClass.getPackage();
    Assert.assertNotNull(pck);

    Assert.assertEquals(FIXTURE_PACKAGE, pck.getName());
    Assert.assertEquals("package " + FIXTURE_PACKAGE, pck.toString());
  }
}
