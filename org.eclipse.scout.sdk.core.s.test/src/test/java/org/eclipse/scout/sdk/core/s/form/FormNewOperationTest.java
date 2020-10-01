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
package org.eclipse.scout.sdk.core.s.form;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.AbstractBooleanPermutationArgumentsProvider;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ClassIdAutoCreationExtension;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironmentExtension;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * <h3>{@link FormNewOperationTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWith(ClassIdAutoCreationExtension.class)
@ExtendWithTestingEnvironment(
    primary = @ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class),
    dto = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class FormNewOperationTest {

  @DisplayName("Test Form Creation")
  @ArgumentsSource(FormTestArgumentsProvider.class)
  @ParameterizedTest(name = "withDTO={0}, withPermission={1}, withService={2}, withClientTest={3}, withServerTest={4}")
  public void testFormCreation(boolean isCreateFormData, boolean isCreatePermissions, boolean isCreateService,
      boolean sourceFolderClientTest, boolean sourceFolderServerTest, TestingEnvironment env) {
    IScoutApi scoutApi = env.primaryEnvironment().requireApi(IScoutApi.class);
    FormNewOperation fno = new FormNewOperation();
    fno.setClientPackage("org.eclipse.scout.sdk.s2e.client.test");
    fno.setClientSourceFolder(env.getTestingSourceFolder());
    if (sourceFolderClientTest) {
      fno.setClientTestSourceFolder(env.getTestingSourceFolder());
    }
    fno.setCreateFormData(isCreateFormData);
    fno.setCreatePermissions(isCreatePermissions);
    fno.setCreateService(isCreateService);
    fno.setFormDataSourceFolder(env.getTestingSourceFolder());
    fno.setFormName("My" + ISdkConstants.SUFFIX_FORM);
    fno.setServerSourceFolder(env.getTestingSourceFolder());
    if (sourceFolderServerTest) {
      fno.setServerTestSourceFolder(env.getTestingSourceFolder());
    }
    fno.setSharedSourceFolder(env.getTestingSourceFolder());
    fno.setSuperType(scoutApi.AbstractForm().fqn());

    env.run(fno);

    assertNotNull(fno.getCreatedForm());
    if (isCreateFormData) {
      assertNotNull(fno.getCreatedFormData());
    }
    if (isCreatePermissions) {
      assertNotNull(fno.getCreatedUpdatePermission());
      assertNotNull(fno.getCreatedCreatePermission());
      assertNotNull(fno.getCreatedReadPermission());
    }
    if (isCreateService) {
      assertNotNull(fno.getCreatedServiceImpl());
      assertNotNull(fno.getCreatedServiceInterface());
    }
    if (sourceFolderClientTest) {
      assertNotNull(fno.getCreatedFormTest());
    }
    if (isCreateService && sourceFolderServerTest) {
      assertNotNull(fno.getCreatedServiceTest());
    }
  }

  private static class FormTestArgumentsProvider extends AbstractBooleanPermutationArgumentsProvider {
    protected FormTestArgumentsProvider() {
      super(5);
    }
  }
}
