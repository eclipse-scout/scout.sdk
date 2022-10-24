/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.codetype;

import static org.eclipse.scout.sdk.core.util.Strings.isBlank;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.CoreTestingUtils;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.junit.jupiter.api.Test;

import formdata.shared.services.process.replace.HugeCodeType;

/**
 * <h3>{@link HugeCodeTypeTest}</h3>
 * <p>
 * Tests that converting a huge class to a working copy and building the source does not consume to much resources
 *
 * @since 7.1.0
 */
@ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class)
public class HugeCodeTypeTest {
  @Test
  public void testHugeCodeType(IJavaEnvironment env) {
    var codeType = env.requireType(HugeCodeType.class.getName()).requireCompilationUnit();
    var workingCopy = codeType.toWorkingCopy();
    var javaSource = workingCopy.toJavaSource(codeType.javaEnvironment());

    assertFalse(isBlank(javaSource));

    var scoutApi = env.requireApi(IScoutApi.class);
    var superType = scoutApi.AbstractCodeType().fqn() + JavaTypes.C_GENERIC_START + String.class.getName() + JavaTypes.C_COMMA + JavaTypes.Long + JavaTypes.C_GENERIC_END;
    var packageName = HugeCodeType.class.getPackage().getName();
    var ctg = new CodeTypeGenerator<>()
        .withPackageName(packageName)
        .withElementName(HugeCodeType.class.getSimpleName())
        .withClassIdValue("whocares")
        .withCodeTypeIdDataType(String.class.getName())
        .withIdValueBuilder(b -> b.stringLiteral("id_value"))
        .withSuperClass(superType);
    var newType = CoreTestingUtils.registerCompilationUnit(codeType.javaEnvironment(), ctg);

    assertTrue(newType.source().orElseThrow().asCharSequence().length() < 400); // assert the huge one has been replaced with an empty code type
  }
}
