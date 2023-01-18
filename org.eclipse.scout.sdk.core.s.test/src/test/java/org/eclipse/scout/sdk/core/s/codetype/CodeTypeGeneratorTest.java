/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.codetype;

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertNoCompileErrors;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.junit.jupiter.api.Test;

/**
 * <h3>{@link CodeTypeGeneratorTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class CodeTypeGeneratorTest {

  @Test
  public void testCodeTypeAllParams(IJavaEnvironment env) {
    var ctg = new CodeTypeGenerator<>()
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .withElementName("MyCodeType")
        .withClassIdValue("whocares")
        .withCodeTypeIdDataType(String.class.getName())
        .withIdValueBuilder(b -> b.stringLiteral("id_value"))
        .withSuperClassFrom(IScoutApi.class, CodeTypeGeneratorTest::buildSuperClass);

    assertEqualsRefFile(env, "org/eclipse/scout/sdk/core/s/generator/codetype/CodeTypeTest1.txt", ctg);
    assertNoCompileErrors(env, ctg);
  }

  protected static String buildSuperClass(IScoutApi scoutApi) {
    return scoutApi.AbstractCodeType().fqn() + JavaTypes.C_GENERIC_START + String.class.getName() + JavaTypes.C_COMMA + JavaTypes.Long + JavaTypes.C_GENERIC_END;
  }
}
