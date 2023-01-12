/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.form;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.AbstractBooleanPermutationArgumentsProvider;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ClassIdAutoCreationExtension;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
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
@ExtendWith(ClassIdAutoCreationExtension.class)
@ExtendWithTestingEnvironment(
    primary = @ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class),
    dto = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class FormNewOperationTest {

  @DisplayName("Test Form Creation")
  @ArgumentsSource(FormTestArgumentsProvider.class)
  @ParameterizedTest(name = "withDTO={0}, withPermission={1}, withService={2}, withClientTest={3}, withServerTest={4}, withAttributes={5}")
  public void testFormCreation(boolean isCreateFormData, boolean isCreatePermissions, boolean isCreateService,
      boolean sourceFolderClientTest, boolean sourceFolderServerTest, boolean withAttributes, TestingEnvironment env) {
    var scoutApi = env.primaryEnvironment().requireApi(IScoutApi.class);
    var fno = new FormNewOperation();
    fno.setClientPackage("org.eclipse.scout.sdk.s2e.client.test");
    fno.setClientSourceFolder(env.primarySourceFolder());
    if (sourceFolderClientTest) {
      fno.setClientTestSourceFolder(env.primarySourceFolder());
    }
    fno.setCreateFormData(isCreateFormData);
    fno.setCreatePermissions(isCreatePermissions);
    fno.setCreateOrAppendService(isCreateService);
    fno.setFormDataSourceFolder(env.dtoSourceFolder());
    fno.setFormName("My" + ISdkConstants.SUFFIX_FORM);
    fno.setServerSourceFolder(env.primarySourceFolder());
    if (sourceFolderServerTest) {
      fno.setServerTestSourceFolder(env.primarySourceFolder());
    }
    fno.setSharedSourceFolder(env.dtoSourceFolder());
    fno.setSuperType(scoutApi.AbstractForm().fqn());

    List<String> attributes = null;
    if (withAttributes) {
      attributes = new ArrayList<>(2);
      attributes.add("LastName");
      attributes.add("FirstName");
    }
    fno.setAttributes(attributes);

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
      super(6);
    }
  }
}
