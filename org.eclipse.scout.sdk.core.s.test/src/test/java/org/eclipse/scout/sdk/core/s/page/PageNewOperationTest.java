/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.page;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.AbstractBooleanPermutationArgumentsProvider;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutFullJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * <h3>{@link PageNewOperationTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWithTestingEnvironment(
    primary = @ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class),
    dto = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class PageNewOperationTest {

  @DisplayName("Test Page Creation")
  @ArgumentsSource(PageTestArgumentsProvider.class)
  @ParameterizedTest(name = "withDTO={0}, withServer={1}, withShared={2}, withTest={3}, isPageWithTable={4}, withAbstractPage={5}")
  public void testPageCreation(boolean dtoSourceFolder, boolean serverSourceFolder, boolean sharedSourceFolder,
      boolean testSourceFolder, boolean isPageWithTable, boolean isCreateAbstractPage, TestingEnvironment env) {
    var scoutApi = env.primaryEnvironment().requireApi(IScoutApi.class);

    var pno = new PageNewOperation();
    pno.setClientSourceFolder(env.primarySourceFolder());
    pno.setPackage("org.eclipse.scout.sdk.s2e.client.test");
    String suffix;
    String superType;
    if (isPageWithTable) {
      suffix = ISdkConstants.SUFFIX_PAGE_WITH_TABLE;
      superType = scoutApi.AbstractPageWithTable().fqn();
    }
    else {
      suffix = ISdkConstants.SUFFIX_PAGE_WITH_NODES;
      superType = scoutApi.AbstractPageWithNodes().fqn();
    }
    pno.setCreateAbstractPage(isCreateAbstractPage);
    pno.setPageName("My" + System.currentTimeMillis() + suffix);
    pno.setSuperType(superType);
    if (dtoSourceFolder) {
      pno.setPageDataSourceFolder(env.dtoSourceFolder());
    }
    if (serverSourceFolder) {
      pno.setServerSourceFolder(env.primarySourceFolder());
    }
    if (sharedSourceFolder) {
      pno.setSharedSourceFolder(env.dtoSourceFolder());
    }
    if (testSourceFolder) {
      pno.setTestSourceFolder(env.primarySourceFolder());
    }

    env.run(pno);

    assertNotNull(pno.getCreatedPage());
    if (isCreateAbstractPage) {
      assertNotNull(pno.getCreatedAbstractPage());
    }
    if (isPageWithTable && dtoSourceFolder) {
      assertNotNull(pno.getCreatedPageData());
      if (isCreateAbstractPage) {
        assertNotNull(pno.getCreatedAbstractPageData());
      }
      if (serverSourceFolder && sharedSourceFolder) {
        if (testSourceFolder) {
          assertNotNull(pno.getCreatedServiceTest());
        }
      }
    }
  }

  private static class PageTestArgumentsProvider extends AbstractBooleanPermutationArgumentsProvider {
    protected PageTestArgumentsProvider() {
      super(6);
    }
  }
}
