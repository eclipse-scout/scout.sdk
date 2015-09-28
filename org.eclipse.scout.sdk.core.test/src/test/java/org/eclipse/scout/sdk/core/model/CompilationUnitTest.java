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
import org.eclipse.scout.sdk.core.fixture.BaseClass;
import org.eclipse.scout.sdk.core.fixture.Long;
import org.eclipse.scout.sdk.core.model.api.ICompilationUnit;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class CompilationUnitTest {
  @Test
  public void testIcu() {
    ICompilationUnit baseClassIcu = CoreTestingUtils.getBaseClassIcu();
    Assert.assertNotNull(baseClassIcu);
    Assert.assertNotNull(baseClassIcu.javaEnvironment());
    Assert.assertEquals(BaseClass.class.getName(), baseClassIcu.mainType().name());

    Assert.assertEquals(5, baseClassIcu.imports().size());
    Assert.assertEquals(1, baseClassIcu.types().list().size());
    Assert.assertEquals(2, baseClassIcu.types().first().innerTypes().list().size());
  }

  @Test
  public void testFindTypeBySimpleName() {
    ICompilationUnit baseClassIcu = CoreTestingUtils.getBaseClassIcu();
    IType sdkLong = baseClassIcu.resolveTypeBySimpleName(Long.class.getSimpleName());

    Assert.assertNotNull(sdkLong);
    Assert.assertEquals(Long.class.getName(), sdkLong.name());
  }

  @Test
  public void testToString() {
    ICompilationUnit baseClassIcu = CoreTestingUtils.getBaseClassIcu();
    Assert.assertFalse(StringUtils.isBlank(baseClassIcu.toString()));
  }
}
