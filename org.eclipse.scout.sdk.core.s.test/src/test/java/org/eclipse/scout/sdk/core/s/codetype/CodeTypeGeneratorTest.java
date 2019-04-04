/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s.codetype;

import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertEqualsRefFile;
import static org.eclipse.scout.sdk.core.testing.SdkAssertions.assertNoCompileErrors;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link CodeTypeGeneratorTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(JavaEnvironmentExtension.class)
@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class CodeTypeGeneratorTest {

  @Test
  public void testCodeTypeAllParams(IJavaEnvironment env) {
    String superType = IScoutRuntimeTypes.AbstractCodeType + JavaTypes.C_GENERIC_START + String.class.getName() + JavaTypes.C_COMMA + JavaTypes.Long + JavaTypes.C_GENERIC_END;

    CodeTypeGenerator<?> ctg = new CodeTypeGenerator<>()
        .withPackageName("org.eclipse.scout.sdk.core.s.test")
        .withElementName("MyCodeType")
        .withClassIdValue("whocares")
        .withCodeTypeIdDataType(String.class.getName())
        .withIdValueBuilder(b -> b.stringLiteral("id_value"))
        .withSuperClass(superType);

    assertEqualsRefFile(env, "org/eclipse/scout/sdk/core/s/generator/codetype/CodeTypeTest1.txt", ctg);
    assertNoCompileErrors(env, ctg);
  }
}
