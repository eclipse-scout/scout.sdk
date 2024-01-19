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

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;

import org.eclipse.scout.sdk.core.java.generator.method.MethodGenerator;
import org.eclipse.scout.sdk.core.java.generator.methodparam.MethodParameterGenerator;
import org.eclipse.scout.sdk.core.java.model.api.Flags;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.testing.context.UsernameExtension;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.service.ServiceInterfaceGenerator;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutClientJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link PageGeneratorTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(UsernameExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutClientJavaEnvironmentFactory.class)
public class PageGeneratorTest {

  private static final String REF_FILE_FOLDER = "org/eclipse/scout/sdk/core/s/generator/page/";

  @Test
  public void testPageWithTable(IJavaEnvironment env) {
    for (var i = 0; i < 2; i++) {
      // page service
      var dataFetchMethodName = "getTestTableData";
      var svcIfcGenerator = new ServiceInterfaceGenerator<>()
          .withPackageName("org.eclipse.scout.sdk.core.s.test")
          .withElementName("IMyPageService" + i)
          .withMethod(MethodGenerator.create()
              .withElementName(dataFetchMethodName)
              .withReturnType("formdata.shared.services.pages.BaseTablePageData")
              .withParameter(MethodParameterGenerator.create()
                  .withElementName("filter")
                  .withDataTypeFrom(IScoutApi.class, api -> api.SearchFilter().fqn())));

      assertEqualsRefFile(env, REF_FILE_FOLDER + "ServiceTest" + (i + 1) + ".txt", svcIfcGenerator);
      var createdSvcIfc = assertNoCompileErrors(env, svcIfcGenerator);

      // page
      var pageGenerator = new PageGenerator<>()
          .withPackageName("org.eclipse.scout.sdk.core.s.test")
          .withElementName("MyTablePage" + i)
          .withClassIdValue("whatever")
          .withTableClassIdValue("whatever2")
          .withPageData("formdata.shared.services.pages.BaseTablePageData")
          .withDataFetchMethodName(dataFetchMethodName)
          .asPageWithTable(true)
          .withFlags(i == 1 ? Flags.AccAbstract : Flags.AccPublic)
          .withNlsMethod(i == 1)
          .withPageServiceInterface(createdSvcIfc.name());

      assertEqualsRefFile(env, REF_FILE_FOLDER + "PageTest" + (i + 1) + ".txt", pageGenerator);
      assertNoCompileErrors(env, pageGenerator);
    }
  }

  @Test
  public void testPageWithNodes(IJavaEnvironment env) {
    for (var i = 0; i < 2; i++) {
      // page
      var pageGenerator = new PageGenerator<>()
          .withElementName("MyNodePage" + i)
          .withClassIdValue("whatever")
          .asPageWithTable(false)
          .withFlags(i == 1 ? Flags.AccAbstract : Flags.AccPublic);

      assertEqualsRefFile(env, REF_FILE_FOLDER + "NodePageTest" + (i + 1) + ".txt", pageGenerator);
      assertNoCompileErrors(env, pageGenerator);
    }
  }
}
