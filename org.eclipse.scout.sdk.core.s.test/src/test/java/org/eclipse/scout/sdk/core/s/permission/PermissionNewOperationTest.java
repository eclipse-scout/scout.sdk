/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.permission;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link PermissionNewOperationTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class PermissionNewOperationTest {

  @Test
  public void testPermissionCreation(TestingEnvironment env) {
    var scoutApi = env.primaryEnvironment().requireApi(IScoutApi.class);

    var op = new PermissionNewOperation();
    op.setPackage("org.eclipse.scout.sdk.s2e.shared.test");
    op.setPermissionName("My" + ISdkConstants.SUFFIX_PERMISSION);
    op.setSharedSourceFolder(env.primarySourceFolder());
    op.setSuperType(scoutApi.AbstractPermission().fqn());
    env.run(op);
    assertNotNull(op.getCreatedPermission());
  }
}
