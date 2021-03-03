/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.scout.sdk.core.s.testing.AbstractBooleanPermutationArgumentsProvider;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * <h3>{@link EntityNewOperationTest}</h3>
 */
@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWithTestingEnvironment(
    primary = @ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class),
    dto = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class EntityNewOperationTest {

  @DisplayName("Test Entity Creation")
  @ArgumentsSource(EntityTestArgumentsProvider.class)
  @ParameterizedTest(name = "withClientSourceFolder={0}, withSharedSourceFolder={1}, withServerSourceFolder={2}, withTestSourceFolder={3}")
  public void testEntityCreation(boolean withClientSourceFolder, boolean withSharedSourceFolder, boolean withServerSourceFolder, boolean withTestSourceFolder, TestingEnvironment env) {
    var srcFolder = env.primarySourceFolder();

    var eno = new EntityNewOperation();
    eno.setEntityName("My");
    eno.setClientPackage("org.eclipse.scout.sdk.s2e.client.test");
    if (withClientSourceFolder) {
      eno.setClientSourceFolder(srcFolder);
      eno.setClientTestSourceFolder(withTestSourceFolder ? srcFolder : null);
    }
    if (withSharedSourceFolder) {
      eno.setSharedSourceFolder(srcFolder);
      eno.setSharedTestSourceFolder(withTestSourceFolder ? srcFolder : null);
    }
    if (withServerSourceFolder) {
      eno.setServerSourceFolder(srcFolder);
      eno.setServerTestSourceFolder(withTestSourceFolder ? srcFolder : null);
    }

    env.run(eno);

    assertNotNull(eno.getFormNewOperation());
    assertNotNull(eno.getPageNewOperation());

    if (withClientSourceFolder) {
      assertNotNull(eno.getFormNewOperation().getCreatedForm());
      assertNotNull(eno.getPageNewOperation().getCreatedPage());
    }
  }

  private static class EntityTestArgumentsProvider extends AbstractBooleanPermutationArgumentsProvider {
    protected EntityTestArgumentsProvider() {
      super(4);
    }
  }
}
