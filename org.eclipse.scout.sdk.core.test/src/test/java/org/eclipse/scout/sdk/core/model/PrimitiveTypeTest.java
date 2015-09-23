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

import org.eclipse.scout.sdk.core.TypeNames;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.util.JavaEnvironmentBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * <h3>{@link PrimitiveTypeTest}</h3>
 * <p>
 *
 * @author imo
 * @since 5.1.0
 */
public class PrimitiveTypeTest {

  @Test
  public void testExistence() {
    IJavaEnvironment env = new JavaEnvironmentBuilder()
        .withExcludeScoutSdk()
        .build();

    Assert.assertNotNull(env.findType(TypeNames._short));
    Assert.assertNotNull(env.findType(TypeNames._int));
    Assert.assertNotNull(env.findType(TypeNames._float));
    Assert.assertNotNull(env.findType(TypeNames._long));
    Assert.assertNotNull(env.findType(TypeNames._double));
    Assert.assertNotNull(env.findType(TypeNames._boolean));
    Assert.assertNotNull(env.findType(TypeNames._char));
    Assert.assertNotNull(env.findType(TypeNames._byte));
    Assert.assertNotNull(env.findType(TypeNames._void));

    Assert.assertNull(env.findType("null"));
  }
}
