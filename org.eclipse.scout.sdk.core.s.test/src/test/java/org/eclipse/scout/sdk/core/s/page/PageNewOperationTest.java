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
package org.eclipse.scout.sdk.core.s.page;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
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
 * <h3>{@link PageNewOperationTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(TestingEnvironmentExtension.class)
@ExtendWithTestingEnvironment(
    primary = @ExtendWithJavaEnvironmentFactory(ScoutFullJavaEnvironmentFactory.class),
    dto = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class PageNewOperationTest {

  @DisplayName("Test Page Creation")
  @ArgumentsSource(PageTestArgumentsProvider.class)
  @ParameterizedTest(name = "withDTO={0}, withServer={1}, withShared={2}, withTest={3}, isPageWithTable={4}, withAbstractPage={5}")
  public void testPageCreation(boolean dtoSourceFolder, boolean serverSourceFolder, boolean sharedSourceFolder,
      boolean testSourceFolder, boolean isPageWithTable, boolean isCreateAbstractPage, TestingEnvironment env) {
    IScoutApi scoutApi = env.primaryEnvironment().requireApi(IScoutApi.class);

    PageNewOperation pno = new PageNewOperation();
    pno.setClientSourceFolder(env.getTestingSourceFolder());
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
      pno.setPageDataSourceFolder(env.getTestingSourceFolder());
    }
    if (serverSourceFolder) {
      pno.setServerSourceFolder(env.getTestingSourceFolder());
    }
    if (sharedSourceFolder) {
      pno.setSharedSourceFolder(env.getTestingSourceFolder());
    }
    if (testSourceFolder) {
      pno.setTestSourceFolder(env.getTestingSourceFolder());
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
