/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.java.apidef;

import org.eclipse.scout.sdk.core.java.apidef.MaxApiLevel;
import org.eclipse.scout.sdk.core.java.builder.expression.IExpressionBuilder;

@MaxApiLevel({24, 2})
@SuppressWarnings({"squid:S2176", "squid:S00118", "squid:S00100", "findbugs:NM_METHOD_NAMING_CONVENTION", "squid:S2166"}) // naming conventions
public interface Scout242Api extends IScoutApi, IScout242Api, IScoutChartApi, IScout22DoApi {

  PermissionId PERMISSION_ID = new PermissionId();

  @Override
  default PermissionId PermissionId() {
    return PERMISSION_ID;
  }

  class PermissionId implements IScout242Api.PermissionId {

    @Override
    public String fqn() {
      return "org.eclipse.scout.rt.api.data.security.PermissionId";
    }

    @Override
    public String ofMethodName() {
      return "of";
    }
  }

  @Override
  default boolean createPermissionIdField() {
    return true;
  }

  @Override
  default String getPermissionIdFieldDataTypeFqn() {
    return PermissionId().fqn();
  }

  @Override
  default void appendPermissionIdFieldValue(IExpressionBuilder<?> builder, String permissionName) {
    builder.ref(PermissionId().fqn()).dot().append(PermissionId().ofMethodName()).parenthesisOpen().stringLiteral(permissionName).parenthesisClose();
  }
}
