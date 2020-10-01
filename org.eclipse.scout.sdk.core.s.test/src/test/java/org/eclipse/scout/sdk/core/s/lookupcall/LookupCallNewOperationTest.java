/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.lookupcall;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.AbstractBooleanPermutationArgumentsProvider;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutServerJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * <h3>{@link LookupCallNewOperationTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(TestingEnvironmentExtension.class)
public class LookupCallNewOperationTest {

  @DisplayName("Test LookupCall Creation")
  @ArgumentsSource(LookupCallTestArgumentsProvider.class)
  @ParameterizedTest(name = "withLookupService={0}, withServer={1}, withTest={2}")
  @ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutServerJavaEnvironmentFactory.class))
  public void testLookupCallCreation(boolean lookupServiceSuperTypeFqn, boolean serverSourceFolder, boolean testSourceFolder, TestingEnvironment env) {
    IScoutApi scoutApi = env.primaryEnvironment().requireApi(IScoutApi.class);

    LookupCallNewOperation op = new LookupCallNewOperation();
    op.setKeyType(JavaTypes.Double);
    op.setLookupCallName("My" + ISdkConstants.SUFFIX_LOOKUP_CALL);
    if (lookupServiceSuperTypeFqn) {
      op.setLookupServiceSuperType(scoutApi.AbstractLookupService().fqn());
    }
    op.setPackage("org.eclipse.scout.sdk.s2e.shared.test");
    if (serverSourceFolder) {
      op.setServerSourceFolder(env.getTestingSourceFolder());
    }
    op.setSharedSourceFolder(env.getTestingSourceFolder());
    op.setSuperType(scoutApi.LookupCall().fqn());
    if (testSourceFolder) {
      op.setTestSourceFolder(env.getTestingSourceFolder());
    }
    op.setServerSession(scoutApi.IServerSession().fqn());

    env.run(op);

    assertNotNull(op.getCreatedLookupCall());
    if (testSourceFolder) {
      assertNotNull(op.getCreatedLookupCallTest());
    }
    if (serverSourceFolder && lookupServiceSuperTypeFqn) {
      assertNotNull(op.getCreatedLookupServiceIfc());
      assertNotNull(op.getCreatedLookupServiceImpl());
    }
  }

  private static class LookupCallTestArgumentsProvider extends AbstractBooleanPermutationArgumentsProvider {
    protected LookupCallTestArgumentsProvider() {
      super(3);
    }
  }
}
