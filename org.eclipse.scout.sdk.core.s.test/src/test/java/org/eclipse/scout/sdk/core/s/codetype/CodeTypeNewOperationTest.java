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

import static org.eclipse.scout.sdk.core.java.testing.SdkJavaAssertions.assertFieldExist;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.sdk.core.java.JavaTypes;
import org.eclipse.scout.sdk.core.java.model.api.IType;
import org.eclipse.scout.sdk.core.java.model.api.MetaValueType;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.ISdkConstants;
import org.eclipse.scout.sdk.core.s.java.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.testing.ScoutFixtureHelper.ScoutSharedJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.s.testing.context.ExtendWithTestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.TestingEnvironment;
import org.eclipse.scout.sdk.core.s.testing.context.UniqueIdExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * <h3>{@link CodeTypeNewOperationTest}</h3>
 *
 * @since 5.2.0
 */
@ExtendWith(UniqueIdExtension.class)
@ExtendWithTestingEnvironment(primary = @ExtendWithJavaEnvironmentFactory(ScoutSharedJavaEnvironmentFactory.class))
public class CodeTypeNewOperationTest {

  @Test
  public void testCodeTypeLongInteger(TestingEnvironment env) {
    var codeType = testCodeTypeCreation(JavaTypes.Long, JavaTypes.Integer, env);
    var id = assertFieldExist(codeType, CodeTypeGenerator.ID_CONSTANT_NAME);
    assertEquals(MetaValueType.Long, id.constantValue().orElseThrow().type());
    assertEquals(UniqueIdExtension.UNIQUE_ID, id.constantValue().orElseThrow().as(Long.class).longValue());
  }

  @Test
  public void testCodeTypeComplex(TestingEnvironment env) {
    var codeTypeIdFqn = List.class.getName() + JavaTypes.C_GENERIC_START + BigDecimal.class.getName() + JavaTypes.C_GENERIC_END;
    var codeIdFqn = Map.class.getName() + JavaTypes.C_GENERIC_START + BigDecimal.class.getName() + ", " + CharSequence.class.getName() + JavaTypes.C_GENERIC_END;
    testCodeTypeCreation(codeTypeIdFqn, codeIdFqn, env);
  }

  protected static IType testCodeTypeCreation(String codeTypeIdFqn, String codeIdFqn, TestingEnvironment env) {
    var scoutApi = env.primaryEnvironment().requireApi(IScoutApi.class);
    var superTypeFqn = scoutApi.AbstractCodeType().fqn() + JavaTypes.C_GENERIC_START + codeTypeIdFqn + ", " + codeIdFqn + JavaTypes.C_GENERIC_END;
    var op = new CodeTypeNewOperation();
    op.setCodeTypeIdDataType(codeTypeIdFqn);
    op.setCodeTypeName("My" + ISdkConstants.SUFFIX_CODE_TYPE);
    op.setPackage("org.eclipse.scout.sdk.s2e.shared.test");
    op.setSharedSourceFolder(env.primarySourceFolder());
    op.setSuperType(superTypeFqn);

    env.run(op);
    assertNotNull(op.getCreatedCodeType());
    return op.getCreatedCodeType().result();
  }
}
