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

import org.eclipse.scout.sdk.core.fixture.AnnotationQueryTestFixture;
import org.eclipse.scout.sdk.core.fixture.AnnotationQueryTestFixture.TestChildClass;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link AnnotationQueryTest}</h3>
 *
 * @author Matthias Villiger
 * @since 5.2.0
 */
public class AnnotationQueryTest {
  @Test
  public void testAllSuperTypes() {
    IType testChildClass = CoreTestingUtils.createJavaEnvironment().findType(AnnotationQueryTestFixture.class.getName() + '$' + TestChildClass.class.getSimpleName());
    Assert.assertNotNull(testChildClass);
    Assert.assertEquals(4, testChildClass.methods().first().annotations().withSuperTypes(true).list().size());
  }

  @Test
  public void testSuperClasses() {
    IType testChildClass = CoreTestingUtils.createJavaEnvironment().findType(AnnotationQueryTestFixture.class.getName() + '$' + TestChildClass.class.getSimpleName());
    Assert.assertNotNull(testChildClass);
    Assert.assertEquals(3, testChildClass.methods().first().annotations().withSuperClasses(true).list().size());
  }

  @Test
  public void testSuperInterfaces() {
    IType testChildClass = CoreTestingUtils.createJavaEnvironment().findType(AnnotationQueryTestFixture.class.getName() + '$' + TestChildClass.class.getSimpleName());
    Assert.assertNotNull(testChildClass);
    Assert.assertEquals(3, testChildClass.methods().first().annotations().withSuperInterfaces(true).list().size());
  }
}
