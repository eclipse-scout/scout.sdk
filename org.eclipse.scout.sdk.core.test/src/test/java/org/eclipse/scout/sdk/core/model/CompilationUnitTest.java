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
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.Long;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CompilationUnitTest {
  @Test
  public void testIcu() throws Exception {
    ICompilationUnit baseClassIcu = CoreTestingUtils.getBaseClassIcu();
    Assert.assertNotNull(baseClassIcu);
    Assert.assertNotNull(baseClassIcu.getLookupEnvironment());
    Assert.assertEquals(BaseClass.class.getName(), baseClassIcu.getMainType().getName());

    Assert.assertEquals(5, baseClassIcu.getImports().size());
    Assert.assertEquals(1, baseClassIcu.getTypes().size());
    Assert.assertEquals(2, baseClassIcu.getTypes().get(0).getTypes().size());
  }

  @Test
  public void testFindTypeBySimpleName() throws Exception {
    ICompilationUnit baseClassIcu = CoreTestingUtils.getBaseClassIcu();
    IType sdkLong = baseClassIcu.findTypeBySimpleName(Long.class.getSimpleName());

    Assert.assertNotNull(sdkLong);
    Assert.assertEquals(Long.class.getName(), sdkLong.getName());
  }

  @Test
  public void testToString() throws Exception {
    ICompilationUnit baseClassIcu = CoreTestingUtils.getBaseClassIcu();
    Assert.assertFalse(StringUtils.isBlank(baseClassIcu.toString()));
  }
}
